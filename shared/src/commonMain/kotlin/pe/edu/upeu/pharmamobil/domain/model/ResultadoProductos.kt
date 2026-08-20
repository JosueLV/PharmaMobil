package pe.edu.upeu.pharmamobil.domain.model

sealed class ResultadoProductos {
    data object Cargando : ResultadoProductos()
    data class Exito(val productos: List<Producto>) : ResultadoProductos()
    data class Error(val mensaje: String) : ResultadoProductos()
}