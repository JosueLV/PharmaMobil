package pe.edu.upeu.pharmamobil.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoLocalTest {

    @Test
    fun `la bajada del servidor no borra lo pendiente`() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        local.guardarPendiente(Producto(id = 0, nombre = "Ibuprofeno", precio = 4.5, stock = 20))

        local.reemplazarSincronizados(
            listOf(Producto(id = 1, nombre = "Paracetamol", precio = 2.0, stock = 50))
        )

        val productos = local.observar().first()
        assertEquals(2, productos.size)
        assertTrue(productos.any { it.nombre == "Ibuprofeno" && it.id < 0 })
        assertEquals(1, local.pendientes().size)
    }

    @Test
    fun `un id local y uno del servidor conviven sin sobrescribirse`() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        val guardado = local.guardarPendiente(
            Producto(id = 0, nombre = "Ibuprofeno", precio = 4.5, stock = 20)
        )

        local.reemplazarSincronizados(
            listOf(
                Producto(id = 1, nombre = "Paracetamol", precio = 2.0, stock = 50),
                Producto(id = 2, nombre = "Amoxicilina", precio = 8.0, stock = 30)
            )
        )

        val ids = local.observar().first().map { it.id }.toSet()
        assertTrue(guardado.id < 0)
        assertEquals(setOf(guardado.id, 1L, 2L), ids)
    }
}

