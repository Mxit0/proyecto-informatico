package com.example.marketelectronico.ui.product

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.repository.ProductRepository
import com.example.marketelectronico.data.repository.ChatRepository
import com.example.marketelectronico.data.remote.ApiClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Estado para la pantalla de detalle
sealed class ProductDetailUiState {
    data object Loading : ProductDetailUiState()
    data class Success(val product: Product) : ProductDetailUiState()
    data class Error(val message: String) : ProductDetailUiState()
}

/**
 * ViewModel para la pantalla de detalle (ProductScreen)
 */
class ProductViewModel(
    // Inyección manual de dependencias
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<String>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    // Repositorio de chat para el botón "Contactar"
    private val chatRepository: ChatRepository = ChatRepository(ApiClient.chatApi)

    fun fetchProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState.Loading
            try {
                // Esto ahora llama a la función mejorada del repositorio
                val product = productRepository.getProductById(productId)

                if (product != null) {
                    _uiState.value = ProductDetailUiState.Success(product)
                } else {
                    _uiState.value = ProductDetailUiState.Error("Producto no encontrado")
                }
            } catch (e: Exception) {
                _uiState.value = ProductDetailUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun contactSeller(sellerId: Int) {
        viewModelScope.launch {
            try {
                val chatDto = chatRepository.createOrGetChat(sellerId)

                if (chatDto != null) {
                    _navigationEvent.send("conversation/${chatDto.id}/$sellerId")
                } else {
                    Log.e("ProductVM", "Error: No se obtuvo el chat")
                }
            } catch (e: Exception) {
                Log.e("ProductVM", "Error al contactar vendedor", e)
            }
        }
    }
}