# Paso 3. Configuración de firma, versión y backend

- applicationId: pe.edu.upeu.pharmamobil, se mantiene para no romper Firebase.
- versionCode: 1; versionName: 1.0.0.
- Gradle lee keystore.properties solo si existe; debug no exige firma privada.
- preReleaseBuild exige las cuatro propiedades, un almacén existente y una URL explícita para teléfono físico. No se usa la firma debug como alternativa silenciosa.
- BuildConfig.PHARMA_API_URL llega al cliente Ktor por propiedades de Koin. DEBUG controla el registro HTTP; release lo apaga.
- La configuración XML de red se genera para el host elegido. El resto mantiene HTTP bloqueado. HTTP local es una excepción de laboratorio, no una configuración de producción.

## Teléfono físico

1. PC y teléfono en la misma red Wi-Fi, sin aislamiento entre clientes. Backend escuchando en la interfaz LAN, no solo 127.0.0.1.
2. Ejecutar `ipconfig` en Windows; tomar IPv4 del adaptador activo (no VPN/VirtualBox).
3. Permitir el puerto TCP 8080 para Java/backend en la red privada de Windows. No desactivar todo el firewall.
4. Comprobar desde el teléfono que se alcanza `http://IP_DEL_PC:8080/api/v1/productos`: incluso un 401 demuestra que responde el servidor, no que autorice la consulta.
5. Compilar usando `-PpharmaApiUrl=http://IP_DEL_PC:8080/api/v1/`.

No cambiar de IP entre construir y demostrar; si cambia, reconstruir y verificar los artefactos. Para el emulador, debug mantiene por defecto 10.0.2.2.

El cliente iOS conserva el valor predeterminado; requiere configuración cuando se disponga de Mac. No se afirma que el ajuste Android habilite iOS.
