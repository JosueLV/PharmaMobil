package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResultadoProductos

class ProductoRepository {

    // Paso 3: fuente temporal de productos simulados
    private val productosSimulados = listOf(
        Producto(id = 1, nombre = "Paracetamol", precio = 8.50, stock = 100),
        Producto(id = 2, nombre = "Ibuprofeno", precio = 12.00, stock = 50),
        Producto(id = 3, nombre = "Amoxicilina", precio = 18.50, stock = 20)
    )

    // Paso 4 y 5: función suspend que simula una espera de red o de base de datos
    suspend fun obtenerProductos(): List<Producto> {
        delay(1000)
        return productosSimulados
    }

    // Paso 9, 10 y 11: Flow simple que emite dos estados de texto
    fun observarEstados(): Flow<String> = flow {
        emit("Iniciando")
        delay(1000)
        emit("Finalizado")
    }

    // Paso 12, 13 y 14: Flow que emite cambios en la lista de productos usando copy()
    fun observarProductos(): Flow<List<Producto>> = flow {
        emit(emptyList())
        delay(1000)
        emit(productosSimulados)
        delay(1000)
        val productosConStockActualizado = productosSimulados.map { producto ->
            producto.copy(stock = producto.stock - 5)
        }
        emit(productosConStockActualizado)
    }

    // Paso 15, 16 y 17: Flow integrado que combina Flow con la sealed class de estados
    fun cargarProductos(): Flow<ResultadoProductos> = flow {
        emit(ResultadoProductos.Cargando)
        delay(1000)
        try {
            val productos = obtenerProductos()
            emit(ResultadoProductos.Exito(productos))
        } catch (e: Exception) {
            emit(ResultadoProductos.Error(e.message ?: "Error desconocido"))
        }
    }
}