package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.CompletableDeferred
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto

/**
 * Repositorio falso reutilizable para pruebas del ViewModel.
 * Permite simular: lista inicial, error al listar, error al registrar
 * (tipificado como ErrorApi) y una "puerta" opcional que detiene
 * eliminar() hasta que el test decida continuarla, para poder
 * observar el estado EnCurso antes de que la operacion termine.
 */
class FakeProductoRepository(
    productosIniciales: List<Producto> = emptyList(),
    private val errorAlListar: Throwable? = null,
    private val errorAlRegistrar: ErrorApi? = null,
    private val puertaEliminar: CompletableDeferred<Unit>? = null
) : ProductoRepository {

    private val productos = productosIniciales.toMutableList()

    var seLlamoRegistrar: Boolean = false
        private set
    var seLlamoEliminar: Boolean = false
        private set

    override suspend fun listar(): List<Producto> {
        errorAlListar?.let { throw it }
        return productos.toList()
    }

    override suspend fun obtener(id: Long): Producto =
        productos.first { it.id == id }

    override suspend fun registrar(producto: Producto): Producto {
        seLlamoRegistrar = true
        errorAlRegistrar?.let { throw ErrorApiException(it) }
        val nuevo = producto.copy(id = (productos.maxOfOrNull { it.id } ?: 0L) + 1L)
        productos.add(nuevo)
        return nuevo
    }

    override suspend fun actualizar(producto: Producto): Producto {
        val indice = productos.indexOfFirst { it.id == producto.id }
        if (indice == -1) throw ErrorApiException(ErrorApi.NoEncontrado)
        productos[indice] = producto
        return producto
    }

    override suspend fun eliminar(id: Long) {
        seLlamoEliminar = true
        puertaEliminar?.await()
        productos.removeAll { it.id == id }
    }
}