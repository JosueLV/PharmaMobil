package pe.edu.upeu.pharmamobil.domain.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioEnMemoria
import kotlin.test.Test

class ProductoRepositoryTest {

    // MockEngine simula una respuesta de red sin llamar a internet real.
    private val mockEngine = MockEngine { _ ->
        respond(
            content = "[]",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
    private val api = ProductoApi(crearHttpClient(mockEngine))
    private val repository = ProductoRepositorioEnMemoria(api)

    @Test
    fun probarObtenerProductos() = runTest {
        val productos = repository.obtenerProductos()
        println("Productos obtenidos: $productos")
    }

    @Test
    fun probarObservarEstados() = runTest {
        repository.observarEstados().collect { estado ->
            println("Estado recibido: $estado")
        }
    }

    @Test
    fun probarObservarProductos() = runTest {
        repository.observarProductos().collect { lista ->
            println("Lista recibida (${lista.size} productos): $lista")
        }
    }

    @Test
    fun probarCargarProductos() = runTest {
        repository.cargarProductos().collect { resultado ->
            println("Resultado recibido: $resultado")
        }
    }
}