package pe.edu.upeu.pharmamobil.data.mapper

import pe.edu.upeu.pharmamobil.data.remote.dto.UsuarioActualDto
import pe.edu.upeu.pharmamobil.domain.model.Sesion

fun UsuarioActualDto.aSesion(token: String) = Sesion(
    token = token,
    usuario = username,
    nombreCompleto = empleadoNombre ?: username,
    permisos = permisos.toSet()
)