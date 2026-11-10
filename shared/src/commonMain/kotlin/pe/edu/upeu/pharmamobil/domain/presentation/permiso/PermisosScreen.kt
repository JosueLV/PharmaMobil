package pe.edu.upeu.pharmamobil.domain.presentation.permiso

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso

@Composable
fun PermisosScreen(viewModel: PermisosViewModel = koinViewModel()) {
    val estado by viewModel.estado.collectAsState()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.consultar() }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Permisos", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Cámara", style = MaterialTheme.typography.titleMedium)
                Text("Esta práctica comprueba el acceso a la cámara. Solo se solicita el permiso al pulsar el botón; no se toman ni envían fotos. Puedes seguir usando el inventario si lo rechazas.")
                Text(when (estado.estado) {
                    EstadoPermiso.Concedido -> "Concedido"
                    EstadoPermiso.Denegado -> "Denegado · puedes solicitarlo"
                    EstadoPermiso.DenegadoParaSiempre -> "Denegado permanentemente · habilítalo en Ajustes"
                    null -> "Consultando permiso…"
                }, color = MaterialTheme.colorScheme.primary)
                when (estado.estado) {
                    EstadoPermiso.Denegado -> Button(onClick = viewModel::solicitar, enabled = !estado.solicitando) {
                        Text(if (estado.solicitando) "Solicitando…" else "Solicitar permiso de cámara")
                    }
                    EstadoPermiso.DenegadoParaSiempre -> Button(onClick = viewModel::abrirAjustes) { Text("Abrir Ajustes") }
                    else -> Unit
                }
            }
        }
    }
}
