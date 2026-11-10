import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.net.URI
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

abstract class GenerarRedBackend : DefaultTask() {
    @get:Input abstract val host: Property<String>
    @get:Input abstract val permitirHttp: Property<Boolean>
    @get:OutputDirectory abstract val destino: DirectoryProperty

    @TaskAction fun generar() {
        val archivo = destino.get().file("xml/pharma_network_security_config.xml").asFile
        archivo.parentFile.mkdirs()
        archivo.writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <network-security-config>
                <base-config cleartextTrafficPermitted="false" />
                <domain-config cleartextTrafficPermitted="${permitirHttp.get()}">
                    <domain includeSubdomains="false">${host.get()}</domain>
                </domain-config>
            </network-security-config>
        """.trimIndent())
    }
}

abstract class ValidarEntrega : DefaultTask() {
    @get:Input abstract val faltantes: ListProperty<String>
    @get:Input abstract val urlExplicita: Property<Boolean>
    @get:Input abstract val host: Property<String>
    // Solo la ruta: este task nunca recibe ni imprime contraseñas.
    @get:Internal abstract val almacen: RegularFileProperty

    @TaskAction fun validar() {
        check(faltantes.get().isEmpty()) {
            "Completa keystore.properties localmente. Faltan: ${faltantes.get().joinToString()}. No subir secretos."
        }
        check(almacen.orNull?.asFile?.isFile == true) { "No se encuentra el almacén indicado en storeFile." }
        check(urlExplicita.get() && host.get() !in listOf("10.0.2.2", "localhost", "127.0.0.1", "[::1]")) {
            "Release requiere -PpharmaApiUrl=http://IP_LAN_DEL_PC:8080/api/v1/ o un servidor HTTPS accesible."
        }
    }
}

// Sin credenciales, sync y debug siguen disponibles. Release valida antes de compilar.
val firma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.isFile) archivo.inputStream().use { load(it) }
}
val clavesFirma = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
val faltantesFirma = clavesFirma.filter { firma.getProperty(it).isNullOrBlank() }
val rutaFirma = firma.getProperty("storeFile")?.takeIf { it.isNotBlank() }
val archivoFirma = rutaFirma?.let { rootProject.file(it) }
val urlConfigurada = providers.gradleProperty("pharmaApiUrl").orNull
val urlBackend = (urlConfigurada ?: "http://10.0.2.2:8080/api/v1/").trimEnd('/') + "/"
val uriBackend = URI(urlBackend)
require(uriBackend.scheme in listOf("http", "https") && !uriBackend.host.isNullOrBlank() &&
    uriBackend.userInfo == null && uriBackend.query == null && uriBackend.fragment == null &&
    !urlBackend.contains('"') && !urlBackend.contains('\\')) {
    "pharmaApiUrl debe ser una URL HTTP(S), sin credenciales, query ni fragmento."
}
val hostBackend = uriBackend.host
val redGenerada = layout.buildDirectory.dir("generated/pharmaNetwork/res")
val generarRedBackend = tasks.register<GenerarRedBackend>("generarRedBackend") {
    host.set(hostBackend)
    permitirHttp.set(uriBackend.scheme == "http")
    destino.set(redGenerada)
}
val validarEntrega = tasks.register<ValidarEntrega>("validarEntrega") {
    faltantes.set(faltantesFirma)
    urlExplicita.set(!urlConfigurada.isNullOrBlank())
    host.set(hostBackend)
    archivoFirma?.let { almacen.set(it) }
}
tasks.matching { it.name == "preBuild" }.configureEach { dependsOn(generarRedBackend) }
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(validarEntrega) }

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "pe.edu.upeu.pharmamobil"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pe.edu.upeu.pharmamobil"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "PHARMA_API_URL", "\"$urlBackend\"")
    }
    sourceSets.getByName("main").res.srcDir(redGenerada)
    signingConfigs {
        if (faltantesFirma.isEmpty()) {
            create("release") {
                storeFile = archivoFirma
                storePassword = firma.getProperty("storePassword")
                keyAlias = firma.getProperty("keyAlias")
                keyPassword = firma.getProperty("keyPassword")
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
