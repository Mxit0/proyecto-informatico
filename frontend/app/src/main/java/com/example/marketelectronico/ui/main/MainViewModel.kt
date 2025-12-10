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

class MainViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<Int?>(null)
    val selectedCategory: StateFlow<Int?> = _selectedCategory.asStateFlow()

    init {
        fetchProducts()
        loadCategories()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            _uiState.value = ProductListUiState.Loading
            try {
                val products = productRepository.getAllProducts()
                android.util.Log.d("MainViewModel", "Productos obtenidos: ${products.size}")
                _uiState.value = ProductListUiState.Success(products)
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error: ${e.message}", e)
                _uiState.value = ProductListUiState.Error(e.message ?: "Error desconocido")
            }
        }
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
    
    fun selectCategory(categoryId: Int?) {
        _selectedCategory.value = categoryId
    }
}