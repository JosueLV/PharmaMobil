package pe.edu.upeu.pharmamobil.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val activo: Boolean = true
) {
    companion object {
        const val STOCK_MINIMO = 5
    }

    fun requiereReposicion(): Boolean = stock < STOCK_MINIMO
}