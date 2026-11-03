package pe.edu.upeu.pharmamobil.domain.port

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio

interface ColaProductos {
    fun pendientesPorEnviar(maxIntentos: Int, ahora: Long): List<Producto>
    fun contarPendientes(maxIntentos: Int): Int
    fun sumarIntento(id: Long, motivo: String, proximo: Long, confirmacion: Boolean)
    fun marcarFallida(id: Long, motivo: String, maxIntentos: Int)
    fun confirmarEnvio(idLocal: Long, producto: Producto)
    fun reemplazarSincronizados(productos: List<Producto>, instante: Long? = null)
}

interface EnvioProductos {
    suspend fun enviar(producto: Producto): ResultadoEnvio
    suspend fun listar(): List<Producto>
}
