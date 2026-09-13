package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class RegistrarProductoUseCase(
    private val productoRepository: ProductoRepository
) {

    data class Errores(
        val nombre: String? = null,
        val precio: String? = null,
        val stock: String? = null
    ) {
        val esValido: Boolean
            get() = nombre == null && precio == null && stock == null
    }

    class CamposInvalidosException(
        val errores: Errores
    ) : Exception("Hay campos del formulario con errores")

    suspend operator fun invoke(
        nombre: String,
        precio: String,
        stock: String
    ): Result<Producto> {

        val errores = validar(nombre, precio, stock)

        if (!errores.esValido) {
            return Result.failure(CamposInvalidosException(errores))
        }

        val producto = productoRepository.registrar(
            nombre = nombre,
            precio = precio.toDouble(),
            stock = stock.toInt()
        )

        return Result.success(producto)
    }

    private fun validar(nombre: String, precio: String, stock: String): Errores {

        val nombreError = if (nombre.isBlank()) "El nombre es obligatorio" else null

        val precioValor = precio.toDoubleOrNull()
        val precioError = when {
            precio.isBlank() -> "El precio es obligatorio"
            precioValor == null -> "El precio debe ser un número válido"
            precioValor <= 0 -> "El precio debe ser mayor a 0"
            else -> null
        }

        val stockValor = stock.toIntOrNull()
        val stockError = when {
            stock.isBlank() -> "El stock es obligatorio"
            stockValor == null -> "El stock debe ser un número entero"
            stockValor < 0 -> "El stock no puede ser negativo"
            else -> null
        }

        return Errores(nombreError, precioError, stockError)
    }
}