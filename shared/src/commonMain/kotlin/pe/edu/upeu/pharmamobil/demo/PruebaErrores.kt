package pe.edu.upeu.pharmamobil.demo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobil.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException

private fun logResultado(tag: String, resultado: Result<*>) {
    resultado
        .onSuccess { println("$tag: EXITO inesperado -> $it") }
        .onFailure { fallo ->
            if (fallo is ErrorApiException) {
                println("$tag: ErrorApi=${fallo.error::class.simpleName} detalle=${fallo.error}")
            } else {
                println("$tag: excepcion no tipificada -> ${fallo::class.simpleName}: ${fallo.message}")
            }
        }
}

// Escenario 2: precio invalido, saltando la validacion local del use case
fun probarPrecioInvalido(api: ProductoApi) {
    CoroutineScope(Dispatchers.Default).launch {
        val resultado = ejecutarLlamada {
            api.crear(
                ProductoRequestDto(
                    nombre = "Producto precio invalido",
                    precio = 0.0,
                    stock = 10,
                    estado = true,
                    categoriaId = 1
                )
            )
        }
        logResultado("PHARMA_TEST_PRECIO", resultado)
    }
}

// Escenario 3: actualizar un id que no existe
fun probarRecursoInexistente(api: ProductoApi) {
    CoroutineScope(Dispatchers.Default).launch {
        val resultado = ejecutarLlamada {
            api.actualizar(
                id = 999999,
                request = ProductoRequestDto(
                    nombre = "No deberia existir",
                    precio = 10.0,
                    stock = 5,
                    estado = true,
                    categoriaId = 1
                )
            )
        }
        logResultado("PHARMA_TEST_404", resultado)
    }
}

// Escenario 4: doble eliminacion sobre el mismo id
fun probarDobleEliminacion(api: ProductoApi, id: Long) {
    CoroutineScope(Dispatchers.Default).launch {
        val primera = ejecutarLlamada { api.eliminar(id) }
        logResultado("PHARMA_TEST_DELETE_1", primera)

        val segunda = ejecutarLlamada { api.eliminar(id) }
        logResultado("PHARMA_TEST_DELETE_2", segunda)
    }
}

// Escenario 5: nombre duplicado (regla de negocio)
fun probarNombreDuplicado(api: ProductoApi, nombreExistente: String) {
    CoroutineScope(Dispatchers.Default).launch {
        val resultado = ejecutarLlamada {
            api.crear(
                ProductoRequestDto(
                    nombre = nombreExistente,
                    precio = 15.0,
                    stock = 10,
                    estado = true,
                    categoriaId = 1
                )
            )
        }
        logResultado("PHARMA_TEST_DUPLICADO", resultado)
    }
}

// Escenario 7: timeout agotado (requiere bajar requestTimeoutMillis temporalmente)
fun probarTimeout(api: ProductoApi) {
    CoroutineScope(Dispatchers.Default).launch {
        val resultado = ejecutarLlamada { api.listar() }
        logResultado("PHARMA_TEST_TIMEOUT", resultado)
    }
}
fun probarObtener(api: ProductoApi, id: Long) {
    CoroutineScope(Dispatchers.Default).launch {
        val resultado = ejecutarLlamada { api.obtener(id) }
        logResultado("PHARMA_TEST_OBTENER", resultado)
    }
}