package pe.edu.upeu.pharmamobil.domain.repository


import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepository
import kotlin.test.Test

class ProductoRepositoryTest {

    private val repository = ProductoRepository()

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