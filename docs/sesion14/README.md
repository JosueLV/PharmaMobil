# Guía práctica 14 — PharmaMobil


## Estado de preparación

Rama `feature/builds-14` creada desde develop e integrada con el trabajo de sesión 13. La fusión de cierre en develop se realiza después de revisar la entrega.

| Paso | Documento | Estado real |
|---|---|---|
| 1 | [Alcance congelado](01_alcance_congelado.md) | Clasificación preparada con límites explícitos |
| 2 | [Almacén privado](02_almacen_privado.md) | Procedimiento y exclusiones; creación y respaldo en PC por realizar |
| 3 | [Firma y red](03_firma_y_red.md) | Configuración implementada; compilación por comprobar en Windows |
| 4 | [Builds y firma](04_builds_y_firma.md) | Script preparado; AAB/APK y firma por ejecutar |
| 5 | [Minificación](05_minificacion.md) | R8 habilitado; recorrido release por ejecutar |
| 6 | [Límite iOS](06_limite_ios.md) | Declarado: sin Mac/Xcode/iPhone |
| 7 | [Dispositivos y defectos](07_pruebas_dispositivos.md) | Matriz lista; identificación de dispositivos y resultados por registrar |

No se presenta este material como certificación de los pasos físicos. El entorno de asistencia intentó `bash gradlew :shared:testAndroidHostTest :androidApp:assembleDebug --no-daemon`, pero no pudo descargar Gradle 9.5.0 (`Network is unreachable`). No llegó a compilar ni ejecutar los tests. La prueba anterior de sesión 13 no valida los cambios actuales. Repetir en Windows con el entorno del proyecto.

## Archivo privado de firma

Copiar `keystore.properties.example` a `keystore.properties` y completar **solo localmente**:

```properties
storeFile=C:/ClavesPharmaMobil/equipo2.jks
storePassword=COMPLETAR_EN_PRIVADO
keyAlias=equipo2
keyPassword=COMPLETAR_EN_PRIVADO
```

Nunca subir valores reales ni el almacén. El ejemplo versionado deja las contraseñas vacías. Git ignora propiedades, almacenes y la carpeta de artefactos `entrega-local/`.

## Lista de cotejo de la guía

| N.º | Criterio | Situación |
|---|---|---|
| 1 | Ficha clasificada y alcance congelado | Documentado |
| 2 | Almacén y copia externa | Por realizar en privado |
| 3 | Secretos fuera de Git y exclusiones | Configurado; comprobar antes de cada envío |
| 4 | Debug abre y compila sin propiedades | Configuración prevista; ejecución bloqueada en entorno remoto |
| 5 | AAB firmado sin errores | Por ejecutar |
| 6 | APK generado e instalado en físico | Por ejecutar |
| 7 | Firma con titular y huella | Por ejecutar y capturar |
| 8 | Release completo, serialización e inyección | Por probar en físico |
| 9 | Desactivación de minificación justificada si aplica | No aplica actualmente: R8 habilitado |
| 10 | iOS archivado o límite documentado | Límite declarado, sin archivo generado |
| 11 | Dos Android físicos | Dispositivos y pruebas por registrar |
| 12 | Defectos con pasos, severidad y estado | D-01 y D-02 documentados; agregar hallazgos reales |
| 13 | Identidad y versión coinciden con artefacto | Declaradas en código; verificar artefacto cuando exista |
| 14 | Commits por paso | Preparación separada por paso; añadir resultados al ejecutarlos |

## Evidencias para completar el informe

Agrupar por tema, con explicación breve y capturas legibles, no una imagen aislada por hoja:

1. Firma y versión: BUILD SUCCESSFUL, AAB/APK, titular y SHA-256 del certificado. Sin claves ni contraseñas.
2. Android físico D1 y D2: modelo/Android y recorrido P01-P08, con resultados y defectos. La sesión 13 se cita como antecedente; no reemplaza pruebas release.
3. R8: minificación habilitada y recorrido funcional del APK correspondiente.
4. iOS: entorno Windows, advertencia previa y declaración de falta de Mac; sin simular un Archive.
5. Ficha de versión y checklist: applicationId real `pe.edu.upeu.pharmamobil`, 1 / 1.0.0, commit, hash de archivos, certificado y etiqueta final solo después del cierre.

La entrega final requiere AAB, APK y evidencia, además del documento. Las contraseñas/JKS no forman parte del entregable público. Mantener el mapeo R8 y el respaldo de la clave bajo custodia del equipo.

Referencias: [firma Android](https://developer.android.com/studio/publish/app-signing), [apksigner](https://developer.android.com/tools/apksigner), [seguridad de red](https://developer.android.com/privacy-and-security/security-config), [reglas de Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization#android).
