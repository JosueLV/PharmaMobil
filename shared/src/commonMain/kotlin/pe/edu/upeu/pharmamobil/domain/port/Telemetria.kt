package pe.edu.upeu.pharmamobil.domain.port

interface Telemetria {
    val disponible: Boolean
    fun registrarError(e: Throwable, contexto: Map<String, String> = emptyMap())
    fun registrarEvento(nombre: String, datos: Map<String, String> = emptyMap())
}

/** Implementación para pruebas sin SDK ni red. Koin utiliza TelemetriaFirebase en la app. */
object TelemetriaNula : Telemetria {
    override val disponible = false
    override fun registrarError(e: Throwable, contexto: Map<String, String>) = Unit
    override fun registrarEvento(nombre: String, datos: Map<String, String>) = Unit
}
