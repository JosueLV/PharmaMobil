# Producto D: ficha de integración

Actualizada: 05/11/2026. Las fechas futuras son objetivos de cierre; no equivalen a trabajo ya validado.

| N.º | Hueco | Gravedad | Decisión / resultado | Fecha de cierre real u objetivo |
| --- | --- | --- | --- | --- |
| 1 | No había intentos/error persistidos por fila. | Alta | Cerrado en práctica: esquema v3 y migración. Suite Android anterior aprobada por el estudiante. | Real: 03/11/2026. |
| 2 | Riesgo de repetir POST después de perder su respuesta. | Alta | Cerrado el reenvío automático inseguro: confirmación explícita, espera y máximo de intentos. La idempotencia general del servidor sigue sin garantizarse. | Real: 03/11/2026. |
| 3 | Faltaban contadores, antigüedad y revisión de pendientes. | Alta | Cerrado y evidenciado en Android. | Real: 03/11/2026. |
| 4 | Solicitud de 1000 filas con límite de 100 del backend. | Alta | Cerrado en e64523f; paginación completa de 100 en 100, build y sincronización confirmados. | Real: 03/11/2026. |
| 5 | Falta de permiso nativo verificable. | Media | Cerrado con cámara: tres estados y recuperación por Ajustes. | Real: 03/11/2026. |
| 6 | Integración Firebase sin evidencia real. | Media | Cerrado en Android: evento no fatal y claves de prueba visibles en Crashlytics. iOS queda en el punto 11. | Real: 03/11/2026. |
| 7 | Detalle consulta la red e ignora productos provisionales. | Media | Corrección implementada el 05/11: obtener por ID con prioridad local; ID visible en detalle y pruebas de regresión añadidas. Validación nueva pendiente. | Objetivo de validación: 09/11/2026. |
| 8 | Se pierde el formulario no registrado al expirar la sesión. | Media | Sesión 14: borrador asociado al usuario y restauración solo para la misma cuenta. Comportamiento actual observado, sin prometer recuperación. | Objetivo: 10/11/2026. |
| 9 | Cola SQLite global, sin propietario por cuenta. | Alta | Sesión 14: aislar pendientes por usuario y comprobar cambios de cuenta sin envío ajeno. Hasta entonces completar la cola antes de cambiar de cuenta. | Objetivo: 10/11/2026. |
| 10 | Faltan capturas y datos de los diez pasos del producto A. | Alta | Completar antes de entregar: acceso inicial, rol, conteos/hora, creación online, IDs, validación, autorización y logout. | Objetivo: 09/11/2026. |
| 11 | iOS sin compilación y ejecución acreditadas. | Alta | Sesión 14: Mac/Xcode, plist propio de Firebase, URL backend accesible y recorrido. Si no se dispone de Mac, registrar el bloqueo real. | Objetivo: 10/11/2026; sujeto a acceso a Mac. |
| 12 | Notificaciones ausentes. | Baja | Sesión 14: evaluar avisos y permiso Android 13+. No se exige añadirlas en esta actividad porque la app ya usa cámara. | Objetivo: 10/11/2026. |
| 13 | Reinstalación y restauración de token no verificadas. | Media | Completar prueba en un AVD de ensayo; describir efecto de backup sin asumir que se borra o conserva el token. Revisar política antes de publicación. | Objetivo: 09/11/2026. |

No se han descartado huecos. Las decisiones de sesión 14 tienen fecha objetivo explícita y no se marcan como cerradas en el PDF.

## Fuentes y corte de evaluación

- Actividad Autónoma N.º 13: productos A-D, formato, lista de cotejo y rúbrica.
- Código propio en [feature/integrador-13](https://github.com/JosueLV/PharmaMobil/tree/feature/integrador-13).
- Base validada el 03/11: [e64523f](https://github.com/JosueLV/PharmaMobil/commit/e64523fa10107c3ea1b5a212317faa0f47afc18b).
- El commit final evaluado será el devuelto por `git rev-parse HEAD` en la ejecución que produzca las nuevas evidencias. Se incluirá su enlace completo en la última página del PDF.
