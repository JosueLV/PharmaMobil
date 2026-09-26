package pe.edu.upeu.pharmamobil.data.mapper

import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobil.domain.model.Producto

// La API pública (escuelajs) no expone stock ni estado activo —
// es una API genérica de productos, no de farmacia — por lo que
// se asigna un stock de referencia y se marca como activo por defecto.
fun ProductoDto.toDomain(): Producto = Producto(
    id = id.toLong(),
    nombre = title,
    precio = price,
    stock = 20,
    activo = true
)