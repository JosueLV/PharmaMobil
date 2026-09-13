package pe.edu.upeu.pharmamobil.domain.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.presentation.producto.ProductoListaEstado
import pe.edu.upeu.pharmamobil.domain.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue


@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun configurar() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun limpiar() {
        Dispatchers.resetMain()
    }


    private class RepositorioVacio : ProductoRepository {
        override suspend fun registrar(nombre: String, precio: Double, stock: Int): Producto {
            error("No debería llamarse registrar() en esta prueba")
        }

        override suspend fun listar(): List<Producto> = emptyList()
    }


    private class RepositorioConProductos : ProductoRepository {
        override suspend fun registrar(nombre: String, precio: Double, stock: Int): Producto {
            error("No debería llamarse registrar() en esta prueba")
        }

        override suspend fun listar(): List<Producto> = listOf(
            Producto(id = 1, nombre = "Paracetamol", precio = 15.50, stock = 100),
            Producto(id = 2, nombre = "Ibuprofeno", precio = 18.90, stock = 50),
            Producto(id = 3, nombre = "Amoxicilina", precio = 25.00, stock = 5)
        )
    }


    private class RepositorioConError : ProductoRepository {
        override suspend fun registrar(nombre: String, precio: Double, stock: Int): Producto {
            error("No debería llamarse registrar() en esta prueba")
        }

        override suspend fun listar(): List<Producto> {
            throw Exception("Error simulado de conexión")
        }
    }


    private class RepositorioEspiaRegistro : ProductoRepository {
        var seLlamoRegistrar: Boolean = false

        override suspend fun registrar(nombre: String, precio: Double, stock: Int): Producto {
            seLlamoRegistrar = true
            return Producto(id = 99, nombre = nombre, precio = precio, stock = stock)
        }

        override suspend fun listar(): List<Producto> = emptyList()
    }

    @Test
    fun repositorioVacio_produceFaseSinProductos() = runTest(dispatcher) {
        val repositorio = RepositorioVacio()
        val viewModel = ProductoViewModel(
            registrarProductoUseCase = RegistrarProductoUseCase(repositorio),
            productoRepository = repositorio
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertIs<ProductoListaEstado.SinProductos>(viewModel.uiState.value.lista)
    }

    @Test
    fun repositorioConProductos_produceFaseConProductos() = runTest(dispatcher) {
        val repositorio = RepositorioConProductos()
        val viewModel = ProductoViewModel(
            registrarProductoUseCase = RegistrarProductoUseCase(repositorio),
            productoRepository = repositorio
        )

        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState.value.lista
        assertIs<ProductoListaEstado.ConProductos>(estado)
        assertEquals(3, estado.productos.size)
    }

    @Test
    fun repositorioConExcepcion_produceFaseError() = runTest(dispatcher) {
        val repositorio = RepositorioConError()
        val viewModel = ProductoViewModel(
            registrarProductoUseCase = RegistrarProductoUseCase(repositorio),
            productoRepository = repositorio
        )

        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState.value.lista
        assertIs<ProductoListaEstado.Error>(estado)
        assertEquals("Error simulado de conexión", estado.mensaje)
    }

    @Test
    fun registroConPrecioCero_dejaErrorEnFormularioSinLlamarRepositorio() = runTest(dispatcher) {
        val repositorio = RepositorioEspiaRegistro()
        val viewModel = ProductoViewModel(
            registrarProductoUseCase = RegistrarProductoUseCase(repositorio),
            productoRepository = repositorio
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onNombreChange("Paracetamol")
        viewModel.onPrecioChange("0")
        viewModel.onStockChange("10")
        viewModel.registrar()

        dispatcher.scheduler.advanceUntilIdle()

        val formulario = viewModel.uiState.value.formulario
        assertEquals("El precio debe ser mayor a 0", formulario.precioError)
        assertTrue(!repositorio.seLlamoRegistrar)
    }
}