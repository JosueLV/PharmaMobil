package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro

/**
 * Cliente Ktor de PharmaMobil.
 *
 * @param almacen almacen cifrado del que el plugin Auth lee el token JWT.
 *                Es opcional para que las pruebas con MockEngine sigan funcionando.
 * @param esDepuracion si es false, el plugin Logging queda apagado (LogLevel.NONE).
 * @param alExpirarSesion se invoca cuando el servidor responde 401 en una llamada
 *                        que no es el login (token vencido o invalido).
 */
fun crearHttpClient(
    engine: HttpClientEngine,
    almacen: AlmacenSeguro? = null,
    esDepuracion: Boolean = true,
    alExpirarSesion: (() -> Unit)? = null
): HttpClient =
    HttpClient(engine) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        // Adjunta "Authorization: Bearer <token>" a cada peticion.
        // Ninguna llamada escribe la cabecera a mano.
        install(Auth) {
            bearer {
                // Se consulta una sola vez y queda en cache:
                // tras login/logout hay que invocar clearToken() (paso 5).
                loadTokens {
                    almacen?.leerToken()?.let { BearerTokens(it, "") }
                }
                // El token viaja ya en la primera peticion, salvo en el login.
                sendWithoutRequest { peticion ->
                    !peticion.url.encodedPath.endsWith("/auth/login")
                }
            }
        }

        // Avisa a la interfaz del 401 sin consumir la excepcion: el repositorio sigue recibiendola.
        HttpResponseValidator {
            handleResponseExceptionWithRequest { causa, peticion ->
                val e = causa as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                val esLogin = peticion.url.encodedPath.endsWith("/auth/login")
                if (e.response.status == HttpStatusCode.Unauthorized && !esLogin) {
                    alExpirarSesion?.invoke()
                }
            }
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("PHARMA_KTOR_LOG: $message")
                }
            }
            level = if (esDepuracion) LogLevel.HEADERS else LogLevel.NONE
            // El token nunca debe aparecer completo en Logcat.
            sanitizeHeader { nombre -> nombre == HttpHeaders.Authorization }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
        }

        defaultRequest {
            url("http://10.0.2.2:8080/api/v1/")
            contentType(ContentType.Application.Json)
        }
    }
