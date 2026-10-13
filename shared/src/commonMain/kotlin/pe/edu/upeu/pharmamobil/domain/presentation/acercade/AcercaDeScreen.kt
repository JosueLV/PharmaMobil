package pe.edu.upeu.pharmamobil.domain.presentation.acercade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobil.platform.InfoDispositivo

import pe.edu.upeu.pharmamobil.platform.formatearSoles

@Composable
fun AcercaDeScreen() {
    val info = remember { InfoDispositivo() }

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