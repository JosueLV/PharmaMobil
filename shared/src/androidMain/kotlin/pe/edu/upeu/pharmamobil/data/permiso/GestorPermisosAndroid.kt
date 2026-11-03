package pe.edu.upeu.pharmamobil.data.permiso

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso
import pe.edu.upeu.pharmamobil.domain.permiso.resolverEstadoPermiso

class GestorPermisosAndroid(private val contexto: Context) : GestorPermisos {
    private val preferencias = contexto.getSharedPreferences("permisos_nativos", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    var puente: PuentePermisos? = null

    override suspend fun estado(permiso: Permiso): EstadoPermiso = withContext(Dispatchers.Main.immediate) {
        val nombre = nombreDeSistema(permiso)
        val concedido = ContextCompat.checkSelfPermission(contexto, nombre) == PackageManager.PERMISSION_GRANTED
        if (concedido) return@withContext EstadoPermiso.Concedido
        val actividad = puente?.actividad ?: return@withContext EstadoPermiso.Denegado
        resolverEstadoPermiso(false, preferencias.getBoolean("pedido_$nombre", false),
            ActivityCompat.shouldShowRequestPermissionRationale(actividad, nombre))
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso = mutex.withLock {
        withContext(Dispatchers.Main.immediate) {
            val anterior = estado(permiso)
            if (anterior != EstadoPermiso.Denegado) return@withContext anterior
            val actual = puente ?: return@withContext EstadoPermiso.Denegado
            val nombre = nombreDeSistema(permiso)
            actual.pedir(nombre) { preferencias.edit().putBoolean("pedido_$nombre", true).apply() }
            estado(permiso)
        }
    }

    override fun abrirAjustes() {
        contexto.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${contexto.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun nombreDeSistema(permiso: Permiso): String = when (permiso) {
        Permiso.CAMARA -> Manifest.permission.CAMERA
    }
}
