package pe.edu.upeu.pharmamobil.domain.usecase

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.local.driverEnMemoria
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.EnvioProductosRest
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SincronizacionRefinadaTest {
    @Test
    fun unFalloTransitorioSumaIntentoYNoMarcaFallida() = runTest {
        escenario {
            val producto = local.guardarPendiente(nuevo())
            val resumen = caso()
            val fila = requireNotNull(local.obtener(producto.id))
            assertEquals(1, fila.intentos)
            assertTrue(fila.pendiente)
            assertTrue(fila.requiereConfirmacion)
            assertNotNull(fila.ultimoError)
            assertEquals(1_000L, fila.proximoIntento)
            assertEquals(1, resumen.pendientes)
            assertEquals(0, resumen.fallidas)
            assertEquals(listOf("POST", "GET"), llamadas)
        }
    }

    @Test
    fun unFalloPermanenteMarcaLaFilaYNoLaReintenta() = runTest {
        escenario {
            estadoPost = HttpStatusCode.Conflict
            val producto = local.guardarPendiente(nuevo())
            caso()
            caso()
            val fila = requireNotNull(local.obtener(producto.id))
            assertEquals(5, fila.intentos)
            assertTrue(fila.pendiente)
            assertEquals(1L, local.observarFallidas(5).first())
            assertEquals(0L, local.observarPendientes(5).first())
            assertEquals(1, llamadas.count { it == "POST" })
        }
    }

    @Test
    fun esperaCreceHastaElLimite_ySeConservaAlReabrirLaBase() = runTest {
        escenario {
            val producto = local.guardarPendiente(nuevo())
            for (intento in 0 until 5) {
                if (intento > 0) {
                    ahora = requireNotNull(local.obtener(producto.id)).proximoIntento
                    local.autorizarReintento(producto.id, 5, ahora)
                }
                caso()
                val fila = requireNotNull(ProductoLocal(PharmaDb(driver)).obtener(producto.id))
                assertEquals(intento + 1, fila.intentos)
                assertEquals(ahora + (1_000L shl intento), fila.proximoIntento)
            }
            ahora += 60_000L
            caso()
            assertEquals(5, llamadas.count { it == "POST" })
            assertEquals(1L, local.observarFallidas(5).first())
        }
    }

    @Test
    fun confirmarAntesDeLaEspera_noHabilitaElReenvio() = runTest {
        escenario {
            val producto = local.guardarPendiente(nuevo())
            caso()
            local.autorizarReintento(producto.id, 5, 999L)
            ahora = 999L
            caso()
            assertEquals(1, llamadas.count { it == "POST" })
            assertTrue(requireNotNull(local.obtener(producto.id)).requiereConfirmacion)
        }
    }

    @Test
    fun falloTransitorio_noRepiteUnPostAunquePaseTiempo() = runTest {
        escenario {
            local.guardarPendiente(nuevo())
            caso()
            ahora += 60_000L
            caso()
            assertEquals(1, llamadas.count { it == "POST" })
        }
    }

    @Test
    fun confirmarReenvioDespuesDeLaEspera_conservaElIdDelServidor() = runTest {
        escenario {
            val producto = local.guardarPendiente(nuevo())
            caso()
            ahora = 1_000L
            local.autorizarReintento(producto.id, 5, ahora)
            estadoPost = HttpStatusCode.Created
            val resumen = caso()
            val guardado = local.observar().first().single()
            assertEquals(10L, guardado.id)
            assertFalse(guardado.pendiente)
            assertEquals(1, resumen.enviadas)
            assertEquals(0, resumen.pendientes)
        }
    }

    @Test
    fun bajarDatosConFalloDeEnvio_conservaLaFilaLocal() = runTest {
        escenario {
            val pendiente = local.guardarPendiente(nuevo())
            contieneRemoto = true
            caso()
            assertEquals(setOf(pendiente.id, 10L), local.observar().first().map { it.id }.toSet())
        }
    }

    @Test
    fun sesionVencida_detieneLaColaSinEnviarElResto() = runTest {
        escenario {
            estadoPost = HttpStatusCode.Unauthorized
            local.guardarPendiente(nuevo())
            local.guardarPendiente(nuevo().copy(nombre = "Segundo"))
            val error = assertFailsWith<ErrorApiException> { caso() }
            assertTrue(error.error is ErrorApi.NoAutenticado)
            assertEquals(listOf("POST"), llamadas)
            assertEquals(2, local.observar().first().size)
        }
    }

    @Test
    fun cancelarDurantePost_noHabilitaUnReintentoAutomatico() = runTest {
        escenario {
            cancelar = true
            val pendiente = local.guardarPendiente(nuevo())
            assertFailsWith<CancellationException> { caso() }
            assertTrue(requireNotNull(local.obtener(pendiente.id)).requiereConfirmacion)
            cancelar = false
            ahora = 10_000L
            caso()
            assertEquals(1, llamadas.count { it == "POST" })
        }
    }

    @Test
    fun corregirUnaFallida_conservaSuIdYRequiereConfirmacion() = runTest {
        escenario {
            estadoPost = HttpStatusCode.BadRequest
            val pendiente = local.guardarPendiente(nuevo())
            caso()
            val corregida = local.actualizarPendiente(pendiente.copy(nombre = "Corregido"))
            assertEquals(pendiente.id, corregida.id)
            assertEquals(0, corregida.intentos)
            assertTrue(corregida.requiereConfirmacion)
            caso()
            assertEquals(1, llamadas.count { it == "POST" })
            assertEquals(1, local.observar().first().size)
        }
    }

    private suspend fun escenario(bloque: suspend Escenario.() -> Unit) {
        val escenario = Escenario()
        try { escenario.bloque() } finally {
            escenario.cliente.close()
            escenario.motor.close()
            escenario.driver.close()
        }
    }

    private class Escenario {
        val driver = driverEnMemoria()
        val local = ProductoLocal(PharmaDb(driver))
        var ahora = 0L
        var estadoPost = HttpStatusCode.ServiceUnavailable
        var cancelar = false
        var contieneRemoto = false
        val llamadas = mutableListOf<String>()
        val motor = MockEngine { request ->
            val headers = headersOf(HttpHeaders.ContentType, "application/json")
            if (request.method == HttpMethod.Post) {
                llamadas += "POST"
                if (cancelar) throw CancellationException("Envío interrumpido")
                if (estadoPost == HttpStatusCode.Created) contieneRemoto = true
                respond(if (estadoPost == HttpStatusCode.Created) PRODUCTO else "{}", estadoPost, headers)
            } else {
                llamadas += "GET"
                respond("""{"contenido":[${if (contieneRemoto) PRODUCTO else ""}],"pagina":0,"tamanio":1000,"totalElementos":${if (contieneRemoto) 1 else 0},"totalPaginas":1,"ultima":true}""",
                    HttpStatusCode.OK, headers)
            }
        }
        val cliente = crearHttpClient(motor, esDepuracion = false)
        val caso = SincronizarUseCase(local, EnvioProductosRest(ProductoApi(cliente), 1L), { ahora })
    }

    private fun nuevo() = Producto(0L, "Paracetamol", 2.5, 10)
    companion object {
        private const val PRODUCTO = """{"id":10,"nombre":"Paracetamol","precio":2.5,"stock":10,"estado":true}"""
    }
}
