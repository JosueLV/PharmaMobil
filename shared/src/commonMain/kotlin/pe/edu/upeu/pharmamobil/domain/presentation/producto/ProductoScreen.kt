package pe.edu.upeu.pharmamobil.domain.presentation.producto

import pe.edu.upeu.pharmamobil.domain.model.Producto
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
@Composable
fun ProductoScreen() {

    var nombre by remember {
        mutableStateOf("")
    }

    var precio by remember {
        mutableStateOf("")
    }

    var stock by remember {
        mutableStateOf("")
    }

    var nombreError by remember {
        mutableStateOf<String?>(null)
    }

    var precioError by remember {
        mutableStateOf<String?>(null)
    }

    var stockError by remember {
        mutableStateOf<String?>(null)
    }

    var mensajeExito by remember {
        mutableStateOf<String?>(null)
    }

    fun validar(): Boolean {
        nombreError = if (nombre.isBlank()) "El nombre es obligatorio" else null

        val precioValor = precio.toDoubleOrNull()
        precioError = when {
            precio.isBlank() -> "El precio es obligatorio"
            precioValor == null -> "El precio debe ser un número válido"
            precioValor <= 0 -> "El precio debe ser mayor a 0"
            else -> null

        }

        val stockValor = stock.toIntOrNull()
        stockError = when {
            stock.isBlank() -> "El stock es obligatorio"
            stockValor == null -> "El stock debe ser un número entero"
            stockValor < 0 -> "El stock no puede ser negativo"
            else -> null
        }
        return nombreError == null && precioError == null && stockError == null
    }

    var tabSeleccionada by remember { mutableStateOf(0) }
    val titulosTabs = listOf("Activos", "Inactivos", "Bajo Stock")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("PharmaMobil")
        Text("Registro de Producto")

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = {
                Text("Nombre")
            },
            isError = nombreError != null,
            supportingText = {
                nombreError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = {
                Text("Precio")
            },
            isError = precioError != null,
            supportingText = {
                precioError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = stock,
            onValueChange = { stock = it },
            label = {
                Text("Stock")
            },
            isError = stockError != null,
            supportingText = {
                stockError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                mensajeExito = null
                if (validar()) {
                    val producto = Producto(
                        id = 0L,
                        nombre = nombre,
                        precio = precio.toDouble(),
                        stock = stock.toInt()
                    )
                    mensajeExito = if (producto.stock == 0) {
                        "Producto \"${producto.nombre}\" registrado correctamente con stock 0"
                    } else {
                        "Producto \"${producto.nombre}\" registrado correctamente"
                    }
                    nombre = ""
                    precio = ""
                    stock = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        mensajeExito?.let {
            Text(it)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Inventario")

        ScrollableTabRow(selectedTabIndex = tabSeleccionada) {
            titulosTabs.forEachIndexed { index, titulo ->
                Tab(
                    selected = tabSeleccionada == index,
                    onClick = { tabSeleccionada = index },
                    text = { Text(titulo) }
                )
            }
        }

        val productosFiltrados = when (tabSeleccionada) {
            0 -> productosMock.filter { it.activo && it.stock > 5 }       // Activos
            1 -> productosMock.filter { !it.activo }                      // Inactivos
            else -> productosMock.filter { it.stock <= 5 }                // Bajo Stock
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(productosFiltrados) { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(producto.nombre)
                        Text("Precio: S/ ${producto.precio}  |  Stock: ${producto.stock}")
                    }
                }
            }
        }
}

}
