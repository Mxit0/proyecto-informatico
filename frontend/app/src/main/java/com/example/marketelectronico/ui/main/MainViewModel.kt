package com.example.marketelectronico.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

// Estado para la lista de productos
sealed class ProductListUiState {
    data object Loading : ProductListUiState()
    data class Success(val products: List<Product>) : ProductListUiState()
    data class Error(val message: String) : ProductListUiState()
}

/**
 * ViewModel para la pantalla principal (MainScreen)
 */
class MainViewModel(
    // Inyectamos el repositorio que ya creamos
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    // Lista de categorías obtenida desde el backend
    private val _categories = MutableStateFlow<List<com.example.marketelectronico.data.model.Category>>(emptyList())
    val categories: StateFlow<List<com.example.marketelectronico.data.model.Category>> = _categories.asStateFlow()

    // init se llama en cuanto el ViewModel es creado
    init {
        fetchProducts()
        fetchCategories()
    }

    // Public refresh method para que otras pantallas soliciten recargar la lista
    fun refreshProducts() {
        fetchProducts()
    }

    /**
     * Llama al repositorio para obtener todos los productos
     * y actualiza el _uiState.
     */
    private fun fetchProducts() {
        viewModelScope.launch {
            _uiState.value = ProductListUiState.Loading
            try {
                // ¡Aquí está la llamada a la API!
                val products = productRepository.getAllProducts()
                _uiState.value = ProductListUiState.Success(products)
            } catch (e: Exception) {
                _uiState.value = ProductListUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val cats = productRepository.getCategories()
                _categories.value = cats
            } catch (e: Exception) {
                // deja la lista vacía si falla
                e.printStackTrace()
            }
        }
    }
}