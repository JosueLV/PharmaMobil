@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package pe.edu.upeu.pharmamobil.data.permiso

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso
import kotlin.coroutines.resume

class GestorPermisosIos : GestorPermisos {
    override suspend fun estado(permiso: Permiso): EstadoPermiso = when (
        AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
    ) {
        AVAuthorizationStatusAuthorized -> EstadoPermiso.Concedido
        AVAuthorizationStatusNotDetermined -> EstadoPermiso.Denegado
        else -> EstadoPermiso.DenegadoParaSiempre
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso = withContext(Dispatchers.Main) {
        val anterior = estado(permiso)
        if (anterior != EstadoPermiso.Denegado) return@withContext anterior
        suspendCancellableCoroutine<Boolean> { continuacion ->
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { concedido ->
                if (continuacion.isActive) continuacion.resume(concedido)
            }
        }
        estado(permiso)
    }

    override fun abrirAjustes() {
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let {
            UIApplication.sharedApplication.openURL(it, emptyMap<Any?, Any?>(), null)
        }
    }
}
