# Paso 6 · Firebase

Se integra Crashlytics con GitLive 2.7.0, detrás de `domain/port/Telemetria`. No se agrega Messaging: la guía exige al menos un módulo y el permiso real elegido es cámara. La sesión JWT sigue perteneciendo a PharmaSoft.

Android usa el `google-services.json` proporcionado por el usuario, correspondiente a `pe.edu.upeu.pharmamobil`, proyecto `pharmamobil-c8506`. Google Services y Crashlytics están aplicados en `androidApp`. El proveedor inicializa Firebase al iniciar la app; no hace falta instalar el plugin de ChatGPT.

`TelemetriaFirebase` registra fallos no fatales y registros de contexto; no usa Analytics. El adaptador filtra campos y valores técnicos, elimina el mensaje y la causa de las excepciones y no envía tokens, cuerpos HTTP, nombres de personas ni de productos. Koin inyecta la implementación real; las pruebas usan una falsa sin Firebase.

## Comprobación en Android

1. Sincronizar Gradle y ejecutar la app.
2. Iniciar sesión y abrir Acerca de.
3. Comprobar que dice «Firebase configurado».
4. Pulsar «Registrar prueba técnica», cerrar y volver a abrir la app con internet.
5. Revisar el error no fatal en Firebase Console → Crashlytics. La recepción puede tardar; la app no afirma que ya se entregó al servidor.
6. Capturar la pantalla y el error en la consola. Esto todavía necesita ejecución real; no se ha inventado una evidencia.

## iOS

El proyecto Xcode enlaza FirebaseCore y FirebaseCrashlytics 11.8.0 mediante Swift Package Manager, la versión de referencia nativa de GitLive 2.7.0. Se inicializa desde `data/ConfigurarFirebase.swift` solo si existe `GoogleService-Info.plist` en el bundle.

El JSON Android no sirve para iOS. Falta registrar la app iOS con el bundle ID real del equipo y añadir su plist a `iosApp/iosApp/`. Se requieren Mac/Xcode para resolver paquetes, ejecutar y verificar símbolos/dSYM; no se afirma aquí una prueba iOS superada. Los tests nativos de Gradle necesitan además rutas de enlace a los frameworks de Firebase (el README oficial de GitLive explica CocoaPods para ese caso).

Referencias:
- https://github.com/GitLiveApp/firebase-kotlin-sdk/tree/v2.7.0
- https://firebase.google.com/docs/crashlytics/android/get-started
