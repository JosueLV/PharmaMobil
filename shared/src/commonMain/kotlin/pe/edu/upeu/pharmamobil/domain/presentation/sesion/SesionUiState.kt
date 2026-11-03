package pe.edu.upeu.pharmamobil.domain.presentation.sesion

import pe.edu.upeu.pharmamobil.domain.model.Sesion

data class SesionUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val sesion: Sesion? = null,
    // true mientras se comprueba si ya habia una sesion guardada al abrir la app
    val iniciando: Boolean = true
) {
    val puedeEnviar: Boolean
        get() = usuario.isNotBlank() && clave.isNotBlank() && !cargando
}