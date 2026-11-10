# Paso 2. Crear una sola clave en Windows

Estado: procedimiento preparado; el almacén aún debe crearse en el PC del estudiante.
No enviar el .jks, contraseñas ni keystore.properties al chat o GitHub.

En PowerShell, fuera del repositorio:

```powershell
New-Item -ItemType Directory -Force C:\ClavesPharmaMobil
if (Test-Path C:\ClavesPharmaMobil\equipo2.jks) { throw 'Ya existe una clave. No generar otra.' }
keytool -genkeypair -v -keystore C:\ClavesPharmaMobil\equipo2.jks -storetype JKS -keyalg RSA -keysize 2048 -validity 10000 -alias equipo2
```

Si keytool no está en PATH, usar keytool.exe del directorio bin del JDK configurado en Android Studio. La contraseña se introduce en la solicitud interactiva, no como argumento. Usar al menos 6 caracteres. Completar datos reales del titular y país PE. Anotar alias y contraseñas en un gestor privado. Si no se desea contraseña de clave diferente, aceptar la misma cuando keytool lo ofrezca.

Hacer una copia privada en otra unidad o medio seguro y comprobar que abre con `keytool -list -keystore RUTA_DE_LA_COPIA`. No perder ni reemplazar la clave entre versiones.

```powershell
Copy-Item keystore.properties.example keystore.properties
notepad keystore.properties
```

Completar las dos contraseñas localmente. Para caracteres especiales del formato Java Properties (por ejemplo barra inversa), usar escapes apropiados; una ruta con / evita el problema de Windows. No hacer captura del archivo abierto.

Verificar las exclusiones:

```powershell
git check-ignore keystore.properties prueba.jks prueba.keystore
```

Evidencia admisible: confirmación de creación y titular/huella pública del certificado. No es evidencia de haber creado la clave el simple archivo de ejemplo.
