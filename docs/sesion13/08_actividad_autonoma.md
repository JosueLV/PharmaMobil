# Actividad autónoma 13: recorrido y evidencias

Fecha de revisión: 5 de noviembre de 2026. Rama: `feature/integrador-13`.

Esta actividad es distinta de la guía práctica: requiere los productos A, B, C y D en un solo PDF. Este archivo prepara el recorrido y registra qué evidencia existe; no sustituye las capturas pendientes ni declara que se hayan ejecutado pruebas nuevas.

## Formato del PDF final

- Tamaño carta (612 × 792 puntos), entre 8 y 14 páginas, menos de 20 MB.
- Nombre: `Llontop_Vivas_Josue_Sesion13_Evidencias.pdf`.
- Portada: Josué Llontop Vivas, código universitario y grupo (estos dos datos deben ser proporcionados por el estudiante).
- Índice; capturas agrupadas por tema, recortadas al emulador o al contenido pertinente y con pies descriptivos.
- Última página: enlace a la rama y al SHA completo del commit evaluado, tomado de `git rev-parse HEAD` después de actualizar y probar.
- Entrega: 9 de noviembre de 2026, 23:59, hora de Perú. Revisión: 10 de noviembre de 2026.

## Producto A: diez pasos, en orden

Las capturas existentes son del 3 de noviembre, sobre `e64523f`. Se pueden reutilizar para comportamientos que siguen iguales, indicando su fecha y versión. La corrección posterior afecta al detalle de producto; su nueva vista requiere captura nueva. No presentar dos capturas de productos diferentes como la conversión del mismo ID.

| Paso | Estado que se debe registrar | Evidencia disponible / falta |
| --- | --- | --- |
| 1. Primera apertura | Sin token y base local vacía en una instalación realmente nueva. | Falta. Una pantalla de sesión expirada no acredita instalación limpia. Usar otro emulador para conservar las pruebas anteriores. |
| 2. Login correcto | Usuario, rol confirmado por el backend, privilegios y persistencia de sesión. Nunca mostrar el token. | Falta captura del menú después de entrar y comprobación al reabrir. La app usa permisos, no muestra una lista de roles. |
| 3. Primera sincronización | Número total de filas y hora de la sincronización. | Inventario online de las 20:51 disponible; no muestra hora exacta ni total global. Completar mediante Database Inspector. |
| 4. Crear con conexión | Registro confirmado e ID asignado por el servicio. | Falta creación nueva y detalle con ID del servicio. No confundir guardado local con respuesta remota. |
| 5. Crear sin conexión | ID negativo provisional, marca pendiente y contador. | Captura 20:54 disponible (Prueba offline S13); falta su ID original. Hacer una nueva pareja de capturas para un nuevo producto: lista pendiente y detalle local. |
| 6. Recuperar red | Pendientes antes/después y nuevo ID definitivo del mismo producto. | Capturas 20:57, 20:58 y 20:59 prueban persistencia, revisión y reenvío. Completar con el nuevo producto del paso 5 y su detalle tras sincronizar. |
| 7. Validación del servidor | HTTP observado y mensaje asociado al campo. | Falta. Provocar una validación remota sobre un producto de prueba ya sincronizado; una validación local sin petición no demuestra HTTP 400. |
| 8. Falta de privilegios | Código HTTP real y restricciones de interfaz para esa cuenta. | Falta. Catálogo de vendedor sin botones de escritura y petición rechazada por el servicio, documentados por separado. No habilitar botones prohibidos para hacer la captura. |
| 9. Token vencido | Aviso, destino del usuario y estado del formulario. | Capturas 21:44 y 21:45 disponibles: retorno al login; al volver, borrador perdido y productos registrados presentes. No repetir salvo que cambie ese comportamiento. |
| 10. Logout | Token/sesión segura borrados; qué queda en SQLite. | Falta captura tras Cerrar sesión y comprobación al reabrir sin red, más estado de la base local. El código conserva productos y pendientes; no afirmar que limpia SQLite. |

Procedimiento de captura: [09_capturas_pendientes.md](09_capturas_pendientes.md).

## Producto B: plataformas

