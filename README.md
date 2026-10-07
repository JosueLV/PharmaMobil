# PharmaMobil — Consumo REST con Ktor

## URL base

`https://api.escuelajs.co/api/v1/`

## Endpoint consumido

`GET products` (con parámetro de consulta `limit`)

Ejemplo: `https://api.escuelajs.co/api/v1/products?limit=10`

## Campos del DTO (`ProductoDto`)

| Campo | Tipo | Obligatorio |
|---|---|---|
| `id` | `Int` | Sí |
| `title` | `String` | Sí |
| `price` | `Double` | Sí |
| `description` | `String` | No (default `""`) |
| `images` | `List<String>` | No (default lista vacía) |
| `category` | `CategoriaDto?` | No |

`CategoriaDto`: `id: Int`, `name: String`.

## Conectividad REST

- **URL base:** `https://api.escuelajs.co/api/v1/`
- **Endpoints implementados:** `GET /products`, `GET /products/{id}`
- **Manejo de errores:** excepciones de red, timeout y deserialización se capturan en `ProductoRepositorioEnMemoria` y se exponen como `ProductoListaEstado.Error` sin cerrar la aplicación.

## Capacidades nativas

Sesión 9: capacidades dependientes de la plataforma, resueltas con expect/actual e interfaz + inyección. La interfaz Compose, el dominio y el texto a compartir viven en `commonMain`.

| Capacidad | Mecanismo | commonMain | androidMain | iosMain |
|---|---|---|---|---|
| Formato de moneda (soles) | expect/actual | `platform/Formato.kt` | `platform/Formato.android.kt` (`NumberFormat`) | `platform/Formato.ios.kt` (`NSNumberFormatter`) |
| Compartir producto | Interfaz + Koin | `domain/platform/Compartidor.kt`, `domain/usecase/TextoParaCompartir.kt` | `platform/CompartidorAndroid.kt` (`Intent.ACTION_SEND`) | `platform/CompartidorIos.kt` (`UIActivityViewController`) |
| Registro en Koin | `expect val platformModule` | `di/AppModule.kt` | `di/PlatformModule.android.kt` | `di/PlatformModule.ios.kt` |

- El formato se aplica en la capa de presentación (`ProductoUi.toUi()`), no en el dominio ni en el composable.
- `DetalleProductoViewModel` recibe `Compartidor` por constructor; la pantalla no conoce la implementación.
- Ninguna clase de `presentation` importa `android.*` ni `platform.UIKit`.
- En iOS, Koin se inicia desde `iOSApp.swift` con `KoinIosKt.doInitKoinIos()`.
- Las pruebas se ejecutaron en Android (`./gradlew :shared:testAndroidHostTest`, 11/11). El target iOS se compila en macOS con Xcode.