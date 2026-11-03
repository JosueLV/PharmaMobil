package pe.edu.upeu.pharmamobil.data.firebase

import kotlin.test.Test
import kotlin.test.assertEquals

class ContextoTecnicoTest {
    @Test
    fun descartaTokensNombresYValoresAjenoAlVocabularioTecnico() {
        val datos = mapOf("pantalla" to "productos", "operacion" to "sincronizar",
            "token" to "Bearer secreto", "usuario" to "Josue", "resultado" to "correo@ejemplo.com")
        assertEquals(mapOf("pantalla" to "productos", "operacion" to "sincronizar"), contextoTecnico(datos))
    }

    @Test
    fun nombreLibreNoSeEnviaComoEvento() {
        assertEquals("evento_tecnico", nombreEventoTecnico("token-personal"))
        assertEquals("prueba_integrador", nombreEventoTecnico("prueba_integrador"))
    }
}
