package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Producto

interface ProductoRepository {
    /** La pantalla se suscribe una vez y se actualiza sola. */
    fun observar(): Flow<List<Producto>>

    fun observarUltimaSincronizacion(): Flow<Long?>

    /** Sube lo pendiente y luego trae la lista del servidor. */
    suspend fun sincronizar()

    /** Reenvío solicitado explícitamente tras revisar el posible envío anterior. */
    suspend fun reintentar(id: Long)

    suspend fun listar(): List<Producto>
    suspend fun obtener(id: Long): Producto
    suspend fun registrar(producto: Producto): Producto
    suspend fun actualizar(producto: Producto): Producto
    suspend fun eliminar(id: Long)
}
