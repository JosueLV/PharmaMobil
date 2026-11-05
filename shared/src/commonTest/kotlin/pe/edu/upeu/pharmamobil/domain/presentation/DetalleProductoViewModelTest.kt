package pe.edu.upeu.pharmamobil.domain.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.domain.presentation.producto.DetalleEstado
import pe.edu.upeu.pharmamobil.domain.presentation.producto.DetalleProductoViewModel
import pe.edu.upeu.pharmamobil.domain.repository.FakeProductoRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleProductoViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun configurar() { Dispatchers.setMain(dispatcher) }

    @AfterTest
    fun limpiar() { Dispatchers.resetMain() }

    @Test
    fun detalleDisponibleSinRed_paraPendienteYProductoSincronizado() = runTest(dispatcher) {
        val productos = listOf(
            Producto(id = -7, nombre = "Pendiente", precio = 5.0, stock = 10, pendiente = true),
            Producto(id = 42, nombre = "Sincronizado", precio = 8.0, stock = 12)
        )
        val repositorio = FakeProductoRepository(
            productosIniciales = productos,
            errorAlListar = ErrorApiException(ErrorApi.SinConexion)
        )
        val viewModel = DetalleProductoViewModel(repositorio, CompartidorFalso())

        for (producto in productos) {
            viewModel.cargar(producto.id)
            dispatcher.scheduler.advanceUntilIdle()
            val estado = assertIs<DetalleEstado.Listo>(viewModel.estado.value)
            assertEquals(producto.id, estado.producto.id)
            assertEquals(producto.pendiente, estado.producto.pendiente)
        }
        assertEquals(0, repositorio.vecesQueSeListo)
    }

    @Test
    fun errorAlCambiarDeProducto_noComparteElProductoAnterior() = runTest(dispatcher) {
        val repositorio = FakeProductoRepository(
            productosIniciales = listOf(Producto(id = 1, nombre = "Anterior", precio = 5.0, stock = 10))
        )
        val compartidor = CompartidorFalso()
        val viewModel = DetalleProductoViewModel(repositorio, compartidor)
        viewModel.cargar(1)
        dispatcher.scheduler.advanceUntilIdle()
        assertIs<DetalleEstado.Listo>(viewModel.estado.value)

        viewModel.cargar(99)
        dispatcher.scheduler.advanceUntilIdle()
        assertIs<DetalleEstado.Error>(viewModel.estado.value)
        viewModel.compartir()
        assertTrue(compartidor.textos.isEmpty())
    }

    private class CompartidorFalso : Compartidor {
        val textos = mutableListOf<String>()
        override fun compartir(texto: String) { textos.add(texto) }
    }
}
