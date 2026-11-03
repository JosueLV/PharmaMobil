package pe.edu.upeu.pharmamobil.domain.model

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Canal para avisar a la interfaz que el servidor rechazo el token (401)
 * en una llamada que no era el login. Lo emite el cliente HTTP y lo escucha SesionViewModel.
 */
class SesionEventos {
    private val _expirada = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expirada: SharedFlow<Unit> = _expirada.asSharedFlow()

    fun notificarExpirada() {
        _expirada.tryEmit(Unit)
    }
}