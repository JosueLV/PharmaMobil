# Paso 7 · Pruebas y verificación

## Pruebas añadidas

- `SincronizacionRefinadaTest`: 503 suma intento, 409 marca fallida, esperas 1/2/4/8/16 s persistidas, máximo de cinco, confirmación anticipada, confirmación explícita, protección de pendientes en la descarga, interrupción por 401, cancelación del POST y corrección de una fallida.
- `GestorPermisosTest`: primera solicitud, denegación con explicación, denegación definitiva, concesión y ViewModel con gestor falso (incluye doble toque y retorno de Ajustes).
- `ContextoTecnicoTest`: filtra información ajena al vocabulario técnico y nombres de evento no autorizados.
- Suite anterior: se conserva; el envío tras una respuesta incierta ahora se confirma explícitamente, según la advertencia de idempotencia del PDF. Se añade comprobación del resumen observable.
- `MigracionOfflineTest`: recorre la versión 1 hasta la versión actual y verifica la conservación de datos y los valores iniciales de las nuevas columnas.

Cada escenario de repositorio crea y cierra su driver en memoria y usa MockEngine, sin servidor ni emulador. La hora de los reintentos es inyectable y se avanza en las pruebas sin esperar tiempo real.

## Corrección detectada al preparar las pruebas

Una cancelación durante el POST también tiene resultado incierto. La fila se marca como pendiente de confirmación ANTES de iniciar el envío. Si la app se cierra o la corrutina se cancela, no se vuelve a crear automáticamente al abrirla. El resultado confirmado reemplaza la fila local por la del servidor.

## Verificación realizada aquí

- Migraciones SQL 1 → 2 → 3 ejecutadas en SQLite: conservan registros y producen el mismo esquema que una instalación nueva.
- Consultas reales de la cola ejecutadas en SQLite: límite, espera, confirmación, fallo permanente, corrección y conservación de pendientes.
- Validación de AndroidManifest.xml, Info.plist y coincidencia del paquete de google-services.json.
- Verificación de que los imports del SDK Firebase están en la capa de datos.
- `git diff --check` sin errores.
- Se intentó `bash gradlew :shared:testAndroidHostTest :androidApp:assembleDebug --console=plain`, pero no pudo descargar Gradle 9.5.0: `java.net.SocketException: Network is unreachable`. No se llegó a compilar ni a ejecutar la suite en este entorno. NO se presenta como pruebas aprobadas.

## Ejecución pendiente en el equipo del usuario

```powershell
.\gradlew.bat :shared:testAndroidHostTest
.\gradlew.bat :androidApp:assembleDebug
```

El JSON Android ya está incluido. Sincronizar Gradle en el IDE antes de instalar la app actualizada.

## Evidencias que se pedirán después de compilar

1. Salida completa de la suite y del build Android.
2. Inventario en modo avión con producto pendiente y contador.
3. Error permanente visible, motivo y diálogo de revisión; luego corrección y reenvío comprobado en PharmaSoft.
4. Permiso cámara denegado, concedido y denegado permanentemente con botón Ajustes (Android permite volver a pedir o enviar a Ajustes según su versión y las elecciones del usuario).
5. Acerca de con Firebase configurado y error de prueba recibido en Crashlytics.
6. Recorrido manual de los ocho momentos del paso 1, distinguiendo hallazgo del código y resultado observado.
7. iOS solo si se dispone de Mac y dispositivo/simulador: configuración plist propia, ejecución y evidencia real. No usar una captura Android como evidencia iOS.

El entregable PDF se preparará después, con el formato y colores de la guía y únicamente con capturas reales aportadas por el usuario.
