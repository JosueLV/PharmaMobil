package pe.edu.upeu.pharmamobil.domain.presentation.permiso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso

data class PermisosUiState(val estado: EstadoPermiso? = null, val solicitando: Boolean = false)

class PermisosViewModel(private val gestor: GestorPermisos) : ViewModel() {
    private val _estado = MutableStateFlow(PermisosUiState())
    val estado = _estado.asStateFlow()

    fun consultar() {
        if (_estado.value.solicitando) return
        viewModelScope.launch { _estado.update { it.copy(estado = gestor.estado(Permiso.CAMARA)) } }
    }

    fun solicitar() {
        if (_estado.value.solicitando) return
        _estado.update { it.copy(solicitando = true) }
        viewModelScope.launch {
            try {
                val resultado = gestor.solicitar(Permiso.CAMARA)
                _estado.update { it.copy(estado = resultado) }
            } finally {
                _estado.update { it.copy(solicitando = false) }
            }
        }
    }

    fun abrirAjustes() = gestor.abrirAjustes()
}
