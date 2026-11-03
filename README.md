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

## Manejo de errores

Las excepciones de Ktor se traducen a un único tipo de dominio (`ErrorApi`) en `EjecutarLlamada.kt`, de modo que la capa de presentación nunca conoce clases de `io.ktor`.

| ErrorApi | Código HTTP | Causa |
|---|---|---|
| `Validacion` | 400 | Campos inválidos (nombre, precio, stock) según las reglas de PharmaSoft |
| `NoEncontrado` | 404 | El recurso solicitado no existe |
| `Conflicto` | 409 | Regla de negocio violada (nombre duplicado, producto ya inactivo) |
| `Servidor` | 5xx | Error interno del backend |
| `SinConexion` | — | Sin red o backend apagado (`IOException`) |
| `TiempoAgotado` | — | Se superó `requestTimeoutMillis` |

La cancelación de corrutinas (`CancellationException`) se relanza explícitamente antes de los demás `catch`, para no interferir con el ciclo de vida de las operaciones en curso.

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

## Código específico de plataforma

Inventario de capacidades que dependen del sistema operativo. Las rutas son relativas a `shared/src/<source set>/kotlin/pe/edu/upeu/pharmamobil/`.

| Capacidad | commonMain | androidMain | iosMain |
|---|---|---|---|
| Plataforma (plantilla) | `Platform.kt` (expect fun getPlatform) | `Platform.android.kt` (Build) | `Platform.ios.kt` (UIDevice) |
| Formato de moneda | `platform/Formato.kt` (expect fun) | `platform/Formato.android.kt` (NumberFormat) | `platform/Formato.ios.kt` (NSNumberFormatter) |
| Módulo de inyección | `di/AppModule.kt` (expect val platformModule) | `di/PlatformModule.android.kt` | `di/PlatformModule.ios.kt` |
| Compartir producto | `domain/platform/Compartidor.kt` (interface) | `platform/CompartidorAndroid.kt` (Intent) | `platform/CompartidorIos.kt` (UIActivityViewController) |
| Información del dispositivo | `platform/InfoDispositivo.kt` (expect class) | `platform/InfoDispositivo.android.kt` | `platform/InfoDispositivo.ios.kt` |

Regla de aislamiento: `commonMain` no importa `android.*` ni `platform.*` (Apple). Se comprueba con:

    Get-ChildItem shared\src\commonMain -Recurse -Filter *.kt | Select-String "^import (android|platform)\."

## Autenticación

Sesión 10: PharmaMobil inicia sesión contra PharmaSoft con un token JWT, lo guarda cifrado en cada plataforma, lo adjunta solo a cada petición y distingue una sesión expirada (401) de una falta de permisos (403).

### Flujo

1. `POST /api/v1/auth/login` (ruta pública) devuelve el `token`, el `tipo` (Bearer), `expiraEnSegundos` (3600), los roles y los permisos.
2. El token se guarda con `AlmacenSeguro` (cifrado). Ninguna llamada escribe la cabecera `Authorization` a mano: la adjunta el plugin Auth de Ktor.
3. Al abrir la app se consulta `GET /api/v1/auth/me` con el token guardado. Si responde 200 se reconstruye la sesión y se entra directo al listado; si responde 401 se borra el token y se muestra el login.
4. PharmaSoft no renueva el token: al expirar hay que repetir el login.

### Usuarios de prueba

Cargados por Flyway en PharmaSoft (solo para desarrollo).

| Usuario | Contraseña | Rol | Permisos |
|---|---|---|---|
| `admin` | `Admin123` | ADMIN | Los ocho permisos del sistema |
| `vendedor` | `Vendedor123` | VENDEDOR | Catálogo solo lectura; clientes y ventas completos |
| `almacen` | `Almacen123` | ALMACENERO | Solo catálogo: leer y escribir |

### Almacén seguro del token

| Plataforma | Archivo | Mecanismo |
|---|---|---|
| commonMain | `data/local/AlmacenSeguro.kt` | Interfaz: `guardarToken`, `leerToken`, `borrarToken` |
| androidMain | `data/local/AlmacenSeguroAndroid.kt` | `EncryptedSharedPreferences`, llave AES256_GCM custodiada por el Android Keystore |
| iosMain | `data/local/AlmacenSeguroIos.kt` | Keychain del sistema (`platform.Security`) |

