package com.example.marketelectronico.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.marketelectronico.data.model.Category


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
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    // init se llama en cuanto el ViewModel es creado
    init {
        fetchProducts()
        loadCategories()
    }
    
    /**
     * Carga las categorías desde el repositorio
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = productRepository.getAllCategories()
                _categories.value = categories
            } catch (e: Exception) {
                e.printStackTrace()
                // Si falla, simplemente dejamos la lista vacía
                _categories.value = emptyList()
            }
        }
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
}