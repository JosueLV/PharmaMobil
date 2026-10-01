package pe.edu.upeu.pharmamobil.domain.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.presentation.components.EstadoVacio
import pe.edu.upeu.pharmamobil.domain.presentation.components.MensajeExito
import pe.edu.upeu.pharmamobil.domain.presentation.components.ValidatedTextField

@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val formulario = uiState.formulario
    val operacion = uiState.operacion

    var tabSeleccionada by remember { mutableStateOf(0) }
    val titulosTabs = listOf("Activos", "Inactivos", "Bajo Stock")

    val idEnCursoEliminando = (operacion as? Operacion.EnCurso)
        ?.takeIf { it.tipo == Operacion.Tipo.Eliminar }
        ?.productoId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        FormularioProductoCard(
            estaEditando = formulario.estaEditando,
            nombre = formulario.nombre,
            precio = formulario.precio,
            stock = formulario.stock,
            nombreError = formulario.nombreError,
            precioError = formulario.precioError,
            stockError = formulario.stockError,
            enviando = formulario.enviando,
            onNombreChange = viewModel::onNombreChange,
            onPrecioChange = viewModel::onPrecioChange,
            onStockChange = viewModel::onStockChange,
            onGuardar = viewModel::registrar,
            onCancelar = viewModel::cancelarEdicion
        )

        uiState.mensajeExito?.let {
            MensajeExito(it)
        }

        if (operacion is Operacion.Fallida) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = operacion.mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = viewModel::descartarError) {
                        Icon(imageVector = Icons.Default.CloudOff, contentDescription = "Cerrar")
                    }
                }
            }
        }

        Text(
            text = "Inventario",
            style = MaterialTheme.typography.titleMedium
        )

        ScrollableTabRow(selectedTabIndex = tabSeleccionada) {
            titulosTabs.forEachIndexed { index, titulo ->
                Tab(
                    selected = tabSeleccionada == index,
                    onClick = { tabSeleccionada = index },
                    text = { Text(titulo) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val estadoLista = uiState.lista) {

                is ProductoListaEstado.Cargando ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Cargando inventario…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                is ProductoListaEstado.SinProductos ->
                    EstadoVacio(
                        icono = Icons.Default.Inventory2,
                        titulo = "Todavía no hay productos",
                        descripcion = "Registra el primero con el formulario de arriba.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                is ProductoListaEstado.Error ->
                    EstadoVacio(
                        icono = Icons.Default.CloudOff,
                        titulo = "No pudimos cargar el inventario",
                        descripcion = estadoLista.mensaje,
                        colorIcono = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                        accion = {
                            FilledTonalButton(onClick = { viewModel.cargarProductos() }) {
                                Text("Reintentar")
                            }
                        }
                    )

                is ProductoListaEstado.ConProductos -> {
                    val productosFiltrados = when (tabSeleccionada) {
                        0 -> estadoLista.productos.filter { it.activo && it.stock > 5 }
                        1 -> estadoLista.productos.filter { !it.activo }
                        else -> estadoLista.productos.filter { it.stock <= 5 }
                    }

                    if (productosFiltrados.isEmpty()) {
                        EstadoVacio(
                            icono = Icons.Default.Inventory2,
                            titulo = "Nada por aquí",
                            descripcion = "No hay productos en esta pestaña.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = productosFiltrados,
                                key = { it.id }
                            ) { producto ->
                                ProductoItem(
                                    producto = producto,
                                    eliminando = idEnCursoEliminando == producto.id,
                                    onEditar = { viewModel.iniciarEdicion(producto) },
                                    onEliminar = { viewModel.eliminar(producto.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioProductoCard(
    estaEditando: Boolean,
    nombre: String,
    precio: String,
    stock: String,
    nombreError: String?,
    precioError: String?,
    stockError: String?,
    enviando: Boolean,
    onNombreChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (estaEditando) "Editar producto" else "Registrar producto",
                style = MaterialTheme.typography.titleMedium
            )

            ValidatedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                label = "Nombre",
                error = nombreError,
                leadingIcon = Icons.Default.Medication,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ValidatedTextField(
                    value = precio,
                    onValueChange = onPrecioChange,
                    label = "Precio",
                    error = precioError,
                    ayuda = "En soles",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )

                ValidatedTextField(
                    value = stock,
                    onValueChange = onStockChange,
                    label = "Stock",
                    error = stockError,
                    ayuda = "Unidades",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (estaEditando) {
                    OutlinedButton(
                        onClick = onCancelar,
                        enabled = !enviando,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                }

                Button(
                    onClick = onGuardar,
                    enabled = !enviando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        when {
                            enviando && estaEditando -> "Guardando…"
                            enviando -> "Registrando…"
                            estaEditando -> "Guardar cambios"
                            else -> "Registrar"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductoItem(
    producto: Producto,
    eliminando: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "S/ ${producto.precio}  ·  ${producto.stock} u.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (producto.requiereReposicion()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(
                        text = "Reponer",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (eliminando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                IconButton(onClick = onEditar) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onEliminar) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}