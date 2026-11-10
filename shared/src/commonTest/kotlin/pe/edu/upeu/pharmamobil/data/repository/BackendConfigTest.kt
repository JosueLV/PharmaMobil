package pe.edu.upeu.pharmamobil.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals

class BackendConfigTest {
    @Test
    fun destinoFisicoConservaRutaApiYNoUsaHostDeEmulador() = runTest {
        var destino = ""
        val motor = MockEngine { solicitud ->
            destino = solicitud.url.toString()
            respond("{}", HttpStatusCode.OK)
        }
        val cliente = crearHttpClient(motor, esDepuracion = false,
            urlBase = "http://192.168.1.40:8080/api/v1/")
        try {
            cliente.get("productos/101")
            assertEquals("http://192.168.1.40:8080/api/v1/productos/101", destino)
        } finally { cliente.close() }
    }
}
