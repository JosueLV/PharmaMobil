package pe.edu.upeu.pharmamobil.domain.error

sealed interface ErrorApi {
    data class Validacion(val porCampo: Map<String, String>) : ErrorApi
    data object NoEncontrado : ErrorApi
    data class Conflicto(val mensaje: String) : ErrorApi
    data class NoAutenticado(val mensaje: String) : ErrorApi   // 401
    data object SinPermiso : ErrorApi                          // 403
    data object Servidor : ErrorApi
    data object SinConexion : ErrorApi
    data object TiempoAgotado : ErrorApi
}

class ErrorApiException(val error: ErrorApi) : Exception()