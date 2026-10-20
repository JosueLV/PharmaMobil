package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.mapper.toRequest
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = local.guardarPendiente(producto)
        intentar { sincronizar() }
        return guardado
    }

    override suspend fun sincronizar() {
        subirPendientes()                          // 1. primero subir
        local.reemplazarSincronizados(descargar()) // 2. después bajar
    }

    private suspend fun descargar(): List<Producto> =
        api.listar(tamanio = 1000).contenido.map { it.toDomain() }

    private suspend fun subirPendientes() {
        local.pendientes().forEach { pendiente ->
            api.crear(pendiente.toRequest(categoriaPorDefecto))
            local.eliminar(pendiente.id) // ya está en el servidor, vuelve en la lista con su id real
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
        api.obtener(id).toDomain()

    override suspend fun actualizar(producto: Producto): Producto =
        api.actualizar(producto.id, producto.toRequest(categoriaPorDefecto)).toDomain()

    override suspend fun eliminar(id: Long) = api.eliminar(id)
}