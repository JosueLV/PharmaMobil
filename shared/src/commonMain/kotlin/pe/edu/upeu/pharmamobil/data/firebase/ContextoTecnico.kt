package pe.edu.upeu.pharmamobil.data.firebase

// Lista cerrada: ningún texto del usuario, nombre de producto, token, URL o cuerpo HTTP.
internal fun contextoTecnico(datos: Map<String, String>): Map<String, String> {
    val permitidos = mapOf(
        "pantalla" to setOf("productos", "permisos", "diagnostico"),
        "operacion" to setOf("sincronizar", "crear", "actualizar", "eliminar", "reintentar", "prueba"),
        "resultado" to setOf("correcto", "error", "pendiente")
    )
    return datos.filter { (clave, valor) -> valor in permitidos[clave].orEmpty() }
}

internal fun nombreEventoTecnico(nombre: String): String =
    if (nombre in setOf("sincronizacion_completada", "prueba_integrador")) nombre else "evento_tecnico"
