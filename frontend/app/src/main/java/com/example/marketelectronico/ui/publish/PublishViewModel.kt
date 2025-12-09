package com.example.marketelectronico.ui.publish

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.model.Category
import com.example.marketelectronico.data.model.ComponenteMaestro
import com.example.marketelectronico.data.model.sampleComponentesMaestros
import com.example.marketelectronico.data.repository.ProductRepository
import com.example.marketelectronico.utils.TokenManager
import kotlinx.coroutines.delay
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
) : ViewModel() {

    private val _uiState = MutableStateFlow<PublishUiState>(PublishUiState.Idle)
    val uiState: StateFlow<PublishUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // --- 1. STATEFLOW PARA COMPONENTES MAESTROS ---
    private val _masterComponents = MutableStateFlow<List<ComponenteMaestro>>(emptyList())
    val masterComponents: StateFlow<List<ComponenteMaestro>> = _masterComponents.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                // En una app real, esto vendría del repositorio
                _categories.value = productRepository.getAllCategories()
            } catch (e: Exception) {
                _categories.value = emptyList() // Manejo de error
            }
        }
    }

    // --- 2. FUNCIÓN PARA CARGAR COMPONENTES POR CATEGORÍA ---
    fun loadMasterComponents(categoryId: Int) {
        viewModelScope.launch {
            _masterComponents.value = emptyList() // Limpiar antes de cargar
            try {
                // Llamar al repositorio para obtener componentes reales
                val componentes = productRepository.getComponentsByCategory(categoryId)
                _masterComponents.value = componentes
            } catch (e: Exception) {
                // Fallback: mantener vacía o usar datos de muestra si lo deseas
                _masterComponents.value = emptyList()
            }
        }
    }

    // --- 3. FUNCIÓN PARA LIMPIAR LA LISTA ---
    fun clearMasterComponents() {
        _masterComponents.value = emptyList()
    }

    // --- 4. ACTUALIZAR `publishProduct` ---
    fun publishProduct(
        nombre: String,
        precio: String,
        descripcion: String,
        categoriaId: Int?,
        masterComponentId: String?, // <-- NUEVO PARÁMETRO
        imageUris: List<android.net.Uri>,
        context: Context
    ) {
        viewModelScope.launch {
            _uiState.value = PublishUiState.Loading

            // Validaciones (existentes)
            if (nombre.isBlank()) {
                _uiState.value = PublishUiState.Error("El nombre del producto es requerido")
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

            // --- NUEVA VALIDACIÓN ---
            if (masterComponentId == null) {
                _uiState.value = PublishUiState.Error("Debes seleccionar un componente maestro")
                return@launch
            }

            if (imageUris.size < 3) {
                _uiState.value = PublishUiState.Error("Debes subir al menos 3 imágenes")
                return@launch
            }
            val userId = TokenManager.getUserId()
            if (userId == null) {
                _uiState.value = PublishUiState.Error("No estás autenticado.")
                return@launch
            }

            try {
                // La llamada al repositorio ahora debería incluir el ID del componente maestro.
                // Esto fallará hasta que actualicemos ProductRepository.
                val product = productRepository.createProduct(
                    nombre = nombre.trim(),
                    precio = precioValue,
                    descripcion = descripcion.trim(),
                    idUsuario = userId,
                    categoria = categoriaId,
                    stock = 1,
                    idComponenteMaestro = masterComponentId // <-- PASAR EL NUEVO DATO
                )

                if (product == null) {
                    _uiState.value = PublishUiState.Error("Error al crear el producto")
                    return@launch
                }

                val imagesUploaded = productRepository.uploadProductImages(
                    productId = product.id,
                    imageUris = imageUris,
                    context = context
                )

                if (imagesUploaded) {
                    _uiState.value = PublishUiState.Success
                } else {
                    _uiState.value = PublishUiState.Error("Error al subir las imágenes")
                }
            } catch (e: Exception) {
                _uiState.value = PublishUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() {
        _uiState.value = PublishUiState.Idle
    }
}
