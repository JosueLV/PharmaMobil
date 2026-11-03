package pe.edu.upeu.pharmamobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import org.koin.android.ext.android.inject
import pe.edu.upeu.pharmamobil.data.permiso.GestorPermisosAndroid
import pe.edu.upeu.pharmamobil.data.permiso.PuentePermisos

class MainActivity : ComponentActivity() {
    private val permisos: GestorPermisosAndroid by inject()
    private var puentePermisos: PuentePermisos? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        puentePermisos = PuentePermisos(this).also { permisos.puente = it }

        setContent {
            App()
        }
    }
    override fun onDestroy() {
        if (permisos.puente === puentePermisos) permisos.puente = null
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}