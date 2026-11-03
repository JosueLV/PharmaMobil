package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto

/**
 * Repositorio falso reutilizable para pruebas del ViewModel.
 * Hace de "base local": observar() emite cada cambio, como el Flow de SQLDelight.
 * Permite simular: lista inicial, error al registrar (tipificado como ErrorApi),
 * fallo al sincronizar (por ejemplo SinConexion) y una "puerta" que detiene
 * eliminar() hasta que el test decida continuarla.
 */
class FakeProductoRepository(
    productosIniciales: List<Producto> = emptyList(),
    private val errorAlListar: Throwable? = null,
    private val errorAlRegistrar: ErrorApi? = null,
    private val errorAlSincronizar: ErrorApi? = null,
    private val puertaEliminar: CompletableDeferred<Unit>? = null
) : ProductoRepository {

    private val productos = MutableStateFlow(productosIniciales)

    var seLlamoRegistrar: Boolean = false
        private set
    var seLlamoEliminar: Boolean = false
        private set
    var vecesQueSeListo: Int = 0
        private set

    override fun observar(): Flow<List<Producto>> = productos

    override fun observarUltimaSincronizacion(): Flow<Long?> = flowOf(null)

    override suspend fun sincronizar() {
        errorAlSincronizar?.let { throw ErrorApiException(it) }
    }

    override suspend fun listar(): List<Producto> {
        vecesQueSeListo++
        errorAlListar?.let { throw it }
        return productos.value
    }

    override suspend fun obtener(id: Long): Producto =
        productos.value.first { it.id == id }

    override suspend fun registrar(producto: Producto): Producto {
        seLlamoRegistrar = true
        errorAlRegistrar?.let { throw ErrorApiException(it) }
        val nuevo = producto.copy(id = (productos.value.maxOfOrNull { it.id } ?: 0L) + 1L)
        productos.update { it + nuevo }
        return nuevo
    }

    override suspend fun actualizar(producto: Producto): Producto {
        if (productos.value.none { it.id == producto.id }) {
            throw ErrorApiException(ErrorApi.NoEncontrado)
        }
        productos.update { lista -> lista.map { if (it.id == producto.id) producto else it } }
        return producto
    }

    override suspend fun eliminar(id: Long) {
        seLlamoEliminar = true
        puertaEliminar?.await()
        productos.update { lista -> lista.filter { it.id != id } }
    }
}
