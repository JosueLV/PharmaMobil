package pe.edu.upeu.pharmamobil.domain.presentation.acercade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import org.koin.compose.koinInject
import pe.edu.upeu.pharmamobil.domain.port.Telemetria
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobil.platform.InfoDispositivo

import pe.edu.upeu.pharmamobil.platform.formatearSoles

@Composable
fun AcercaDeScreen() {
    val info = remember { InfoDispositivo() }
    val telemetria = koinInject<Telemetria>()
    var resultadoPrueba by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Acerca de", style = MaterialTheme.typography.titleMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Diagnóstico técnico", style = MaterialTheme.typography.titleSmall)
                Text(if (telemetria.disponible) "Firebase configurado" else "Firebase pendiente de configuración")
                Button(enabled = telemetria.disponible, onClick = {
                    telemetria.registrarEvento("prueba_integrador", mapOf("pantalla" to "diagnostico"))
                    telemetria.registrarError(IllegalStateException("Prueba controlada"),
                        mapOf("pantalla" to "diagnostico", "operacion" to "prueba"))
                    resultadoPrueba = "Prueba registrada localmente. Reinicia la app con conexión y comprueba Crashlytics."
                }) { Text("Registrar prueba técnica") }
                resultadoPrueba?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

                FilaInfo("Sistema operativo", info.sistema)
                FilaInfo("Versión", info.version)
                FilaInfo("Dispositivo", info.modelo)

                val ejemplo = formatearSoles(24.5)
                FilaInfo("formatearSoles(24.5)", ejemplo)
                FilaInfo("Código de cada carácter", ejemplo.map { it.code }.joinToString(" "))
            }
        }
    }
}

@Composable
private fun FilaInfo(etiqueta: String, valor: String) {
    Column {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}