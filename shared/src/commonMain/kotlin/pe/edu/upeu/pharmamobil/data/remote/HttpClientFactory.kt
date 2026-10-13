package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
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
 */
fun crearHttpClient(
    engine: HttpClientEngine,
    almacen: AlmacenSeguro? = null,
    esDepuracion: Boolean = true
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
