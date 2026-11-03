package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

class IniciarSesionUseCase(private val repositorio: AuthRepository) {

    suspend operator fun invoke(usuario: String, clave: String): Result<Sesion> {
        if (usuario.isBlank() || clave.isBlank()) {
            return Result.failure(ErrorSesionException(ErrorSesion.CredencialesInvalidas))
        }
        return try {
            Result.success(repositorio.iniciarSesion(Credenciales(usuario.trim(), clave)))
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}