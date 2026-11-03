package pe.edu.upeu.pharmamobil.domain.presentation

import pe.edu.upeu.pharmamobil.domain.presentation.producto.textoUltimaSincronizacion
import kotlin.test.Test
import kotlin.test.assertEquals

class AntiguedadDatosTest {
    @Test
    fun sinSincronizacion_loIndicaExplicitamente() {
        assertEquals("Nunca sincronizado", textoUltimaSincronizacion(null, 0L))
    }

    @Test
    fun muestraMinutosHorasYDiasConSingularYPlural() {
        val casos = mapOf(
            0L to "Actualizado hace un momento",
            1L to "Actualizado hace 1 minuto",
            2L to "Actualizado hace 2 minutos",
            59L to "Actualizado hace 59 minutos",
            60L to "Actualizado hace 1 hora",
            120L to "Actualizado hace 2 horas",
            1_439L to "Actualizado hace 23 horas",
            1_440L to "Actualizado hace 1 día",
            2_880L to "Actualizado hace 2 días"
        )
        casos.forEach { (minutos, esperado) ->
            assertEquals(esperado, textoUltimaSincronizacion(0L, minutos * 60_000L))
        }
    }

    @Test
    fun relojAtrasado_noMuestraTiempoNegativo() {
        assertEquals("Actualizado hace un momento", textoUltimaSincronizacion(60_000L, 0L))
    }
}
