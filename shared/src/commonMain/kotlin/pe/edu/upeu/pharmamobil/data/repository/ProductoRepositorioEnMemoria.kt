package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random
import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResultadoProductos
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioEnMemoria(
    private val api: ProductoApi
) : ProductoRepository {

    private val productos = mutableListOf(
        Producto(id = 1, nombre = "Paracetamol", precio = 15.50, stock = 100, activo = true),
        Producto(id = 2, nombre = "Ibuprofeno", precio = 18.90, stock = 50, activo = true),
        Producto(id = 3, nombre = "Amoxicilina", precio = 25.00, stock = 5, activo = true),
        Producto(id = 4, nombre = "Loratadina", precio = 12.50, stock = 0, activo = false),
        Producto(id = 5, nombre = "Diclofenaco", precio = 20.00, stock = 3, activo = true)
    )

    private var proximoId = (productos.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun registrar(nombre: String, precio: Double, stock: Int): Producto {
        delay(Random.nextLong(300, 801))
        val producto = Producto(
            id = proximoId++,
            nombre = nombre,
            precio = precio,
            stock = stock
        )
        productos.add(producto)
        return producto
    }

    // Antes lanzaba una excepción simulada; ahora consume la API real vía Ktor.
    override suspend fun listar(): List<Producto> =
        api.obtenerProductosRemotos().map { it.toDomain() }

    // Métodos de sesiones anteriores (se conservan para no romper ProductoRepositoryTest)
    suspend fun obtenerProductos(): List<Producto> {
        delay(1000)
        return productos.toList()
    }

    fun observarEstados(): Flow<String> = flow {
        emit("Iniciando")
        delay(1000)
        emit("Finalizado")
    }

    fun observarProductos(): Flow<List<Producto>> = flow {
        emit(emptyList())
        delay(1000)
        emit(productos.toList())
        delay(1000)
        val productosConStockActualizado = productos.map { producto ->
            producto.copy(stock = producto.stock - 5)
        }
        emit(productosConStockActualizado)
    }

    fun cargarProductos(): Flow<ResultadoProductos> = flow {
        emit(ResultadoProductos.Cargando)
        delay(1000)
        try {
            val lista = obtenerProductos()
            emit(ResultadoProductos.Exito(lista))
        } catch (e: Exception) {
            emit(ResultadoProductos.Error(e.message ?: "Error desconocido"))
        }
    }
}