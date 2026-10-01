package pe.edu.upeu.pharmamobil.domain.presentation.producto

import pe.edu.upeu.pharmamobil.domain.model.Producto

sealed interface ProductoListaEstado {
    data object Cargando : ProductoListaEstado
    data object SinProductos : ProductoListaEstado
    data class ConProductos(val productos: List<Producto>) : ProductoListaEstado
    data class Error(val mensaje: String) : ProductoListaEstado
}

sealed interface Operacion {
    data object Inactiva : Operacion
    data class EnCurso(val tipo: Tipo, val productoId: Long? = null) : Operacion
    data class Fallida(val mensaje: String) : Operacion
    enum class Tipo { Crear, Actualizar, Eliminar }
}

data class ProductoFormularioEstado(
    val idEditando: Long? = null,
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val activo: Boolean = true,
    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null,
    val enviando: Boolean = false
) {
    val estaEditando: Boolean get() = idEditando != null
}

data class ProductoUiState(
    val lista: ProductoListaEstado = ProductoListaEstado.Cargando,
    val formulario: ProductoFormularioEstado = ProductoFormularioEstado(),
    val operacion: Operacion = Operacion.Inactiva,
    val mensajeExito: String? = null
)