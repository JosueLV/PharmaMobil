package pe.edu.upeu.pharmamobil.data.remote

import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesionException

/**
 * Traduce un fallo de red/HTTP a la jerarquia de sesion.
 * En el login, un 401 significa credenciales incorrectas;
 * en cualquier otra llamada, significa sesion expirada.
 */
fun Throwable.aErrorSesion(enLogin: Boolean = false): ErrorSesionException {
    val error = when (val api = (this as? ErrorApiException)?.error) {
        is ErrorApi.NoAutenticado ->
            if (enLogin) ErrorSesion.CredencialesInvalidas else ErrorSesion.SesionExpirada
        ErrorApi.SinPermiso -> ErrorSesion.SinPermiso
        else -> ErrorSesion.SinConexion
    }
    return ErrorSesionException(error)
}