# Paso 4. Generar AAB y APK firmados

Estado: automatización preparada. Aún no hay artefactos de release generados ni firma del equipo verificada.

## Preparación en Windows

1. Completar los pasos 2 y 3: almacén privado, respaldo y archivo de propiedades local.
2. Usar el JDK del proyecto en Android Studio y disponer de `java`, `keytool` y `jarsigner` en la terminal. Si no están en PATH, agregar a la sesión la carpeta `bin` de ese mismo JDK (por ejemplo `C:\Program Files\Android\Android Studio\jbr\bin`).
3. Confirmar IP de la PC y acceso al backend desde el teléfono. No escribir literalmente IP_DEL_PC.
4. Guardar los cambios en Git: el script exige un árbol limpio y registra su commit.

```powershell
# Ejemplo: sustituir por la IPv4 real de la PC.
.\scripts\preparar-entrega-android.ps1 -ApiUrl 'http://192.168.1.40:8080/api/v1/'
# Si el SDK está en otra carpeta, agregar -SdkPath 'C:\ruta\Android\Sdk'.
```

El script ejecuta tests del módulo compartido, bundleRelease y assembleRelease. Desactiva la caché de configuración en esta ejecución de firma para no persistir sus valores en esa caché. No solicita ni imprime contraseñas. Si falla, detener la entrega y conservar el error.

Produce en `entrega-local/<fecha>/` (ignorado por Git):

- `Equipo2_ProyectoFinal_Android.aab`: paquete para entrega/tienda, no se instala directamente.
- `Equipo2_ProyectoFinal_Android.apk`: instalable para probar.
- `firma-apk.txt`: apksigner verify --verbose --print-certs.
- `firma-aab.txt`: jarsigner -verify; `certificado-aab.txt`: certificado del AAB.
- `sha256.csv`: integridad de cada archivo; `ficha-build.txt`: commit, URL, versión y ejecución.

La huella SHA-256 del **archivo** no es la huella del **certificado**. Registrar ambas con su etiqueta correcta. Comparar los certificados de APK y AAB con `keytool -list -v -keystore C:\ClavesPharmaMobil\equipo2.jks -alias equipo2`. El almacén solicita su contraseña de forma interactiva. La captura debe mostrar titular, huella y verificación, nunca contraseñas.

## Instalación

Transferir el APK al teléfono, permitir al gestor de archivos instalar esa aplicación si Android lo solicita y abrirla sin Android Studio conectado. Mantener Wi-Fi para el backend y anotar modelo, versión y fecha. Desautorizar luego la fuente si no se necesita.

Una instalación previa firmada con debug no se puede actualizar con otra clave. Antes de desinstalar, sincronizar y confirmar que no hay registros pendientes: desinstalar elimina datos locales. Obtener evidencia de primera instalación solo si realmente se realiza; una pantalla de login no demuestra por sí sola instalación limpia.

## Comprobación opcional del AAB

Con bundletool oficial, generar `revision.apks` en modo universal indicando `--bundle`, `--output`, `--ks` y `--ks-key-alias=equipo2`. Dejar que solicite las contraseñas; no ponerlas en argumentos ni capturas. Extraer `universal.apk`, verificarlo con apksigner e instalarlo. No sustituye las pruebas del APK entregado sin registrar qué archivo se usó.

## Cierre de versión

Primero resolver defectos, obtener autorización para fusionar en develop y realizar esa fusión. Compilar y verificar desde el commit final limpio; repetir lo afectado por cualquier cambio posterior. Después crear `v1.0-final` exactamente sobre ese commit y subir la etiqueta. No se ha creado una etiqueta final anticipada.
