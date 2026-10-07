package pe.edu.upeu.pharmamobil.domain.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.comoTextoParaCompartir

sealed interface DetalleEstado {
    data object Cargando : DetalleEstado
    data class Listo(val producto: ProductoUi) : DetalleEstado
    data class Error(val mensaje: String) : DetalleEstado
}

class DetalleProductoViewModel(
    private val repository: ProductoRepository,
    private val compartidor: Compartidor
) : ViewModel() {

    private var productoActual: Producto? = null

    private val _estado = MutableStateFlow<DetalleEstado>(DetalleEstado.Cargando)
    val estado: StateFlow<DetalleEstado> = _estado.asStateFlow()

    fun cargar(id: Long) {
        viewModelScope.launch {
            _estado.value = DetalleEstado.Cargando
            try {
                val producto = repository.listar().firstOrNull { it.id == id }
                if (producto == null) {
                    _estado.value = DetalleEstado.Error("Producto no encontrado")
                } else {
                    productoActual = producto
                    _estado.value = DetalleEstado.Listo(producto.toUi())
                }
            } catch (e: Exception) {
                _estado.value = DetalleEstado.Error(e.message ?: "Error al cargar el producto")
            }
        }
    }

    fun compartir() {
        productoActual?.let { compartidor.compartir(it.comoTextoParaCompartir()) }
    }
}

