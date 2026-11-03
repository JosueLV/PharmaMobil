package pe.edu.upeu.pharmamobil.domain.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobil.domain.model.SesionEventos
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase

class SesionViewModel(
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
    private val authRepository: AuthRepository,
    private val eventos: SesionEventos
) : ViewModel() {

    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    init {
        restaurarSesion()
        viewModelScope.launch {
            eventos.expirada.collect { cerrarPorExpiracion() }
        }
    }

    private fun restaurarSesion() {
        viewModelScope.launch {
            try {
                val sesion = authRepository.sesionGuardada()
                _uiState.update { it.copy(sesion = sesion, iniciando = false) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (e: Exception) {
                _uiState.update { it.copy(iniciando = false, error = mensajeDe(e)) }
            }
        }
    }

    fun onUsuarioChange(valor: String) {
        _uiState.update { it.copy(usuario = valor, error = null) }
    }

    fun onClaveChange(valor: String) {
        _uiState.update { it.copy(clave = valor, error = null) }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        if (!estado.puedeEnviar) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            iniciarSesionUseCase(estado.usuario, estado.clave)
                .onSuccess { sesion ->
                    // se limpia el formulario: la clave no debe quedarse en memoria de la pantalla
                    _uiState.value = SesionUiState(sesion = sesion, iniciando = false)
                }
                .onFailure { fallo ->
                    _uiState.update { it.copy(cargando = false, error = mensajeDe(fallo)) }
                }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            cerrarSesionUseCase()
            _uiState.value = SesionUiState(iniciando = false)
        }
    }

    // El servidor rechazo el token en medio del uso: se cierra la sesion y se vuelve al login con un aviso.
    private fun cerrarPorExpiracion() {
        viewModelScope.launch {
            cerrarSesionUseCase()
            _uiState.value = SesionUiState(
                iniciando = false,
                error = mensajeDe(ErrorSesionException(ErrorSesion.SesionExpirada))
            )
        }
    }

    private fun mensajeDe(fallo: Throwable): String =
        when ((fallo as? ErrorSesionException)?.error) {
            ErrorSesion.CredencialesInvalidas -> "Usuario o contraseña incorrectos"
            ErrorSesion.SesionExpirada -> "Tu sesión expiró. Inicia sesión nuevamente"
            ErrorSesion.SinPermiso -> "No tienes permisos para realizar esta operación"
            ErrorSesion.SinConexion -> "No hay conexión con el servidor"
            null -> "Ocurrió un error inesperado"
        }
}
