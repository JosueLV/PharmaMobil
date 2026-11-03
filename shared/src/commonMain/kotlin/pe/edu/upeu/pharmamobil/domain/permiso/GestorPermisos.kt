package pe.edu.upeu.pharmamobil.domain.permiso

// La sesión exige resolver al menos un permiso real. Solo pedimos cámara.
enum class Permiso { CAMARA }

sealed interface EstadoPermiso {
    data object Concedido : EstadoPermiso
    data object Denegado : EstadoPermiso
    data object DenegadoParaSiempre : EstadoPermiso
}

interface GestorPermisos {
    suspend fun estado(permiso: Permiso): EstadoPermiso
    suspend fun solicitar(permiso: Permiso): EstadoPermiso
    fun abrirAjustes()
}

fun resolverEstadoPermiso(concedido: Boolean, yaSePidio: Boolean, procedeExplicar: Boolean): EstadoPermiso = when {
    concedido -> EstadoPermiso.Concedido
    !yaSePidio || procedeExplicar -> EstadoPermiso.Denegado
    else -> EstadoPermiso.DenegadoParaSiempre
}
