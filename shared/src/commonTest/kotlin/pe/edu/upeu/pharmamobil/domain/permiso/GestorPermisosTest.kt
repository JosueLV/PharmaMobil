package pe.edu.upeu.pharmamobil.domain.permiso

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.domain.presentation.permiso.PermisosViewModel
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GestorPermisosTest {
    @Test
    fun primeraSolicitud_noEsDenegacionPermanente() {
        assertEquals(EstadoPermiso.Denegado, resolverEstadoPermiso(false, false, false))
    }

    @Test
    fun denegadoUnaVez_permiteExplicacionYReintento() {
        assertEquals(EstadoPermiso.Denegado, resolverEstadoPermiso(false, true, true))
    }

    @Test
    fun denegadoDefinitivamente_requiereAjustes() {
        assertEquals(EstadoPermiso.DenegadoParaSiempre, resolverEstadoPermiso(false, true, false))
    }

    @Test
    fun concedido_tienePrioridadSobreLaBanderaAnterior() {
        assertEquals(EstadoPermiso.Concedido, resolverEstadoPermiso(true, true, false))
    }

    @Test
    fun viewModelUsaElGestorFalso_yActualizaAlVolverDeAjustes() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val gestor = GestorFalso()
            val vm = PermisosViewModel(gestor)
            vm.consultar()
            testScheduler.runCurrent()
            assertEquals(EstadoPermiso.Denegado, vm.estado.value.estado)
            vm.solicitar()
            vm.solicitar() // Un toque doble no abre dos solicitudes.
            testScheduler.runCurrent()
            assertEquals(1, gestor.solicitudes)
            assertEquals(EstadoPermiso.DenegadoParaSiempre, vm.estado.value.estado)
            vm.abrirAjustes()
            assertEquals(1, gestor.aperturas)
            gestor.actual = EstadoPermiso.Concedido
            vm.consultar()
            testScheduler.runCurrent()
            assertEquals(EstadoPermiso.Concedido, vm.estado.value.estado)
        } finally { Dispatchers.resetMain() }
    }

    private class GestorFalso : GestorPermisos {
        var actual: EstadoPermiso = EstadoPermiso.Denegado
        var solicitudes = 0
        var aperturas = 0
        override suspend fun estado(permiso: Permiso) = actual
        override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
            solicitudes++
            actual = EstadoPermiso.DenegadoParaSiempre
            return actual
        }
        override fun abrirAjustes() { aperturas++ }
    }
}
