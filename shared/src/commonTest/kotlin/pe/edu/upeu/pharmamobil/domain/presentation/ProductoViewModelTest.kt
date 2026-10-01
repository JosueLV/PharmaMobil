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
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
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

    private fun crearViewModel(repositorio: ProductoRepository) = ProductoViewModel(
        registrarProductoUseCase = RegistrarProductoUseCase(repositorio),
        actualizarProductoUseCase = ActualizarProductoUseCase(repositorio),
        eliminarProductoUseCase = EliminarProductoUseCase(repositorio),
        productoRepository = repositorio
    )

    private class RepositorioVacio : ProductoRepository {
        override suspend fun listar(): List<Producto> = emptyList()
        override suspend fun obtener(id: Long): Producto =
            error("No debería llamarse obtener() en esta prueba")
        override suspend fun registrar(producto: Producto): Producto =
            error("No debería llamarse registrar() en esta prueba")
        override suspend fun actualizar(producto: Producto): Producto =
            error("No debería llamarse actualizar() en esta prueba")
        override suspend fun eliminar(id: Long) =
            error("No debería llamarse eliminar() en esta prueba")
    }


    private class RepositorioConProductos : ProductoRepository {
        override suspend fun listar(): List<Producto> = listOf(
            Producto(id = 1, nombre = "Paracetamol", precio = 15.50, stock = 100),
            Producto(id = 2, nombre = "Ibuprofeno", precio = 18.90, stock = 50),
            Producto(id = 3, nombre = "Amoxicilina", precio = 25.00, stock = 5)
        )
        override suspend fun obtener(id: Long): Producto =
            error("No debería llamarse obtener() en esta prueba")
        override suspend fun registrar(producto: Producto): Producto =
            error("No debería llamarse registrar() en esta prueba")
        override suspend fun actualizar(producto: Producto): Producto =
            error("No debería llamarse actualizar() en esta prueba")
        override suspend fun eliminar(id: Long) =
            error("No debería llamarse eliminar() en esta prueba")
    }


    private class RepositorioConError : ProductoRepository {
        override suspend fun listar(): List<Producto> {
            throw Exception("Error simulado de conexión")
        }
        override suspend fun obtener(id: Long): Producto =
            error("No debería llamarse obtener() en esta prueba")
        override suspend fun registrar(producto: Producto): Producto =
            error("No debería llamarse registrar() en esta prueba")
        override suspend fun actualizar(producto: Producto): Producto =
            error("No debería llamarse actualizar() en esta prueba")
        override suspend fun eliminar(id: Long) =
            error("No debería llamarse eliminar() en esta prueba")
    }


    private class RepositorioEspiaRegistro : ProductoRepository {
        var seLlamoRegistrar: Boolean = false

        override suspend fun listar(): List<Producto> = emptyList()
        override suspend fun obtener(id: Long): Producto =
            error("No debería llamarse obtener() en esta prueba")
        override suspend fun registrar(producto: Producto): Producto {
            seLlamoRegistrar = true
            return producto.copy(id = 99)
        }
        override suspend fun actualizar(producto: Producto): Producto =
            error("No debería llamarse actualizar() en esta prueba")
        override suspend fun eliminar(id: Long) =
            error("No debería llamarse eliminar() en esta prueba")
    }

    @Test
    fun repositorioVacio_produceFaseSinProductos() = runTest(dispatcher) {
        val viewModel = crearViewModel(RepositorioVacio())

        dispatcher.scheduler.advanceUntilIdle()

        assertIs<ProductoListaEstado.SinProductos>(viewModel.uiState.value.lista)
    }

    @Test
    fun repositorioConProductos_produceFaseConProductos() = runTest(dispatcher) {
        val viewModel = crearViewModel(RepositorioConProductos())

        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState.value.lista
        assertIs<ProductoListaEstado.ConProductos>(estado)
        assertEquals(3, estado.productos.size)
    }

    @Test
    fun repositorioConExcepcion_produceFaseError() = runTest(dispatcher) {
        val viewModel = crearViewModel(RepositorioConError())

        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState.value.lista
        assertIs<ProductoListaEstado.Error>(estado)
        assertEquals("Error simulado de conexión", estado.mensaje)
    }

    @Test
    fun registroConPrecioCero_dejaErrorEnFormularioSinLlamarRepositorio() = runTest(dispatcher) {
        val repositorio = RepositorioEspiaRegistro()
        val viewModel = crearViewModel(repositorio)
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