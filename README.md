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