Es una interfaz inyectada y no un `expect class` porque la implementación de Android recibe un `Context` y la de iOS no recibe nada. Se registra en el `platformModule` de cada plataforma.

### Plugins de Ktor

Configurados una sola vez en `data/remote/HttpClientFactory.kt`:

- **Auth (bearer):** `loadTokens` lee el token del almacén. `sendWithoutRequest` devuelve `true` para todas las rutas salvo `/auth/login`, así el token viaja desde la primera petición y se evita el ciclo enviar sin token, recibir 401 y reintentar. Tras iniciar o cerrar sesión se llama a `clearToken()` del proveedor, porque `loadTokens` guarda su resultado en caché.
- **Logging:** nivel `HEADERS` en depuración y `NONE` fuera de ella (constante `ES_DEPURACION` en `di/AppModule.kt`). La cabecera `Authorization` se sanea con `sanitizeHeader` y en Logcat aparece como `Authorization: ***`.
- **Aviso de sesión expirada:** un `HttpResponseValidator` avisa a la interfaz cuando el servidor responde 401 en una llamada que no es el login.

### Manejo de 401 y 403

| Situación | Resultado en el dominio | Comportamiento en la app |
|---|---|---|
| 401 en el login | `ErrorSesion.CredencialesInvalidas` | Se queda en el login: «Usuario o contraseña incorrectos» |
| 401 en cualquier otra llamada | `ErrorSesion.SesionExpirada` | Borra el token, cierra la sesión y vuelve al login con el aviso «Tu sesión expiró. Inicia sesión nuevamente» |
| 403 | `ErrorSesion.SinPermiso` | Mantiene la sesión y muestra «No tienes permisos para realizar esta operación» |
| Sin red o backend apagado | `ErrorSesion.SinConexion` | Muestra «No hay conexión con el servidor» |

La traducción de los códigos del servidor se hace en un solo lugar (`data/remote/EjecutarLlamada.kt` y `data/remote/MapeoErrorSesion.kt`), y la capa de presentación no conoce clases de `io.ktor`.

### Permisos en la interfaz

La interfaz usa `sesion.puede("PERMISO")` para ofrecer solo lo que el usuario puede hacer:

- La sección **Clientes** del menú aparece solo con `CLIENTES_LEER`.
- El formulario de **registrar producto** y los botones de **editar y eliminar** aparecen solo con `CATALOGO_ESCRIBIR`.
- El menú muestra el nombre y el usuario de la sesión y la opción **Cerrar sesión**, que borra el token y vuelve al login.

### Estructura

| Capa | Archivos |
|---|---|
| Dominio | `domain/model/Sesion.kt`, `domain/model/ErrorSesion.kt`, `domain/model/SesionEventos.kt`, `domain/repository/AuthRepository.kt`, `domain/usecase/IniciarSesionUseCase.kt`, `domain/usecase/CerrarSesionUseCase.kt` |
| Datos | `data/repository/AuthRepositorioRest.kt`, `data/remote/dto/LoginDto.kt`, `data/mapper/LoginMapper.kt`, `data/remote/MapeoErrorSesion.kt` |
| Presentación | `domain/presentation/sesion/SesionViewModel.kt`, `SesionUiState.kt`, `LoginScreen.kt` |

### Verificación

Probado en Android (emulador, backend en `http://10.0.2.2:8080`) con los tres usuarios de prueba:

- El login con `admin` carga el listado de productos y permite registrar uno nuevo con el token.
- Al cerrar la aplicación por completo y volver a abrirla, la sesión sigue activa.
- Con `almacen` el menú no ofrece Clientes y el formulario de registro está visible; con `vendedor` el catálogo es de solo lectura y Clientes sí aparece.
- Un intento de modificar un producto sin permiso de escritura recibe 403, muestra el aviso y mantiene la sesión.
- Con un token vencido, el 401 cierra la sesión y lleva al login con el aviso.
- Una contraseña incorrecta muestra «Usuario o contraseña incorrectos».

El target iOS se compila en macOS con Xcode. `AlmacenSeguroIos` está implementado, pero no se pudo compilar ni ejecutar en el equipo de desarrollo (Windows).