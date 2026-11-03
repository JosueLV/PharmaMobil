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
import pe.edu.upeu.pharmamobil.domain.usecase.SincronizarUseCase
import kotlin.time.Clock

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long,
    private val ahora: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) : ProductoRepository {
    private val mutexSincronizacion = Mutex()
    private val remoto = EnvioProductosRest(api, categoriaPorDefecto)
    private val sincronizarCaso = SincronizarUseCase(local, remoto, ahora)

    override fun observar(): Flow<List<Producto>> = local.observar()
    override fun observarUltimaSincronizacion(): Flow<Long?> = local.observarUltimaSincronizacion()

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = local.guardarPendiente(producto)
        try {
            mutexSincronizacion.withLock {
                val resumen = sincronizarCaso()
                resumen.errores[guardado.id]?.let { throw ErrorApiException(it) }
            }
        } catch (e: ErrorApiException) {
            when (e.error) {
                ErrorApi.SinConexion, ErrorApi.TiempoAgotado, ErrorApi.Servidor -> Unit
                else -> throw e
            }
        }
        return local.obtener(guardado.id) ?: guardado
    }

    override suspend fun sincronizar() {
        mutexSincronizacion.withLock {
            val resumen = sincronizarCaso()
            resumen.errores.values.firstOrNull()?.let { throw ErrorApiException(it) }
        }
    }

    override suspend fun reintentar(id: Long) {
        mutexSincronizacion.withLock {
            val producto = local.obtener(id) ?: throw ErrorApiException(ErrorApi.NoEncontrado)
            if (!producto.pendiente) return@withLock
            if (producto.proximoIntento > ahora()) {
                val segundos = (producto.proximoIntento - ahora() + 999L) / 1_000L
                throw ErrorApiException(ErrorApi.Conflicto("Espera $segundos segundos antes de reintentar"))
            }
            local.autorizarReintento(id, SincronizarUseCase.MAX_INTENTOS, ahora())
            val resumen = sincronizarCaso()
            resumen.errores[id]?.let { throw ErrorApiException(it) }
        }
    }

    override suspend fun listar(): List<Producto> = remoto.listar()
    override suspend fun obtener(id: Long): Producto =
        local.obtener(id) ?: ejecutarLlamada { api.obtener(id).toDomain() }.getOrThrow()

    override suspend fun actualizar(producto: Producto): Producto = mutexSincronizacion.withLock {
        if (local.obtener(producto.id)?.pendiente == true) {
            local.actualizarPendiente(producto)
        } else {
            ejecutarLlamada { api.actualizar(producto.id, producto.toRequest(categoriaPorDefecto)).toDomain() }.getOrThrow()
        }
    }

    override suspend fun eliminar(id: Long) {
        mutexSincronizacion.withLock {
            if (local.obtener(id)?.pendiente == true) local.eliminar(id)
            else ejecutarLlamada { api.eliminar(id) }.getOrThrow()
        }
    }
}
