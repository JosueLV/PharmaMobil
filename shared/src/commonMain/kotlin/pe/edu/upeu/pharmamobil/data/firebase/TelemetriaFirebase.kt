package pe.edu.upeu.pharmamobil.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app
import dev.gitlive.firebase.crashlytics.crashlytics
import pe.edu.upeu.pharmamobil.domain.port.Telemetria

class TelemetriaFirebase : Telemetria {
    override val disponible: Boolean
        get() = runCatching { Firebase.app; true }.getOrDefault(false)

    override fun registrarError(e: Throwable, contexto: Map<String, String>) {
        if (!disponible) return
        // No se envían ni el mensaje original ni su causa: podrían contener datos personales.
        val seguro = IllegalStateException("Fallo técnico de PharmaMobil: ${e::class.simpleName}")
        runCatching {
            Firebase.crashlytics.recordException(seguro, contextoTecnico(contexto))
        }
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        if (!disponible) return
        // Es un registro de contexto de Crashlytics, no un evento de Google Analytics.
        runCatching {
            Firebase.crashlytics.log("${nombreEventoTecnico(nombre)} ${contextoTecnico(datos)}")
        }
    }
}
