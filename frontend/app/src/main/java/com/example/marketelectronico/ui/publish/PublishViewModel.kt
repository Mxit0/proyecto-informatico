package com.example.marketelectronico.ui.publish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.marketelectronico.data.model.Category
import com.example.marketelectronico.data.repository.ProductRepository
import com.example.marketelectronico.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PublishUiState {
    data object Idle : PublishUiState()
    data object Loading : PublishUiState()
    data object Success : PublishUiState()
    data class Error(val message: String) : PublishUiState()
}

class PublishViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() { // Volver a ViewModel() normal

    private val _uiState = MutableStateFlow<PublishUiState>(PublishUiState.Idle)
    val uiState: StateFlow<PublishUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = productRepository.getAllCategories()
            } catch (e: Exception) {
                _categories.value = emptyList()
            }
        }
    }

    fun publishProduct(
        nombre: String,
        precio: String,
        descripcion: String,
        categoriaId: Int?,
        imageUris: List<android.net.Uri>,
        context: Context // Agregar contexto como parámetro
    ) {
        viewModelScope.launch {
            _uiState.value = PublishUiState.Loading

            // Validaciones
            if (nombre.isBlank()) {
                _uiState.value = PublishUiState.Error("El nombre del producto es requerido")
                return@launch
            }

            if (precio.isBlank()) {
                _uiState.value = PublishUiState.Error("El precio es requerido")
                return@launch
            }

            val precioValue = precio.toDoubleOrNull()
            if (precioValue == null || precioValue <= 0) {
                _uiState.value = PublishUiState.Error("El precio debe ser un número válido mayor a 0")
                return@launch
            }

            if (descripcion.isBlank()) {
                _uiState.value = PublishUiState.Error("La descripción es requerida")
                return@launch
            }

            if (categoriaId == null) {
                _uiState.value = PublishUiState.Error("Debes seleccionar una categoría")
                return@launch
            }

            // Validar imágenes
            if (imageUris.size < 3) {
                _uiState.value = PublishUiState.Error("Debes subir al menos 3 imágenes del producto")
                return@launch
            }

            val userId = TokenManager.getUserId()
            if (userId == null) {
                _uiState.value = PublishUiState.Error("No estás autenticado. Por favor, inicia sesión")
                return@launch
            }

            try {
                // 1. Crear el producto
                val product = productRepository.createProduct(
                    nombre = nombre.trim(),
                    precio = precioValue,
                    descripcion = descripcion.trim(),
                    idUsuario = userId,
                    categoria = categoriaId,
                    stock = 1
                )

                if (product == null) {
                    _uiState.value = PublishUiState.Error("Error al crear el producto")
                    return@launch
                }

                // 2. Subir imágenes usando URIs directamente
                val imagesUploaded = productRepository.uploadProductImages(
                    productId = product.id,
                    imageUris = imageUris,
                    context = context // Usar el contexto pasado como parámetro
                )

                if (imagesUploaded) {
                    _uiState.value = PublishUiState.Success
                } else {
                    _uiState.value = PublishUiState.Error("Error al subir las imágenes")
                }
            } catch (e: Exception) {
                _uiState.value = PublishUiState.Error(e.message ?: "Error desconocido al publicar el producto")
            }
        }
    }

    fun resetState() {
        _uiState.value = PublishUiState.Idle
    }
}

