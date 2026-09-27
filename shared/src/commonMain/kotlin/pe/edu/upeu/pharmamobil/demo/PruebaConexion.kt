package pe.edu.upeu.pharmamobil.demo

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi

fun probarProductoInexistente(api: ProductoApi) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            val producto = api.obtenerProductoPorId(999999)
            println("PHARMA_TEST_404: respuesta inesperada -> $producto")
        } catch (e: ClientRequestException) {
            println("PHARMA_TEST_404: excepcion capturada -> codigo=${e.response.status} mensaje=${e.message}")
        } catch (e: Exception) {
            println("PHARMA_TEST_404: otra excepcion -> ${e::class.simpleName}: ${e.message}")
        }
    }
}

fun probarTimeout(api: ProductoApi) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            val productos = api.obtenerProductosRemotos()
            println("PHARMA_TEST_TIMEOUT: respuesta inesperada -> ${productos.size} productos")
        } catch (e: HttpRequestTimeoutException) {
            println("PHARMA_TEST_TIMEOUT: excepcion capturada -> ${e.message}")
        } catch (e: Exception) {
            println("PHARMA_TEST_TIMEOUT: otra excepcion -> ${e::class.simpleName}: ${e.message}")
        }
    }
}