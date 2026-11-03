package pe.edu.upeu.pharmamobil.domain.model

data class EstadoSincronizacion(
    val pendientes: Int = 0,
    val fallidas: Int = 0,
    val ultimaSync: Long? = null
)
