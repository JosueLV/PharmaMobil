# Paso 7. Matriz y defectos de dispositivos físicos


La guía exige dos para la sesión y un tercero para la actividad autónoma. Las capturas previas del emulador son antecedentes de debug: no acreditan pruebas de un APK firmado en teléfonos físicos.

## Identificación

| Dato | D1 | D2 | D3 actividad autónoma |
|---|---|---|---|
| Marca y modelo | Por registrar | Por registrar | Por registrar |
| Android y API | Por registrar | Por registrar | Por registrar |
| Fecha y responsable | Por registrar | Por registrar | Por registrar |
| Archivo APK / SHA-256 | Por registrar | Por registrar | Por registrar |
| Commit / versión / certificado | Por registrar | Por registrar | Por registrar |
| Tipo de instalación real | Por registrar | Por registrar | Por registrar |

## Recorrido (repetir sobre el mismo APK en cada dispositivo)

| ID | Acción y criterio esperado | D1 | D2 | Evidencia / defecto |
|---|---|---|---|---|
| P01 | Instalar APK desde archivo sin IDE conectado; nombre e icono correctos | Por ejecutar | Por ejecutar | |
| P02 | Login válido, inválido y sin red: mensajes distinguibles | Por ejecutar | Por ejecutar | |
| P03 | Crear, consultar, editar y eliminar producto de prueba; nombre AB rechazado con mensaje en campo | Por ejecutar | Por ejecutar | |
| P04 | Usuario vendedor sin acciones de escritura; petición no autorizada obtiene 403 y mantiene sesión | Por ejecutar | Por ejecutar | |
| P05 | Con sesión válida: modo avión, reabrir, leer y crear; registro marcado pendiente | Por ejecutar | Por ejecutar | |
| P06 | Reconectar y sincronizar: pendiente pasa a 0, producto aparece una sola vez con ID del servicio | Por ejecutar | Por ejecutar | |
| P07 | Cámara: conceder, denegar y denegar permanentemente; acceso a Ajustes cuando corresponda | Por ejecutar | Por ejecutar | |
| P08 | Sin pendientes: cerrar sesión, cerrar app y abrir sin conexión; login y sin acceso autenticado | Por ejecutar | Por ejecutar | |

En P04 el cliente oculta escritura al vendedor; no modificar la app para forzar botones. Si se prueba el rechazo del servicio con una petición externa, identificarla como prueba de API separada y no como acción del APK. La evidencia 403 anterior debe fecharse, no renombrarse como nueva prueba de release.

P08 comprueba la sesión, no que SQLite esté vacía. La base local persiste y la cola no está separada por cuenta: registrar esa limitación. No cambiar de cuenta con operaciones pendientes. Para P05 comprobar que la sesión siga vigente; un 401 o una sesión cerrada no representa el caso offline autenticado.

Registrar resultado `Pasa` o `Falla` solo después de ejecutar, con nombre de captura/video. No cambiar todos los estados por disponer de una captura inicial. Para D3 duplicar esta matriz.

## Registro de defectos y límites conocidos

### D-01 — Borrador perdido al expirar sesión
- Dispositivo y versión observados: emulador Google sdk_gphone16k_x86_64, Android 17, debug previo (no prueba física de release).
- Pasos: 1. Iniciar sesión. 2. Escribir un producto sin registrar. 3. Provocar o esperar expiración y recibir 401. 4. Volver a autenticarse.
- Esperado: conservar o recuperar el borrador no enviado.
- Obtenido: formulario vacío, según evidencia previa del estudiante.
- Severidad: media.
- Estado: pendiente técnico, declarado como limitación del alcance congelado. Requiere almacenamiento de borradores y pruebas; no se afirma corregido.

### D-02 — Cola local compartida entre cuentas
- Dispositivo: hallazgo de revisión de código, no ejecución física; versión base 5d11011.
- Pasos para verificar: 1. Crear sin red con administrador. 2. Cerrar sesión antes de sincronizar. 3. Entrar con otra cuenta. 4. Revisar el pendiente e intentar sincronizar.
- Esperado: aislar o bloquear los pendientes de la cuenta anterior.
- Obtenido: el esquema de producto/cola no registra propietario. El resultado concreto del recorrido está por medir.
- Severidad: alta si se alternan cuentas con pendientes.
- Estado: pendiente técnico, declarado como limitación; no ejecutar cambios de cuenta con pendientes durante la demostración. Esta medida no corrige el diseño.

### Plantilla de un defecto nuevo
- ID: D-03.
- Modelo / Android / APK / commit / fecha:
- Pasos numerados:
- Esperado:
- Obtenido y evidencia:
- Severidad: alta / media / baja, con motivo.
- Estado: corregido / pendiente / descartado con motivo.
- Si se corrige: commit, nuevo build y resultado de repetir la prueba.
