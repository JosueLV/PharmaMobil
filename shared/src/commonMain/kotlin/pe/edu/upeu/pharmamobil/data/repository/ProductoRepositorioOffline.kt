package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.mapper.toRequest
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import kotlin.time.Clock

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long,
    private val ahora: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) : ProductoRepository {

    private val mutexSincronizacion = Mutex()

    override fun observar(): Flow<List<Producto>> = local.observar()

    override fun observarUltimaSincronizacion(): Flow<Long?> = local.observarUltimaSincronizacion()

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = local.guardarPendiente(producto)
        intentar { sincronizar() }
        return guardado
    }

    override suspend fun sincronizar() {
        mutexSincronizacion.withLock {
            ejecutarLlamada {
                subirPendientes() // 1. primero subir
                // La fecha y la lista cambian juntas solo cuando termina toda la descarga.
                local.reemplazarSincronizados(descargar(), ahora())
            }.getOrThrow()
        }
    }

    private suspend fun descargar(): List<Producto> = ejecutarLlamada {
        val productos = mutableListOf<Producto>()
        var pagina = 0
        do {
            val respuesta = api.listar(pagina = pagina++, tamanio = 1000)
            productos.addAll(respuesta.contenido.map { it.toDomain() })
        } while (!respuesta.ultima)
        productos
    }.getOrThrow()

    private suspend fun subirPendientes() {
        local.pendientes().forEach { pendiente ->
            val creado = api.crear(pendiente.toRequest(categoriaPorDefecto)).toDomain()
            local.confirmarEnvio(pendiente.id, creado)
        }
    }

    /** Un fallo de red no debe tumbar la operación local. */
    private suspend fun intentar(bloque: suspend () -> Unit) {
        try {
            bloque()
        } catch (e: ErrorApiException) {
            when (e.error) {
                ErrorApi.SinConexion, ErrorApi.TiempoAgotado -> {
                    // Queda pendiente; se reintentará al sincronizar.
                }
                else -> throw e // otros errores (401, 403, 500...) sí deben verse
            }
        }
    }

    // Operaciones que siguen yendo al servidor (no son parte del criterio offline-first)
    override suspend fun listar(): List<Producto> = descargar()

    override suspend fun obtener(id: Long): Producto =
        local.obtener(id) ?: ejecutarLlamada { api.obtener(id).toDomain() }.getOrThrow()

    override suspend fun actualizar(producto: Producto): Producto =
        api.actualizar(producto.id, producto.toRequest(categoriaPorDefecto)).toDomain()

    override suspend fun eliminar(id: Long) = api.eliminar(id)
}
