package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.data.mapper.toDomain
import pe.edu.upeu.pharmamobil.data.mapper.toRequest
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobil.domain.port.EnvioProductos

class EnvioProductosRest(private val api: ProductoApi, private val categoria: Long) : EnvioProductos {
    override suspend fun enviar(producto: Producto): ResultadoEnvio {
        return try {
            ResultadoEnvio.Exito(ejecutarLlamada {
                api.crear(producto.toRequest(categoria)).toDomain()
            }.getOrThrow())
        } catch (e: ErrorApiException) {
            e.error.aResultadoEnvio()
        }
    }

    override suspend fun listar(): List<Producto> = ejecutarLlamada {
        val productos = mutableListOf<Producto>()
        var pagina = 0
        do {
            val respuesta = api.listar(pagina = pagina++, tamanio = 1000)
            productos.addAll(respuesta.contenido.map { it.toDomain() })
        } while (!respuesta.ultima)
        productos
    }.getOrThrow()
}

internal fun ErrorApi.aResultadoEnvio(): ResultadoEnvio = when (this) {
    ErrorApi.SinConexion -> ResultadoEnvio.Transitorio("Sin conexión; verifica el servidor antes de reenviar", this)
    ErrorApi.TiempoAgotado -> ResultadoEnvio.Transitorio("Tiempo agotado; el servidor podría haber recibido el producto", this)
    ErrorApi.Servidor -> ResultadoEnvio.Transitorio("Servidor no disponible; revisa si recibió el producto", this)
    is ErrorApi.Validacion -> ResultadoEnvio.Permanente("Revisa los campos: " + porCampo.keys.joinToString(), this)
    ErrorApi.NoEncontrado -> ResultadoEnvio.Permanente("El recurso ya no existe", this)
    is ErrorApi.Conflicto -> ResultadoEnvio.Permanente("Conflicto: revisa el producto en el servidor", this)
    ErrorApi.SinPermiso -> ResultadoEnvio.Permanente("Tu usuario no tiene permiso para enviar", this)
    is ErrorApi.NoAutenticado -> ResultadoEnvio.Permanente("Sesión vencida: inicia sesión nuevamente", this)
}
