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
