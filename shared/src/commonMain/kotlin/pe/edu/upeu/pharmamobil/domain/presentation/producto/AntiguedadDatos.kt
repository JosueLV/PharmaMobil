package pe.edu.upeu.pharmamobil.domain.presentation.producto

fun textoUltimaSincronizacion(ultimaSincronizacion: Long?, ahora: Long): String {
    if (ultimaSincronizacion == null) return "Nunca sincronizado"
    val minutos = ((ahora - ultimaSincronizacion).coerceAtLeast(0L)) / 60_000L
    return when {
        minutos == 0L -> "Actualizado hace un momento"
        minutos == 1L -> "Actualizado hace 1 minuto"
        minutos < 60L -> "Actualizado hace $minutos minutos"
        minutos < 120L -> "Actualizado hace 1 hora"
        minutos < 1_440L -> "Actualizado hace ${minutos / 60L} horas"
        minutos < 2_880L -> "Actualizado hace 1 día"
        else -> "Actualizado hace ${minutos / 1_440L} días"
    }
}