| Aspecto | Android | iOS: estado del código, sin prueba de ejecución |
| --- | --- | --- |
| Token | `AlmacenSeguroAndroid`: EncryptedSharedPreferences y MasterKey AES256_GCM. Logout elimina `jwt` y `ultima_sesion_validada`, además de la caché Auth de Ktor. | `AlmacenSeguroIos`: Keychain mediante Security. No hay prueba de persistencia ni de borrado en dispositivo Apple. |
| Reinstalación | Pendiente de comprobar en el entorno del estudiante. El manifiesto permite backup: no asumir que reinstalar equivale siempre a datos vacíos. | Comportamiento de Keychain tras reinstalación no ejecutado; no inferirlo de Android. |
| Cámara | Tres estados observados. Tras denegación definitiva: Abrir Ajustes → Permisos → Cámara → permitir → volver a la app. | Implementación AVFoundation y descripción en Info.plist; diálogos y retorno de Ajustes pendientes de verificación. |
| Sin red | Creación local, reapertura y reenvío manual observados. El detalle se corrige para consultar primero `obtener(id)` local. | Lógica de cola compartida; driver nativo implementado. Sin evidencia de ejecución. |
| Notificaciones | No implementadas; no se declara POST_NOTIFICATIONS ni se usa Firebase Messaging. Crashlytics no envía notificaciones al usuario. | No implementadas ni verificadas. |
| Apariencia | Capturas de emulador Android 17; interfaz Compose con la misma paleta y tarjetas del proyecto. | No hay comparación visual ejecutada: tipografía, barras y áreas seguras se comprobarán en Mac. |

Android es la plataforma documentada. Los targets iOS existen, pero no se acredita que compilen ni se ejecuten: el equipo del estudiante es Windows y Gradle indica que `iosSimulatorArm64Test` necesita macOS. No se ha obtenido un error de compilación Xcode y no se inventa uno. Además, falta `GoogleService-Info.plist` para Firebase iOS y la URL `10.0.2.2:8080` está fijada para el emulador Android, por lo que deberá configurarse un host accesible desde iOS.

Plan para el 10/11/2026: disponer de Mac/Xcode, registrar la app iOS y agregar su plist al target, configurar la URL del backend, resolver las dependencias de Firebase, compilar y repetir el recorrido. Si no se consigue acceso a Mac, mantener el estado como pendiente. La fecha es un objetivo, no una prueba superada.

## Producto C: bitácora de permisos

| Permiso | Estado | Qué se ve | Recuperación | Evidencia del 03/11 |
| --- | --- | --- | --- | --- |
| Cámara | Denegado | «Denegado · puedes solicitarlo» y botón de solicitud. | Volver a solicitar y aceptar en el diálogo nativo. | 20:39; diálogo nativo disponible a las 20:38. |
| Cámara | Denegado para siempre | «Denegado permanentemente · habilítalo en Ajustes». | Abrir Ajustes → Permisos → Cámara → permitir; volver. | 20:40. |
| Cámara | Concedido | «Concedido»; no se pide de nuevo. | No requiere recuperación. | 20:43. |

La aplicación explica que solo comprueba el acceso a cámara y no toma ni envía fotografías. El inventario funciona aunque se rechace. INTERNET es un permiso normal declarado en el manifiesto, sin diálogo de autorización en tiempo de ejecución; no se inventan tres pantallas para él. Como ya existe un permiso real, la indicación condicional de implementar notificaciones cuando no se usa ninguno no aplica.

## Producto D y trazabilidad

La ficha con gravedad, decisión y fechas está en [10_ficha_integracion.md](10_ficha_integracion.md). Se conservará la pérdida de borrador como hallazgo para la sesión 14; no se presenta como corregida.

La captura de Crashlytics y la de BUILD SUCCESSFUL del 03/11 pueden servir como apoyo, pero no sustituyen ningún paso del mapa. La captura de pruebas antiguas no verifica el cambio nuevo de detalle: se necesita volver a ejecutar la suite y el build Android.

Distribución prevista (12 páginas): portada; índice y alcance; A1-A3; A4; A5-A6; A7-A8; A9-A10; plataforma Android; estado iOS y diferencias; permisos; ficha de integración; validación, lista de cotejo y enlaces. Se ajustará dentro de 8-14 páginas al recibir las capturas, manteniendo imágenes legibles.
