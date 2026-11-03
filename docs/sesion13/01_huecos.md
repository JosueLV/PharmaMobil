# Sesión 13 · Paso 1: recorrido y huecos

Base: sesión 11, commit `139ce82`. El usuario confirmó que su ejecución terminó correctamente.
Esta tabla procede de una revisión del código; NO sustituye el recorrido manual del PDF.
Las capturas y resultados de Android/iOS se completarán con el usuario al preparar el entregable.

| N.º | Momento | Hallazgo verificable en código | Gravedad | Decisión |
| --- | --- | --- | --- | --- |
| 1 | Primera apertura sin sesión | Se muestra LoginScreen; no se inventa una sesión si no hay token. | Baja | Mantener; verificar instalación limpia en Android. |
| 2 | Credenciales incorrectas | El mapeo distingue 401 de IOException; la UI tiene mensajes diferentes. | Media | Mantener y comprobar ambos mensajes en el recorrido. |
| 3 | Lista local vacía | Existe EstadoVacio con texto, incluso al fallar la sincronización. | Baja | Mantener y capturar. |
| 4 | Crear en modo avión | Se guarda con id negativo y marca pendiente; faltan intentos y último error. | Alta | Cerrar hoy en pasos 2–4. |
| 5 | Recuperar la señal | Un POST sin respuesta podría repetirse y duplicarse; no consta soporte de idempotencia del servidor. | Alta | Cerrar hoy: espera persistida, límite y confirmación manual de reenvío; no inventar garantía del backend. |
| 6 | Validación del servidor | Los errores llegan a los campos, pero la fila pendiente puede volver a enviarse automáticamente. | Alta | Cerrar hoy: marcar error permanente, mostrar motivo y permitir corregir antes de un reenvío explícito. |
| 7 | Token vencido durante formulario | SesionViewModel limpia la sesión; el formulario no se guarda para recuperación. | Media | Sesión 14: borrador asociado al usuario, sin reutilizarlo entre cuentas. Hoy conservar aviso de expiración y parar la cola ante 401. |
| 8 | Cerrar sesión | Se borran token y sesión segura; la base de productos es global y los pendientes no tienen propietario. | Alta | Sesión 14: separar la cola por usuario antes de afirmar soporte multiusuario. Hasta entonces usar una única cuenta para evidencias de cola; no borrar pendientes silenciosamente. |
| 9 | Estado de sincronización | Se ve última actualización y aviso de red; faltan contadores y acción sobre fallidas. | Alta | Cerrar hoy, paso 4, mediante consultas observables de SQLite. |
| 10 | Permisos | No existe GestorPermisos ni flujo de solicitud nativa. | Media | Cerrar hoy, paso 5: cámara con explicación, tres estados y Ajustes. |
| 11 | Firebase | No hay SDK ni configuración de un proyecto Firebase en el repositorio. | Media | Paso 6: interfaz, adaptador y configuración de compilación; activación real y evidencia dependen del proyecto del usuario. |

## Verificación manual pendiente

Recorrer los ocho momentos de la tabla de la guía en Android y anotar resultado real, fecha y plataforma. No se han ejecutado aquí emulador, backend ni iOS. No dar por probado el funcionamiento de Firebase hasta ver el evento de prueba en su consola.

## Rama

`feature/integrador-13` se creó desde `origin/develop` y recibió un merge de la rama offline validada. `develop` y las ramas anteriores no se modifican. Se registra un commit individual al terminar cada paso.
