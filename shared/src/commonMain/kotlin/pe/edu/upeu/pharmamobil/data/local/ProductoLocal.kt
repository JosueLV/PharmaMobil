package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.domain.port.ColaProductos
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.db.Producto as FilaProducto

class ProductoLocal(private val base: PharmaDb) : ColaProductos {

    private val consultas = base.productoQueries

    fun observarUltimaSincronizacion(): Flow<Long?> =
        base.sincronizacionQueries.ultimaCorrecta().asFlow()
            .mapToOneOrNull(Dispatchers.Default)

    fun obtener(id: Long): Producto? =
        consultas.buscarPorId(id).executeAsOneOrNull()?.aDominio()

    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.aDominio() } }

    fun pendientes(): List<Producto> =
        consultas.listarPendientes().executeAsList()
            .map { it.aDominio() }

    override fun pendientesPorEnviar(maxIntentos: Int, ahora: Long): List<Producto> =
        consultas.pendientesPorEnviar(maxIntentos.toLong(), ahora).executeAsList().map { it.aDominio() }

    fun observarPendientes(maxIntentos: Int): Flow<Long> =
        consultas.contarPendientes(maxIntentos.toLong()).asFlow().mapToOne(Dispatchers.Default)

    fun observarFallidas(maxIntentos: Int): Flow<Long> =
        consultas.contarFallidas(maxIntentos.toLong()).asFlow().mapToOne(Dispatchers.Default)

    override fun contarPendientes(maxIntentos: Int): Int =
        consultas.contarPendientes(maxIntentos.toLong()).executeAsOne().toInt()

    override fun sumarIntento(id: Long, motivo: String, proximo: Long, confirmacion: Boolean) {
        consultas.sumarIntento(motivo, proximo, if (confirmacion) 1L else 0L, id)
    }

    override fun marcarFallida(id: Long, motivo: String, maxIntentos: Int) {
        consultas.marcarFallida(maxIntentos.toLong(), motivo, id)
    }

    fun autorizarReintento(id: Long, maxIntentos: Int, ahora: Long) {
        consultas.autorizarReintento(maxIntentos.toLong(), id, ahora)
    }

    fun actualizarPendiente(producto: Producto): Producto {
        consultas.actualizarPendiente(producto.nombre, producto.precio, producto.stock.toLong(),
            if (producto.activo) 1L else 0L, producto.id)
        return requireNotNull(obtener(producto.id))
    }

    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val local = producto.copy(id = minOf(menor, 0L) - 1L, pendiente = true)
        consultas.insertar(
            local.id, local.nombre, local.precio,
            local.stock.toLong(), 1L, if (local.activo) 1L else 0L
        )
        return local
    }

    override fun reemplazarSincronizados(productos: List<Producto>, instante: Long?) {
        base.transaction {
            consultas.borrarSincronizados()
            productos.forEach {
                consultas.insertar(
                    it.id, it.nombre, it.precio,
                    it.stock.toLong(), 0L, if (it.activo) 1L else 0L
                )
            }
            instante?.let { base.sincronizacionQueries.guardarUltimaCorrecta(it) }
        }
    }

    /** Conserva la respuesta del POST aunque la descarga siguiente falle. */
    override fun confirmarEnvio(idLocal: Long, producto: Producto) {
        consultas.transaction {
            consultas.borrarPorId(idLocal)
            consultas.insertar(
                producto.id, producto.nombre, producto.precio,
                producto.stock.toLong(), 0L, if (producto.activo) 1L else 0L
            )
        }
    }
    fun eliminar(id: Long) {
        consultas.borrarPorId(id)
    }
}

private fun FilaProducto.aDominio() = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock.toInt(),
    activo = activo == 1L,
    pendiente = pendiente == 1L,
    intentos = intentos.toInt(),
    ultimoError = ultimoError,
    proximoIntento = proximoIntento,
    requiereConfirmacion = requiereConfirmacion == 1L
)
