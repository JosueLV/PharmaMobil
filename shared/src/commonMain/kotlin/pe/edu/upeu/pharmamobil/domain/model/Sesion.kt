package pe.edu.upeu.pharmamobil.domain.model

import kotlinx.serialization.Serializable

data class Credenciales(val usuario: String, val clave: String)

@Serializable
data class Sesion(
    val token: String,
    val usuario: String,
    val nombreCompleto: String,
    val permisos: Set<String>
) {
    fun puede(permiso: String): Boolean = permiso in permisos
}
