package pe.edu.upeu.pharmamobil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.presentation.acercade.AcercaDeScreen
import pe.edu.upeu.pharmamobil.domain.presentation.cliente.ClienteScreen
import pe.edu.upeu.pharmamobil.domain.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobil.domain.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobil.domain.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobil.domain.presentation.sesion.LoginScreen
import pe.edu.upeu.pharmamobil.domain.presentation.sesion.SesionViewModel
import pe.edu.upeu.pharmamobil.navigation.Screen
import pe.edu.upeu.pharmamobil.theme.PharmaMobilTheme

@Composable
fun App() {
    var darkTheme by remember { mutableStateOf(false) }

    val sesionViewModel = koinViewModel<SesionViewModel>()
    val estado by sesionViewModel.uiState.collectAsState()
    val sesion = estado.sesion

    PharmaMobilTheme(darkTheme = darkTheme) {
        when {
            // comprobando si ya habia una sesion guardada
            estado.iniciando -> PantallaCarga()

            // sin sesion: login (tambien aparece aqui el aviso de sesion expirada)
            sesion == null -> LoginScreen(
                estado = estado,
                onUsuarioChange = sesionViewModel::onUsuarioChange,
                onClaveChange = sesionViewModel::onClaveChange,
                onIniciarSesion = sesionViewModel::iniciarSesion
            )

            // con sesion: la app. Su estado (pantalla actual, menu) se reinicia al cerrar sesion.
            else -> PantallaPrincipal(
                sesion = sesion,
                darkTheme = darkTheme,
                onDarkThemeChange = { darkTheme = it },
                onCerrarSesion = sesionViewModel::cerrarSesion
            )
        }
    }
}

@Composable
private fun PantallaCarga() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun PantallaPrincipal(
    sesion: Sesion,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var pantallaActual by remember { mutableStateOf<Screen>(Screen.Inicio) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(sesion)

                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = pantallaActual is Screen.Inicio,
                    onClick = {
                        pantallaActual = Screen.Inicio
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") }
                )

                NavigationDrawerItem(
                    label = { Text("Productos") },
                    selected = pantallaActual is Screen.Productos,
                    onClick = {
                        pantallaActual = Screen.Productos
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Medication, contentDescription = "Productos") }
                )

                // la seccion solo se ofrece a quien tiene CLIENTES_LEER (el servidor responderia 403)
                if (sesion.puede("CLIENTES_LEER")) {
                    NavigationDrawerItem(
                        label = { Text("Clientes") },
                        selected = pantallaActual is Screen.Clientes,
                        onClick = {
                            pantallaActual = Screen.Clientes
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Clientes") }
                    )
                }

                NavigationDrawerItem(
                    label = { Text("Pedidos") },
                    selected = pantallaActual is Screen.Pedidos,
                    onClick = {
                        pantallaActual = Screen.Pedidos
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Pedidos") }
                )

                NavigationDrawerItem(
                    label = { Text("Acerca de") },
                    selected = pantallaActual is Screen.AcercaDe,
                    onClick = {
                        pantallaActual = Screen.AcercaDe
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Acerca de") }
                )

                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCerrarSesion()
                    },
                    icon = { Icon(Icons.Default.Close, contentDescription = "Cerrar sesión") }
                )

                Spacer(modifier = Modifier.padding(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Modo oscuro")
                    Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = tituloPantalla(pantallaActual)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    }
                )
            }
        ) { paddingValues ->

            when (pantallaActual) {

                Screen.Inicio -> InicioScreen()

                Screen.Productos -> {
                    Column(modifier = Modifier.padding(paddingValues)) {
                        val productoViewModel = koinViewModel<ProductoViewModel>()
                        // al entrar (o al volver a iniciar sesion) se recarga con el token vigente
                        LaunchedEffect(sesion.usuario) { productoViewModel.cargarProductos() }
                        ProductoScreen(
                            viewModel = productoViewModel,
                            // el boton de registrar (y editar/eliminar) solo con CATALOGO_ESCRIBIR
                            puedeEscribir = sesion.puede("CATALOGO_ESCRIBIR")
                        )
                    }
                }

                Screen.Clientes -> {
                    Column(modifier = Modifier.padding(paddingValues)) {
                        ClienteScreen()
                    }
                }

                Screen.Pedidos -> {
                    Column(modifier = Modifier.padding(paddingValues)) {
                        Text("Pantalla de pedidos en construcción")
                    }
                }

                Screen.AcercaDe -> {
                    Column(modifier = Modifier.padding(paddingValues)) {
                        AcercaDeScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerHeader(sesion: Sesion) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "PharmaMobil",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = sesion.nombreCompleto,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "@${sesion.usuario}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun tituloPantalla(screen: Screen): String = when (screen) {
    Screen.Inicio -> "Inicio"
    Screen.Productos -> "Productos"
    Screen.Clientes -> "Clientes"
    Screen.Pedidos -> "Pedidos"
    Screen.AcercaDe -> "Acerca de"
}

