package com.example.marketelectronico.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CategoryProductsUiState {
    data object Loading : CategoryProductsUiState()
    data class Success(val products: List<Product>) : CategoryProductsUiState()
    data class Error(val message: String) : CategoryProductsUiState()
}

class CategoryProductsViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoryProductsUiState>(CategoryProductsUiState.Loading)
    val uiState: StateFlow<CategoryProductsUiState> = _uiState.asStateFlow()

    private val _categoryName = MutableStateFlow<String>("")
    val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    fun loadProductsByCategory(categoryId: Int) {
        viewModelScope.launch {
            _uiState.value = CategoryProductsUiState.Loading
            try {
                val products = productRepository.getProductsByCategory(categoryId)
                _uiState.value = CategoryProductsUiState.Success(products)
            } catch (e: Exception) {
                _uiState.value = CategoryProductsUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun setCategoryName(name: String) {
        _categoryName.value = name
    }
}