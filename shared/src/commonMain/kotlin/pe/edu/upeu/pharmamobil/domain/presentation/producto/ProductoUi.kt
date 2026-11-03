package pe.edu.upeu.pharmamobil.domain.presentation.producto

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.platform.formatearSoles

data class ProductoUi(
    val id: Long,
    val nombre: String,
    val precio: String,          // ya formateado: "S/ 12.50"
    val stock: Int,
    val activo: Boolean,
    val requiereReposicion: Boolean,
    val pendiente: Boolean = false,
    val intentos: Int = 0,
    val ultimoError: String? = null,
    val fallida: Boolean = false
)

fun Producto.toUi() = ProductoUi(
    id = id,
    nombre = nombre,
    precio = formatearSoles(precio),
    stock = stock,
    activo = activo,
    requiereReposicion = requiereReposicion(),
    pendiente = pendiente,
    intentos = intentos,
    ultimoError = ultimoError,
    fallida = pendiente && intentos >= pe.edu.upeu.pharmamobil.domain.usecase.SincronizarUseCase.MAX_INTENTOS
)
