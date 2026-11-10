package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

class CerrarSesionUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke() = repositorio.cerrarSesion()
}
