package pe.edu.upeu.pharmamobil.domain.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(lista = ProductoListaEstado.Cargando) }
            try {
                val productos = productoRepository.listar()
                _uiState.update {
                    it.copy(
                        lista = if (productos.isEmpty())
                            ProductoListaEstado.SinProductos
                        else
                            ProductoListaEstado.ConProductos(productos)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(lista = ProductoListaEstado.Error(e.message ?: "Error al cargar productos"))
                }
            }
        }
    }

    fun onNombreChange(valor: String) {
        _uiState.update { it.copy(formulario = it.formulario.copy(nombre = valor, nombreError = null)) }
    }

    fun onPrecioChange(valor: String) {
        _uiState.update { it.copy(formulario = it.formulario.copy(precio = valor, precioError = null)) }
    }

    fun onStockChange(valor: String) {
        _uiState.update { it.copy(formulario = it.formulario.copy(stock = valor, stockError = null)) }
    }

    fun registrar() {
        val formulario = _uiState.value.formulario

        viewModelScope.launch {
            _uiState.update {
                it.copy(formulario = it.formulario.copy(enviando = true, mensajeExito = null))
            }

            val resultado = registrarProductoUseCase(
                nombre = formulario.nombre,
                precio = formulario.precio,
                stock = formulario.stock
            )

            resultado.onSuccess { producto ->
                _uiState.update {
                    it.copy(
                        formulario = ProductoFormularioEstado(
                            mensajeExito = if (producto.stock == 0)
                                "Producto \"${producto.nombre}\" registrado correctamente con stock 0"
                            else
                                "Producto \"${producto.nombre}\" registrado correctamente"
                        )
                    )
                }
                cargarProductos()
            }.onFailure { error ->
                if (error is RegistrarProductoUseCase.CamposInvalidosException) {
                    _uiState.update {
                        it.copy(
                            formulario = it.formulario.copy(
                                enviando = false,
                                nombreError = error.errores.nombre,
                                precioError = error.errores.precio,
                                stockError = error.errores.stock
                            )
                        )
                    }
                } else {
                    _uiState.update { it.copy(formulario = it.formulario.copy(enviando = false)) }
                }
            }
        }
    }
}