package pe.edu.upeu.pharmamobil.domain.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarProductoUseCase: ActualizarProductoUseCase,
    private val eliminarProductoUseCase: EliminarProductoUseCase,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos(mostrarCargando: Boolean = true) {
        viewModelScope.launch {
            if (mostrarCargando) {
                _uiState.update { it.copy(lista = ProductoListaEstado.Cargando) }
            }
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

    fun iniciarEdicion(producto: Producto) {
        _uiState.update {
            it.copy(
                formulario = ProductoFormularioEstado(
                    idEditando = producto.id,
                    nombre = producto.nombre,
                    precio = producto.precio.toString(),
                    stock = producto.stock.toString(),
                    activo = producto.activo
                ),
                mensajeExito = null
            )
        }
    }

    fun cancelarEdicion() {
        _uiState.update { it.copy(formulario = ProductoFormularioEstado()) }
    }

    fun registrar() {
        val formulario = _uiState.value.formulario
        val idEditando = formulario.idEditando

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    formulario = it.formulario.copy(enviando = true),
                    operacion = Operacion.EnCurso(
                        tipo = if (idEditando == null) Operacion.Tipo.Crear else Operacion.Tipo.Actualizar,
                        productoId = idEditando
                    ),
                    mensajeExito = null
                )
            }

            val resultado = if (idEditando == null) {
                registrarProductoUseCase(formulario.nombre, formulario.precio, formulario.stock)
            } else {
                actualizarProductoUseCase(
                    id = idEditando,
                    nombre = formulario.nombre,
                    precio = formulario.precio,
                    stock = formulario.stock,
                    activo = formulario.activo
                )
            }

            resultado
                .onSuccess { producto ->
                    _uiState.update {
                        it.copy(
                            formulario = ProductoFormularioEstado(),
                            operacion = Operacion.Inactiva,
                            mensajeExito = if (idEditando == null)
                                "Producto \"${producto.nombre}\" registrado correctamente"
                            else
                                "Producto \"${producto.nombre}\" actualizado correctamente"
                        )
                    }
                    cargarProductos(mostrarCargando = false)
                }
                .onFailure { fallo -> manejarFalloFormulario(fallo) }
        }
    }

    fun eliminar(id: Long) = viewModelScope.launch {
        _uiState.update {
            it.copy(operacion = Operacion.EnCurso(Operacion.Tipo.Eliminar, id))
        }

        eliminarProductoUseCase(id)
            .onSuccess {
                cargarProductos(mostrarCargando = false)
                _uiState.update {
                    it.copy(operacion = Operacion.Inactiva, mensajeExito = "Producto eliminado")
                }
            }
            .onFailure { fallo ->
                val mensaje = if (fallo is ErrorApiException) mensajeDeError(fallo.error)
                else fallo.message ?: "No se pudo eliminar el producto"
                _uiState.update { it.copy(operacion = Operacion.Fallida(mensaje)) }
            }
    }

    private fun manejarFalloFormulario(fallo: Throwable) {
        when (fallo) {
            is RegistrarProductoUseCase.CamposInvalidosException -> {
                _uiState.update {
                    it.copy(
                        formulario = it.formulario.copy(
                            enviando = false,
                            nombreError = fallo.errores.nombre,
                            precioError = fallo.errores.precio,
                            stockError = fallo.errores.stock
                        ),
                        operacion = Operacion.Inactiva
                    )
                }
            }
            is ActualizarProductoUseCase.CamposInvalidosException -> {
                _uiState.update {
                    it.copy(
                        formulario = it.formulario.copy(
                            enviando = false,
                            nombreError = fallo.errores.nombre,
                            precioError = fallo.errores.precio,
                            stockError = fallo.errores.stock
                        ),
                        operacion = Operacion.Inactiva
                    )
                }
            }
            is ErrorApiException -> {
                val error = fallo.error
                if (error is ErrorApi.Validacion) {
                    _uiState.update {
                        it.copy(
                            formulario = it.formulario.copy(
                                enviando = false,
                                nombreError = error.porCampo["nombre"],
                                precioError = error.porCampo["precio"],
                                stockError = error.porCampo["stock"]
                            ),
                            operacion = Operacion.Inactiva
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            formulario = it.formulario.copy(enviando = false),
                            operacion = Operacion.Fallida(mensajeDeError(error))
                        )
                    }
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        formulario = it.formulario.copy(enviando = false),
                        operacion = Operacion.Fallida(fallo.message ?: "Ocurrió un error inesperado")
                    )
                }
            }
        }
    }

    private fun mensajeDeError(error: ErrorApi): String = when (error) {
        is ErrorApi.NoEncontrado -> "El producto ya no existe"
        is ErrorApi.Conflicto -> error.mensaje
        is ErrorApi.Servidor -> "Ocurrió un error en el servidor. Intenta nuevamente"
        is ErrorApi.SinConexion -> "No hay conexión con el servidor"
        is ErrorApi.TiempoAgotado -> "El servidor tardó demasiado en responder"
        is ErrorApi.Validacion -> "Hay campos del formulario con errores"
    }

    fun descartarError() {
        _uiState.update { it.copy(operacion = Operacion.Inactiva) }
    }
}