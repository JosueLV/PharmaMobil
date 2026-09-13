package pe.edu.upeu.pharmamobil.domain.presentation.producto

import pe.edu.upeu.pharmamobil.domain.model.Producto

sealed interface ProductoListaEstado {
    data object Cargando : ProductoListaEstado
    data object SinProductos : ProductoListaEstado
    data class ConProductos(val productos: List<Producto>) : ProductoListaEstado
    data class Error(val mensaje: String) : ProductoListaEstado
}

data class ProductoFormularioEstado(
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null,
    val mensajeExito: String? = null,
    val enviando: Boolean = false
)

data class ProductoUiState(
    val lista: ProductoListaEstado = ProductoListaEstado.Cargando,
    val formulario: ProductoFormularioEstado = ProductoFormularioEstado()
)