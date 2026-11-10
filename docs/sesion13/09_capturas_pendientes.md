# Capturas pendientes: actividad autónoma 13

## 0. Actualizar y comprobar

En PowerShell, desde `C:\ProyectosAndroid\PharmaMobil`:

```powershell
git status --short
git fetch origin
git switch feature/integrador-13
git pull --ff-only
.\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug
git rev-parse HEAD
```

Si Git avisa de cambios locales, conservarlos antes de actualizar; no usar `reset --hard`. Instalar/ejecutar la versión actualizada desde Android Studio. La nueva pantalla de detalle se abre tocando la tarjeta del producto.

La revisión aquí del 05/11 incluyó `git diff --check`. Se intentó Gradle, pero la descarga de Gradle 9.5.0 falló por `Network is unreachable`; no se ejecutó la suite nueva en este entorno. La ejecución que pasó el 03/11 corresponde a la versión anterior.

## 1. Capturas reutilizables

Ya se conservan las capturas del 03/11: cámara denegada, bloqueada y concedida; inventario offline y reenvío; aviso de sesión expirada y regreso con formulario vacío. No hace falta repetir permisos ni provocar otra expiración si el código de esos flujos no cambia.

Para el requisito de ID provisional → definitivo, sí hace falta una nueva creación offline: el ID provisional del producto anterior ya no puede deducirse de su fila remota. Conservar nombre y ambos ID del nuevo ejemplo.

## 2. Primer inicio, sesión y sincronización (A1-A3)

Preferir un segundo AVD/emulador limpio. Así se demuestra primera apertura sin borrar los datos ni permisos del emulador anterior. Con backend encendido:

1. Abrir la app recién instalada, antes de entrar: capturar el login sin aviso de expiración.
2. Entrar con la cuenta de prueba que tiene escritura de catálogo; abrir el menú y capturar usuario y opciones. Confirmar su rol en el backend sin mostrar token/contraseña.
3. Cerrar y reabrir la app: anotar si restaura la sesión. No basta una captura del listado para afirmar persistencia del token.
4. Pulsar Sincronizar y capturar el listado. Anotar la hora y el número real de filas, incluyendo todas las pestañas.

Database Inspector permite respaldar el estado local sin mostrar credenciales. Ejecutar estas consultas de solo lectura sobre la base de la app:

```sql
SELECT COUNT(*) AS total_local,
       SUM(CASE WHEN pendiente = 0 THEN 1 ELSE 0 END) AS sincronizados,
       SUM(CASE WHEN pendiente = 1 THEN 1 ELSE 0 END) AS pendientes
FROM producto;

SELECT instante,
       datetime(instante / 1000, 'unixepoch', '-5 hours') AS ultima_sync_hora_peru
FROM sincronizacion WHERE id = 1;
```

En una base nueva, `total_local = 0`; la fila de sincronización puede no existir todavía. Tras la primera descarga sin pendientes, `sincronizados` corresponde a las filas descargadas. Recortar la evidencia al resultado de la consulta; no capturar todo el escritorio.

## 3. Crear con conexión (A4)

Usar un nombre nuevo, por ejemplo `Evidencia online AA13`, precio `6.50`, stock `12`. Registrar con conexión y verificar 0 pendientes/0 fallidas. Abrir su tarjeta y capturar `ID del servicio`. Si sigue pendiente, no afirmar que el servidor asignó un ID.

## 4. ID provisional y definitivo (A5-A6)

1. Activar modo avión y apagar Wi-Fi. Crear `Evidencia offline AA13`, precio `5.00`, stock `10`.
2. Capturar el pendiente en la lista. Abrir su tarjeta y capturar `ID local provisional` negativo. El detalle debe abrir aunque no haya red.
3. Volver a la lista, cerrar y reabrir la app sin red para verificar persistencia si se necesita evidencia adicional.
4. Recuperar conexión. Comprobar que el producto no existe aún en el servidor y confirmar el reenvío desde Revisar y reintentar. No reenviar a ciegas si existe una respuesta incierta.
5. Capturar el inventario con contadores antes/después y el detalle del mismo nombre con `ID del servicio`. Anotar ambos ID reales; no inventarlos ni asumir una relación numérica.

