# Paso 5. Validar release con R8

Configuración: `isMinifyEnabled = true`, reglas de optimización predeterminadas de Android y atributos de depuración/metadatos en `proguard-rules.pro`.

No se añaden reglas globales `-keep class ** { *; }` ni `-dontwarn **`. Kotlinx Serialization incluye reglas de consumidor para los casos habituales; los módulos actuales de Koin referencian constructores directamente. Esto justifica comenzar con reglas pequeñas, pero no demuestra que la aplicación funcione tras optimizar.

En el APK firmado de cada dispositivo comprobar:

1. Login correcto e incorrecto, apertura de Productos y detalle: transporte y deserialización.
2. Registrar, editar y eliminar un producto de prueba con privilegios; rechazo del nombre `AB`.
3. Navegar por las pantallas que usan ViewModel: creación por Koin sin cierres.
4. Leer y registrar sin red, recuperar conexión y comprobar envío sin duplicados.
5. Permisos y cierre de sesión.

Si aparece un fallo exclusivo del release, registrar traza, versión y dispositivo; identificar la clase afectada y agregar solo su regla necesaria. Reconstruir, verificar firma y repetir el recorrido. Conservar `androidApp/build/outputs/mapping/release/mapping.txt` con el build para interpretar trazas (no contiene la clave de firma). Comprobar la recepción en Crashlytics si se utiliza para validar release.

No desactivar R8 para ocultar un fallo. Si se decide desactivarlo por plazo, registrar motivo, defecto asociado y alcance de las pruebas; regenerar los artefactos y corregir la ficha. Estado actual: habilitado en código, ejecución física aún no realizada.
