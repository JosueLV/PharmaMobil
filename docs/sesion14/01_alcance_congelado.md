# Paso 1. Alcance congelado - 10/11/2026

Base: sesión 13, commit 5d11011971ec47ee218e7d9519318325c109dbcf.
Rama: feature/builds-14, creada desde develop e integrada con la base de sesión 13.
La actualización de develop queda sujeta a aprobación del propietario; no se ha fusionado allí.

## Decisión sobre los huecos de sesión 13

| N.º | Hueco | Clasificación | Motivo / evidencia |
| --- | --- | --- | --- |
| 1 | Historial de intentos y error por fila | Cerrado | Esquema v3 y pruebas de sesión 13. |
| 2 | Reenvío automático de POST incierto | Cerrado con límite | Confirmación manual y espera. No hay garantía general de idempotencia en el servidor. |
| 3 | Contadores y revisión | Cerrado | Capturas de pendientes 1 → 0 y fallidas 0. |
| 4 | Paginación inválida | Cerrado | Tamaño 100; sincronización observada. |
| 5 | Permiso nativo | Cerrado en Android | Cámara concedida, denegada y bloqueada. |
| 6 | Telemetría | Cerrado en Android | Evento no fatal y claves recibidos en Crashlytics. |
| 7 | Detalle offline | Cerrado | Consulta local e ID provisional -1 → remoto 102. |
| 8 | Borrador perdido por expiración | Limitación declarada | No se añade persistencia de formularios durante el congelamiento. Registrar antes de que venza la sesión. |
| 9 | Cola global, sin propietario por cuenta | Limitación declarada, alta | No se altera el esquema. Completar pendientes antes de cambiar de cuenta; no apto para uso multiusuario con cola pendiente. Logout elimina sesión, pero conserva SQLite. |
| 10 | Validación / privilegios | Cerrado en entorno de desarrollo | HTTP 400 y 403 documentados; repetir en APK release físico. |
| 11 | iOS | Limitación declarada | El entorno de trabajo Windows no permite ejecutar Xcode. No se promete IPA ni archivo Xcode generado. |
| 12 | Notificaciones | Descartado de esta entrega | No forman parte del flujo comprometido de inventario; cámara y Crashlytics cubren capacidades nativas. Trabajo futuro. |
| 13 | Reinstalación / backup | Limitación declarada | La evidencia disponible es login, no una prueba del almacén tras reinstalar. No se cambia la política de backup en esta sesión. |

## Ajustes de empaquetado de hoy

- Firma local, exclusión de secretos y configuración de versionado.
- URL del backend configurable al compilar para teléfono físico; HTTP permitido solo al host elegido para el laboratorio.
- Logging HTTP apagado en release; minificación activada y comprobación del recorrido.
- Guías y matriz de pruebas sin resultados inventados.

Sin funciones nuevas, rediseño, actualización de bibliotecas ni migración de SQLite.
Se conserva applicationId pe.edu.upeu.pharmamobil (también registrado en Firebase).
Versión de esta entrega: versionCode 1, versionName 1.0.0; no hay evidencia de publicación anterior en tienda.

## Criterio de salida

AAB y APK firmados, firma verificada y recorrido release en dos Android físicos.
Registrar la disponibilidad y los modelos en la matriz de pruebas.
No se crea v1.0-final hasta compilar, verificar y registrar el commit exacto.
