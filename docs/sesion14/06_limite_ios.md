# Paso 6. Límite de firma y archivo iOS

**Limitación del entorno utilizado:** Windows no permite ejecutar Xcode para archivar iOS. No se ha configurado un equipo Apple, ejecutado Product > Archive ni generado un archivo .xcarchive o .ipa.

La evidencia anterior de Gradle en Windows indica que `iosSimulatorArm64Test` está deshabilitada porque los tests de simulador requieren macOS. Sirve para describir el límite del entorno, no es una ejecución de Xcode ni prueba la compilación iOS.

Para el informe: agrupar la captura existente de esa advertencia con esta explicación y la declaración de disponibilidad. No presentar como obtenida una captura del Organizador, un perfil de aprovisionamiento o un error de Xcode.

## Qué permitiría cerrar el límite

Acceso a macOS/Xcode compatible, dispositivo iOS y cuenta/equipo de firma; revisar identificación, configuración del backend y dependencias de plataforma. Seleccionar equipo en Signing & Capabilities, elegir Any iOS Device, ejecutar Product > Archive y verificar el resultado en Organizer. Exportar solo según lo permita la cuenta y el perfil; registrar la limitación de distribución de una cuenta gratuita. La URL Android configurable no configura automáticamente el cliente iOS.

Estado: límite documentado; compilación, firma y ejecución iOS no verificadas. Esta declaración no equivale a entregar un IPA.
