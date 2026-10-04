package pe.edu.upeu.pharmamobil.domain.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.presentation.producto.Operacion
import pe.edu.upeu.pharmamobil.domain.presentation.producto.ProductoListaEstado
import pe.edu.upeu.pharmamobil.domain.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobil.domain.repository.FakeProductoRepository
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

    private fun crearViewModel(repositorio: FakeProductoRepository) = ProductoViewModel(
        registrarProductoUseCase = RegistrarProductoUseCase(repositorio),
        actualizarProductoUseCase = ActualizarProductoUseCase(repositorio),
        eliminarProductoUseCase = EliminarProductoUseCase(repositorio),
        productoRepository = repositorio
    )

    @Test
    fun repositorioVacio_produceFaseSinProductos() = runTest(dispatcher) {
        val viewModel = crearViewModel(FakeProductoRepository(productosIniciales = emptyList()))

        dispatcher.scheduler.advanceUntilIdle()

        assertIs<ProductoListaEstado.SinProductos>(viewModel.uiState.value.lista)
    }

    @Test
    fun repositorioConProductos_produceFaseConProductos() = runTest(dispatcher) {
        val repositorio = FakeProductoRepository(
            productosIniciales = listOf(
                Producto(id = 1, nombre = "Paracetamol", precio = 15.50, stock = 100),
                Producto(id = 2, nombre = "Ibuprofeno", precio = 18.90, stock = 50),
                Producto(id = 3, nombre = "Amoxicilina", precio = 25.00, stock = 5)
            )
        )
        val viewModel = crearViewModel(repositorio)

        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState.value.lista
        assertIs<ProductoListaEstado.ConProductos>(estado)
        assertEquals(3, estado.productos.size)
    }

    @Test
    fun repositorioConExcepcion_produceFaseError() = runTest(dispatcher) {
        val repositorio = FakeProductoRepository(
            errorAlListar = Exception("Error simulado de conexión")
        )
        val viewModel = crearViewModel(repositorio)

        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState.value.lista
        assertIs<ProductoListaEstado.Error>(estado)
        assertEquals("Error simulado de conexión", estado.mensaje)
    }

    @Test
    fun registroConPrecioCero_dejaErrorEnFormularioSinLlamarRepositorio() = runTest(dispatcher) {
        val repositorio = FakeProductoRepository()
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

    @Test
    fun errorDeValidacionDelServidor_dejaMensajesEnCamposSinCambiarFaseAError() = runTest(dispatcher) {
        val repositorio = FakeProductoRepository(
            errorAlRegistrar = ErrorApi.Validacion(
                porCampo = mapOf("nombre" to "El nombre debe tener entre 3 y 150 caracteres")
            )
        )
        val viewModel = crearViewModel(repositorio)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onNombreChange("pa")
        viewModel.onPrecioChange("10")
        viewModel.onStockChange("5")
        viewModel.registrar()

        dispatcher.scheduler.advanceUntilIdle()

        val formulario = viewModel.uiState.value.formulario
        assertEquals(
            "El nombre debe tener entre 3 y 150 caracteres",
            formulario.nombreError
        )
        // La fase de la LISTA no debe cambiar a Error por un fallo del formulario.
        assertIs<ProductoListaEstado.SinProductos>(viewModel.uiState.value.lista)
    }

    @Test
    fun eliminacion_pasaPorEnCursoYTerminaInactivaConListaRecargada() = runTest(dispatcher) {
        val puerta = CompletableDeferred<Unit>()
        val repositorio = FakeProductoRepository(
            productosIniciales = listOf(
                Producto(id = 1, nombre = "Paracetamol", precio = 15.50, stock = 100)
            ),
            puertaEliminar = puerta
        )
        val viewModel = crearViewModel(repositorio)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.eliminar(1)
        dispatcher.scheduler.advanceUntilIdle()

        // Mientras la "puerta" no se libera, eliminar() sigue en curso.
        val operacionEnCurso = viewModel.uiState.value.operacion
        assertIs<Operacion.EnCurso>(operacionEnCurso)
        assertEquals(Operacion.Tipo.Eliminar, operacionEnCurso.tipo)

        puerta.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()

        assertIs<Operacion.Inactiva>(viewModel.uiState.value.operacion)
        assertIs<ProductoListaEstado.SinProductos>(viewModel.uiState.value.lista)
        assertTrue(repositorio.seLlamoEliminar)
    }
}