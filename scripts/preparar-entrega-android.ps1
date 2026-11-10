[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$ApiUrl,
    [ValidateRange(1, 99)][int]$Equipo = 2,
    [string]$SdkPath = ''
)
$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path $PSScriptRoot -Parent
Push-Location $raizProyecto
try {
    if (-not (Test-Path 'keystore.properties')) {
        throw 'Crea keystore.properties PRIVADO siguiendo docs/sesion14/02_almacen_privado.md.'
    }
    $uri = [Uri]$ApiUrl
    if (-not $uri.IsAbsoluteUri -or $uri.Scheme -notin @('http','https') -or
        $uri.Host -in @('10.0.2.2','localhost','127.0.0.1','[::1]')) {
        throw 'Usa la IP LAN del backend o un servidor HTTPS accesible desde el telefono.'
    }
    # Prioridad: parametro explicito, SDK del proyecto, entorno, ruta habitual.
    if ([string]::IsNullOrWhiteSpace($SdkPath) -and (Test-Path 'local.properties')) {
        foreach ($linea in [System.IO.File]::ReadAllLines((Join-Path $raizProyecto 'local.properties'))) {
            if ($linea -match '^\s*sdk\.dir\s*[=:]\s*(.+)$') {
                # Android Studio guarda rutas como C\:\\Android\\Sdk.
                $SdkPath = [regex]::Unescape($Matches[1].Trim())
                break
            }
        }
    }
    if ([string]::IsNullOrWhiteSpace($SdkPath)) { $SdkPath = $env:ANDROID_HOME }
    if ([string]::IsNullOrWhiteSpace($SdkPath)) { $SdkPath = $env:ANDROID_SDK_ROOT }
    if ([string]::IsNullOrWhiteSpace($SdkPath)) { $SdkPath = "$env:LOCALAPPDATA\Android\Sdk" }
    $carpetaBuildTools = Join-Path $SdkPath 'build-tools'
    if (-not (Test-Path -LiteralPath $carpetaBuildTools -PathType Container)) {
        throw "No existe build-tools en el SDK '$SdkPath'. Revisa sdk.dir en local.properties o pasa -SdkPath con la ubicacion del SDK de Android Studio."
    }
    Write-Host "SDK Android: $SdkPath"
    $buildTools = Get-ChildItem -LiteralPath $carpetaBuildTools -Directory |
        Where-Object { $_.Name -match '^\d+\.\d+\.\d+$' } |
        Sort-Object { [Version]$_.Name } -Descending |
        Where-Object { Test-Path (Join-Path $_.FullName 'apksigner.bat') } |
        Select-Object -First 1
    if (-not $buildTools) { throw 'Instala Android SDK Build-Tools o pasa -SdkPath con su ruta.' }
    $apksigner = Join-Path $buildTools.FullName 'apksigner.bat'
    $jarsigner = (Get-Command jarsigner -ErrorAction Stop).Source
    $keytool = (Get-Command keytool -ErrorAction Stop).Source
    $commit = & git rev-parse HEAD
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo identificar el commit Git.' }
    $cambios = & git status --porcelain
    if ($LASTEXITCODE -ne 0 -or $cambios) {
        throw 'Guarda y publica los cambios antes de construir. La entrega debe corresponder a un commit limpio.'
    }
    & .\gradlew.bat :shared:testAndroidHostTest :androidApp:bundleRelease :androidApp:assembleRelease "-PpharmaApiUrl=$ApiUrl" --no-configuration-cache
    if ($LASTEXITCODE -ne 0) { throw 'Gradle fallo. No se copio una entrega nueva.' }

    $apk = 'androidApp/build/outputs/apk/release/androidApp-release.apk'
    $aab = 'androidApp/build/outputs/bundle/release/androidApp-release.aab'
    foreach ($archivo in @($apk, $aab)) {
        if (-not (Test-Path $archivo)) { throw "No existe el artefacto esperado: $archivo" }
    }
    # Carpeta unica: conserva verificaciones y evita mezclar builds anteriores.
    $marca = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
    $salida = Join-Path 'entrega-local' $marca
    New-Item -ItemType Directory -Path $salida | Out-Null
    & $apksigner verify --verbose --print-certs $apk 2>&1 |
        Tee-Object -FilePath (Join-Path $salida 'firma-apk.txt')
    if ($LASTEXITCODE -ne 0) { throw 'La firma APK no es valida; no distribuir.' }
    # AAB usa firma JAR, no apksigner. Un certificado propio puede avisar que es autofirmado.
    & $jarsigner '-J-Duser.language=en' '-J-Duser.country=US' -verify $aab 2>&1 |
        Tee-Object -Variable verificacionAab |
        Tee-Object -FilePath (Join-Path $salida 'firma-aab.txt')
    if ($LASTEXITCODE -ne 0 -or ($verificacionAab -join "`n") -notmatch 'jar verified\.') {
        throw 'No se confirmo la firma JAR del AAB; revisar firma-aab.txt.'
    }
    & $keytool '-J-Duser.language=en' '-J-Duser.country=US' -printcert -jarfile $aab 2>&1 |
        Tee-Object -FilePath (Join-Path $salida 'certificado-aab.txt')
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo leer el certificado del AAB.' }
    Copy-Item $apk (Join-Path $salida "Equipo${Equipo}_ProyectoFinal_Android.apk")
    Copy-Item $aab (Join-Path $salida "Equipo${Equipo}_ProyectoFinal_Android.aab")
    Get-ChildItem $salida -File | Where-Object { $_.Extension -in @('.apk','.aab') } |
        Get-FileHash -Algorithm SHA256 |
        Select-Object @{Name='Archivo';Expression={Split-Path $_.Path -Leaf}}, Hash |
        Export-Csv (Join-Path $salida 'sha256.csv') -NoTypeInformation -Encoding UTF8
    @"
Proyecto: PharmaMobil - Equipo $Equipo
Commit: $commit
Compilado: $(Get-Date -Format o)
applicationId: pe.edu.upeu.pharmamobil
versionCode: 1
versionName: 1.0.0
Backend: $ApiUrl
Minificacion: habilitada
Tests: :shared:testAndroidHostTest termino sin error en esta ejecucion
Firma: verificaciones APK y AAB terminadas sin error
Recorrido en dispositivos: registrar resultados reales en docs/sesion14/07_pruebas_dispositivos.md
Etiqueta final: crear solo tras cierre, fusion autorizada y validacion del commit de entrega
"@ | Set-Content (Join-Path $salida 'ficha-build.txt') -Encoding UTF8
    Write-Host "Artefactos y verificaciones: $salida"
    Write-Host 'Compara la huella SHA-256 de APK y AAB con el almacen del equipo. Luego prueba el APK en dispositivos reales.'
} finally { Pop-Location }
