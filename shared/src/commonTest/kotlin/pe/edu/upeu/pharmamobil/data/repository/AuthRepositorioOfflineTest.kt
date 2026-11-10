package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.encodedPath
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobil.data.remote.dto.UsuarioActualDto
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositorioOfflineTest {
    @Test
    fun loginCorrecto_permiteRestaurarLaSesionSinRed() = runTest {
        conAutenticacion {
            val sesion = repositorio.iniciarSesion(Credenciales("supervisor", "prueba"))
            conectado = false

            // Una nueva instancia representa la reapertura de la aplicación.
            val restaurado = AuthRepositorioRest(cliente, almacen)
            assertEquals(sesion, restaurado.sesionGuardada())
            assertNotNull(almacen.leerToken())
        }
    }

    @Test
    fun tokenRechazadoAlReconectar_borraSesionYNotificaExpiracion() = runTest {
        conAutenticacion {
            repositorio.iniciarSesion(Credenciales("supervisor", "prueba"))
            conectado = false
            assertNotNull(repositorio.sesionGuardada())
            conectado = true
            estado = HttpStatusCode.Unauthorized

            assertNull(repositorio.sesionGuardada())
            assertTrue(expirada)
            assertNull(almacen.leerToken())
            assertNull(almacen.leerSesion())
        }
    }

    @Test
    fun cerrarSesion_eliminaTambienLaSesionOffline() = runTest {
        conAutenticacion {
            repositorio.iniciarSesion(Credenciales("supervisor", "prueba"))
            repositorio.cerrarSesion()
            conectado = false

            assertNull(repositorio.sesionGuardada())
            assertNull(almacen.leerSesion())
        }
    }

    @Test
    fun sinToken_noInventaUnaSesion() = runTest {
        conAutenticacion {
            conectado = false
            assertNull(repositorio.sesionGuardada())
        }
    }

    @Test
    fun cacheDeOtroToken_noRestauraPermisosDeOtroUsuario() = runTest {
        conAutenticacion {
            repositorio.iniciarSesion(Credenciales("supervisor", "prueba"))
            almacen.guardarToken("otro-token")
            conectado = false

            val error = assertFailsWith<ErrorSesionException> { repositorio.sesionGuardada() }
            assertEquals(ErrorSesion.SinConexion, error.error)
        }
    }

    @Test
    fun sesionAntigua_sinCacheSeActualizaAlValidarseConRed() = runTest {
        conAutenticacion {
            almacen.guardarToken("token-anterior")
            val validada = repositorio.sesionGuardada()
            conectado = false

            assertNotNull(validada)
            assertEquals(validada, AuthRepositorioRest(cliente, almacen).sesionGuardada())
        }
    }

    private suspend fun conAutenticacion(bloque: suspend Escenario.() -> Unit) {
        val escenario = Escenario()
        try {
            escenario.bloque()
        } finally {
            escenario.cliente.close()
            escenario.engine.close()
        }
    }

    private class Escenario {
        val almacen = AlmacenEnMemoria()
        var conectado = true
        var expirada = false
        var estado = HttpStatusCode.OK
        val engine = MockEngine { peticion ->
            if (!conectado) throw IOException("Modo avión simulado")
            val cuerpo = when {
                estado != HttpStatusCode.OK -> "{}"
                peticion.url.encodedPath.endsWith("/auth/login") -> Json.encodeToString(
                    LoginResponseDto("token-prueba", "Bearer", 3600, "supervisor", "Supervisor",
                        listOf("SUPERVISOR"), listOf("CATALOGO_ESCRIBIR"))
                )
                else -> Json.encodeToString(
                    UsuarioActualDto(1, "supervisor", "Supervisor", permisos = listOf("CATALOGO_ESCRIBIR"))
                )
            }
            respond(cuerpo, estado, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val cliente = crearHttpClient(engine, almacen, esDepuracion = false, alExpirarSesion = { expirada = true })
        val repositorio = AuthRepositorioRest(cliente, almacen)
    }

    private class AlmacenEnMemoria : AlmacenSeguro {
        private var token: String? = null
        private var sesion: String? = null
        override fun guardarToken(token: String) { this.token = token }
        override fun leerToken(): String? = token
        override fun borrarToken() { token = null }
        override fun guardarSesion(sesion: String) { this.sesion = sesion }
        override fun leerSesion(): String? = sesion
        override fun borrarSesion() { sesion = null }
    }
}
