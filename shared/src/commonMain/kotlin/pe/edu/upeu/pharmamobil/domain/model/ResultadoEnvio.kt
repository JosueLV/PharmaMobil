package pe.edu.upeu.pharmamobil.domain.model

import pe.edu.upeu.pharmamobil.domain.error.ErrorApi

sealed interface ResultadoEnvio {
    data class Exito(val producto: Producto) : ResultadoEnvio
    data class Transitorio(val motivo: String, val error: ErrorApi) : ResultadoEnvio
    data class Permanente(val motivo: String, val error: ErrorApi) : ResultadoEnvio
}

data class ResumenSync(
    val enviadas: Int,
    val fallidas: Int,
    val pendientes: Int,
    val errores: Map<Long, ErrorApi> = emptyMap()
)
