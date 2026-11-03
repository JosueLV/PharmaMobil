package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobil.domain.model.ResumenSync
import pe.edu.upeu.pharmamobil.domain.port.ColaProductos
import pe.edu.upeu.pharmamobil.domain.port.EnvioProductos

class SincronizarUseCase(
    private val local: ColaProductos,
    private val remoto: EnvioProductos,
    private val ahora: () -> Long
) {
    suspend operator fun invoke(): ResumenSync {
        var enviadas = 0
        var fallidas = 0
        val errores = mutableMapOf<Long, ErrorApi>()
        for (fila in local.pendientesPorEnviar(MAX_INTENTOS, ahora())) {
            when (val resultado = remoto.enviar(fila)) {
                is ResultadoEnvio.Exito -> {
                    local.confirmarEnvio(fila.id, resultado.producto)
                    enviadas++
                }
                is ResultadoEnvio.Transitorio -> {
                    // El servidor no ofrece idempotencia para POST. No repetimos una creación
                    // cuyo resultado podría haberse perdido sin confirmación del usuario.
                    local.sumarIntento(fila.id, resultado.motivo,
                        ahora() + espera(fila.intentos), confirmacion = true)
                    errores[fila.id] = resultado.error
                    if (fila.intentos + 1 >= MAX_INTENTOS) fallidas++
                }
                is ResultadoEnvio.Permanente -> {
                    local.marcarFallida(fila.id, resultado.motivo, MAX_INTENTOS)
                    errores[fila.id] = resultado.error
                    fallidas++
                    if (resultado.error is ErrorApi.NoAutenticado) {
                        // No se envía el resto de la cola con una sesión rechazada.
                        throw ErrorApiException(resultado.error)
                    }
                }
            }
        }
        // Una descarga fallida no borra ni pendientes ni respuestas POST confirmadas.
        val productos = remoto.listar()
        local.reemplazarSincronizados(productos, ahora())
        return ResumenSync(enviadas, fallidas, local.contarPendientes(MAX_INTENTOS), errores)
    }

    companion object {
        const val MAX_INTENTOS = 5
        // La espera se persiste en SQLite, por lo que sigue vigente tras cerrar la app.
        // No mantenemos la pantalla bloqueada con delay ni creamos un bucle de reintentos.
        fun espera(intentos: Int): Long = 1_000L shl intentos.coerceIn(0, 4)
    }
}