## 5. Validación remota (A7)

Usar únicamente el producto de prueba sincronizado del paso A4. El backend de referencia `dreyna/pharmaSoft`, DTO `ProductoRequestDTO` revisado el 05/11, valida nombre de 3 a 150 caracteres; el cliente solo rechaza nombre vacío. Confirmar que la versión local conserva esa regla.

1. Editar ese producto y poner nombre `AB`, manteniendo precio y stock válidos.
2. Guardar. Capturar el error del campo Nombre sin abandonar el formulario.
3. En Logcat filtrar `PHARMA_KTOR_LOG` y capturar solo la respuesta HTTP de esa petición PUT y su ruta. El resultado esperado es 400, pero registrar el resultado observado.
4. Cancelar la edición al terminar. Si el servidor aceptó `AB`, registrar la diferencia y restaurar el nombre original del producto de prueba; no afirmar que hubo validación.

No usar precio cero para esta evidencia: la app lo rechaza antes de contactar al servidor. Tampoco llamar 400 a un conflicto 409 por duplicado. La cabecera Authorization ya se oculta en el logger; no mostrar tokens ni respuestas de login.

## 6. Cuenta sin escritura (A8)

Terminar primero el envío de pendientes con la cuenta que los creó (la cola sigue siendo global). Cerrar sesión e ingresar con la cuenta `vendedor`, si conserva los permisos de lectura del backend de prueba.

- Capturar el catálogo sin formulario, editar ni eliminar. Esto acredita la restricción visual, no un HTTP 403.
- Para el código real, ejecutar desde el cliente API local una solicitud de escritura con esa misma cuenta y un cuerpo inválido (`{}`) a `POST /api/v1/productos`. En el backend de referencia, la autorización debe rechazarla con 403 y el cuerpo inválido evita una creación válida si la cuenta tiene más permisos de los esperados. No usar datos de un producto real ni un token administrador. No modificar la app para eludir sus restricciones.
- Capturar solamente método, ruta y estado devuelto; ocultar Authorization y credenciales. Si devuelve 400 en lugar de 403, la cuenta pasó la autorización: revisar sus permisos y documentar el resultado real.
- Distinguir en el informe la evidencia del cliente API de la captura de PharmaMobil. No afirmar que la app recibió un 403 si no lo recibió.

## 7. Expiración (A9)

Reutilizar las capturas del 03/11: aviso de sesión expirada y formulario vacío al entrar otra vez. El estudiante confirmó pérdida del texto sin registrar; documentarlo como pendiente para sesión 14. No inventar duración del token ni cambio de configuración que no estén acreditados.

## 8. Cerrar sesión (A10)

1. Con 0 pendientes y 0 fallidas, anotar el conteo de SQLite.
2. Cerrar sesión desde el menú; capturar login sin el aviso de expiración.
3. Cerrar la app, apagar la red y reabrir: debe permanecer en login. Esto comprueba que no se restaura la sesión anterior.
4. Consultar nuevamente el número de productos en Database Inspector. No se borran desde `cerrarSesion`; capturar el resultado real.
5. Restaurar la red. No cambiar de cuenta mientras haya productos pendientes de otra cuenta.

## Datos para cerrar el documento

Código universitario, grupo, SHA del build probado, fecha y dispositivo de las nuevas pruebas, número de filas/hora de sincronización, ID local y definitivo del ejemplo offline, resultado real de 400/403 y disponibilidad o ausencia de Mac. Se incorporarán al PDF cuando se reciban; los datos aún desconocidos no se rellenan con valores ficticios.
