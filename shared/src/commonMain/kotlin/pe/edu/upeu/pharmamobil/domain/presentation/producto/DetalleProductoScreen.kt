package pe.edu.upeu.pharmamobil.domain.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetalleProductoScreen(
    productoId: Long,
    onVolver: () -> Unit,
    viewModel: DetalleProductoViewModel = koinViewModel()
) {
    LaunchedEffect(productoId) { viewModel.cargar(productoId) }
    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onVolver) { Text("← Volver") }

        when (val e = estado) {
            is DetalleEstado.Cargando -> CircularProgressIndicator()
            is DetalleEstado.Error -> Text(e.mensaje, color = MaterialTheme.colorScheme.error)
            is DetalleEstado.Listo -> Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(e.producto.nombre, style = MaterialTheme.typography.titleLarge)
                    Text(e.producto.precio, style = MaterialTheme.typography.titleMedium)
                    Text("Stock: ${e.producto.stock} u.")

                    Button(onClick = { viewModel.compartir() }) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir")
                    }
                }
            }
        }
    }
}

