package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.content.TextContent
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.local.driverEnMemoria
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductoRepositorioOfflineTest {

    @Test
    fun sincronizacionConRed_guardaProductosYElFlowEmiteLaListaCompleta() = runTest {
        conRepositorio {
            servidor += productoRemoto(1, "Paracetamol")
            servidor += productoRemoto(2, "Ibuprofeno", estado = false)
            tamanioPagina = 1 // También exige descargar todas las páginas.
            assertTrue(repositorio.observar().first().isEmpty())
            val emision = async(start = CoroutineStart.UNDISPATCHED) {
                repositorio.observar().first { it.size == 2 }
            }

            repositorio.sincronizar()

            assertEquals(servidor.map { it.toDomain() }.toSet(), emision.await().toSet())
            assertEquals(2, base.productoQueries.listarTodos().executeAsList().size)
            assertTrue(local.pendientes().isEmpty())
            assertEquals(instante, repositorio.observarUltimaSincronizacion().first())
        }
    }

    @Test
    fun registroSinRed_guardaLaMarcaPendienteYElFlowEmiteElProducto() = runTest {
        conRepositorio {
            conectado = false
            val guardado = repositorio.registrar(productoNuevo())

            assertTrue(guardado.id < 0)
            assertEquals(listOf(guardado), repositorio.observar().first())
            assertEquals(listOf(guardado), local.pendientes())
            assertEquals(1L, base.productoQueries.buscarPorId(guardado.id).executeAsOne().pendiente)
            assertTrue(guardado.pendiente)
            assertNull(repositorio.observarUltimaSincronizacion().first())
        }
    }

    @Test
    fun envioAlReconectar_enviaElPendienteYLoSustituyePorElIdReal() = runTest {
        conRepositorio {
            conectado = false
            val pendiente = repositorio.registrar(productoNuevo())
            conectado = true

            repositorio.sincronizar()

            val guardado = repositorio.observar().first().single()
            assertEquals(servidor.single().toDomain(), guardado)
            assertTrue(guardado.id > 0)
            assertFalse(guardado.pendiente)
            assertNull(local.obtener(pendiente.id))
            assertTrue(local.pendientes().isEmpty())
            assertEquals(1, envios)
            assertEquals(pendiente.nombre, servidor.single().nombre)
            assertEquals(0L, base.productoQueries.buscarPorId(guardado.id).executeAsOne().pendiente)
        }
    }

    @Test
    fun laBajadaNoBorraLoPendiente_creadoMientrasLlegaLaRespuesta() = runTest {
        conRepositorio {
            servidor += productoRemoto(7, "Producto del servidor")
            // Simula una escritura local entre la fase de subida y la respuesta del GET.
            // Así se comprueba que el reemplazo solo borra filas sincronizadas.
            alDescargar = { local.guardarPendiente(productoNuevo()) }

            repositorio.sincronizar()

            val pendiente = local.pendientes().single()
            val lista = repositorio.observar().first()
            assertEquals(2, lista.size)
            assertTrue(lista.contains(pendiente))
            assertTrue(lista.any { it.id == 7L && !it.pendiente })
            assertEquals(1L, base.productoQueries.buscarPorId(pendiente.id).executeAsOne().pendiente)
        }
    }

    @Test
    fun identificadoresSinColision_convivenElLocalNegativoYElRemotoPositivo() = runTest {
        conRepositorio {
            servidor += productoRemoto(1, "Remoto")
            alDescargar = { local.guardarPendiente(productoNuevo().copy(id = 1, nombre = "Local")) }

            repositorio.sincronizar()

            val lista = repositorio.observar().first()
            assertEquals(setOf(-1L, 1L), lista.map { it.id }.toSet())
            assertEquals("Local", lista.single { it.id == -1L }.nombre)
            assertEquals("Remoto", lista.single { it.id == 1L }.nombre)
            assertEquals(-1L, local.pendientes().single().id)
        }
    }

    @Test
    fun sincronizarDosVecesNoDuplica_niReenviaElProductoConfirmado() = runTest {
        conRepositorio {
            conectado = false
            repositorio.registrar(productoNuevo())
            conectado = true
            repositorio.sincronizar()
            val primeraLista = repositorio.observar().first()

            repositorio.sincronizar()

            assertEquals(primeraLista, repositorio.observar().first())
            assertEquals(1, primeraLista.size)
            assertEquals(1, servidor.size)
            assertEquals(1, envios)
            assertTrue(local.pendientes().isEmpty())
        }
    }

    @Test
    fun descargaFallidaDespuesDelEnvio_conservaElProductoConfirmadoYLaFechaAnterior() = runTest {
        conRepositorio {
            repositorio.sincronizar()
            val fechaAnterior = instante
            conectado = false
            repositorio.registrar(productoNuevo())
            conectado = true
            fallaDescarga = true
            instante += 60_000L

            val error = assertFailsWith<ErrorApiException> { repositorio.sincronizar() }

            assertEquals(ErrorApi.SinConexion, error.error)
            assertEquals(servidor.single().toDomain(), repositorio.observar().first().single())
            assertTrue(local.pendientes().isEmpty())
            assertEquals(fechaAnterior, repositorio.observarUltimaSincronizacion().first())
            fallaDescarga = false
            repositorio.sincronizar()
            assertEquals(1, envios)
            assertEquals(instante, repositorio.observarUltimaSincronizacion().first())
        }
    }

    @Test
    fun fechaYEstadoInactivo_seLeenDeLaMismaBaseAlRecrearElRepositorio() = runTest {
        conRepositorio {
            servidor += productoRemoto(9, "Inactivo", estado = false)
            repositorio.sincronizar()

            val restaurado = ProductoRepositorioOffline(ProductoLocal(PharmaDb(driver)), api, 1)

            assertEquals(instante, restaurado.observarUltimaSincronizacion().first())
            assertFalse(restaurado.observar().first().single().activo)
            assertFalse(restaurado.observar().first().single().pendiente)
        }
    }

    @Test
    fun sincronizacionesConcurrentes_noEnvianDosVecesLaMismaFila() = runTest {
        conRepositorio {
            conectado = false
            repositorio.registrar(productoNuevo())
            conectado = true

            val primera = async { repositorio.sincronizar() }
            val segunda = async { repositorio.sincronizar() }
            primera.await()
            segunda.await()

            assertEquals(1, envios)
            assertEquals(1, repositorio.observar().first().size)
        }
    }

    @Test
    fun respuesta403_seConservaComoErrorDePermisos() = runTest {
        conRepositorio {
            rechazo = HttpStatusCode.Forbidden
            val error = assertFailsWith<ErrorApiException> { repositorio.sincronizar() }
            assertEquals(ErrorApi.SinPermiso, error.error)
            assertNull(repositorio.observarUltimaSincronizacion().first())
        }
    }

    private suspend fun conRepositorio(bloque: suspend Escenario.() -> Unit) {
        val escenario = Escenario()
        try {
            escenario.bloque()
        } finally {
            escenario.cliente.close()
            escenario.engine.close()
            escenario.driver.close()
        }
    }

    private class Escenario {
        val driver = driverEnMemoria()
        val base = PharmaDb(driver)
        val local = ProductoLocal(base)
        val servidor = mutableListOf<ProductoResponseDto>()
        var conectado = true
        var fallaDescarga = false
        var rechazo: HttpStatusCode? = null
        var envios = 0
        var instante = 1_700_000_000_000L
        var tamanioPagina = 1000
        var alDescargar: (() -> Unit)? = null
        private val json = Json { encodeDefaults = true }
        private val cabeceras = headersOf(HttpHeaders.ContentType, "application/json")
        val engine = MockEngine { peticion ->
            if (!conectado) throw IOException("Sin red simulada")
            val estadoRechazo = rechazo
            if (estadoRechazo != null) {
                respond("{}", estadoRechazo, cabeceras)
            } else when (peticion.method) {
                HttpMethod.Post -> {
                    val cuerpo = (peticion.body as TextContent).text
                    val enviado = json.decodeFromString<ProductoRequestDto>(cuerpo)
                    val creado = ProductoResponseDto(
                        id = (servidor.maxOfOrNull { it.id } ?: 0L) + 1L,
                        nombre = enviado.nombre,
                        precio = enviado.precio,
                        stock = enviado.stock,
                        estado = enviado.estado,
                        categoriaId = enviado.categoriaId
                    )
                    servidor += creado
                    envios++
                    respond(json.encodeToString(creado), HttpStatusCode.Created, cabeceras)
                }
                HttpMethod.Get -> {
                    if (fallaDescarga) throw IOException("Se perdió la red después del envío")
                    alDescargar?.also { alDescargar = null }?.invoke()
                    val pagina = peticion.url.parameters["pagina"]?.toInt() ?: 0
                    val contenido = servidor.drop(pagina * tamanioPagina).take(tamanioPagina)
                    val respuesta = PaginaResponseDto(
                        contenido = contenido,
                        pagina = pagina,
                        tamanio = tamanioPagina,
                        totalElementos = servidor.size.toLong(),
                        totalPaginas = (servidor.size + tamanioPagina - 1) / tamanioPagina,
                        ultima = (pagina + 1) * tamanioPagina >= servidor.size
                    )
                    respond(json.encodeToString(respuesta), HttpStatusCode.OK, cabeceras)
                }
                else -> error("Petición inesperada: ${peticion.method}")
            }
        }
        val cliente = crearHttpClient(engine, esDepuracion = false)
        val api = ProductoApi(cliente)
        val repositorio = ProductoRepositorioOffline(local, api, 1L, ahora = { instante })
    }

    private fun productoNuevo() = Producto(0, "Ibuprofeno", 4.5, 20)

    private fun productoRemoto(id: Long, nombre: String, estado: Boolean = true) =
        ProductoResponseDto(id, nombre, 2.0, 50, estado)
}
