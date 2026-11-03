package pe.edu.upeu.pharmamobil.data.mapper

import pe.edu.upeu.pharmamobil.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobil.domain.model.Sesion

fun LoginResponseDto.aSesion() = Sesion(
    token = token,
    usuario = username,
    nombreCompleto = nombreCompleto,
    permisos = permisos.toSet()
)