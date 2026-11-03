package pe.edu.upeu.pharmamobil.data.permiso

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class PuentePermisos(val actividad: ComponentActivity) {
    private val mutex = Mutex()
    private var pendiente: CompletableDeferred<Boolean>? = null
    private val lanzador = actividad.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        pendiente?.complete(it)
    }

    init {
        actividad.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                pendiente?.cancel()
                pendiente = null
            }
        })
    }

    suspend fun pedir(nombre: String, alLanzar: () -> Unit): Boolean = mutex.withLock {
        withContext(Dispatchers.Main.immediate) {
            val espera = CompletableDeferred<Boolean>()
            pendiente = espera
            try {
                lanzador.launch(nombre)
                alLanzar()
                espera.await()
            } finally {
                pendiente = null
            }
        }
    }
}
