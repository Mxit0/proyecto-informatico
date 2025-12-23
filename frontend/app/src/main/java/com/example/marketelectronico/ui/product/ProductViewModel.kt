package com.example.marketelectronico.ui.product

import android.content.Context
import android.net.Uri
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
import com.example.marketelectronico.data.remote.UpdateProductRequest
import com.example.marketelectronico.utils.TokenManager
import com.example.marketelectronico.data.repository.Review
import com.example.marketelectronico.data.repository.ReviewRepository

sealed class ProductDetailUiState {
    data object Loading : ProductDetailUiState()
    data class Success(val product: Product) : ProductDetailUiState()
    data class Error(val message: String) : ProductDetailUiState()
}

/**
 * ViewModel para la pantalla de detalle (ProductScreen)
 */
class ProductViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<String>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val chatRepository: ChatRepository = ChatRepository(ApiClient.chatApi)

    private val _myExistingReview = MutableStateFlow<Review?>(null)
    val myExistingReview: StateFlow<Review?> = _myExistingReview

    private val _toastMessage = Channel<String>()
    val toastMessage = _toastMessage.receiveAsFlow()

    fun fetchProduct(productId: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _uiState.value = ProductDetailUiState.Loading
            }

            try {
                val product = productRepository.getProductById(productId)
                if (product != null) {
                    _uiState.value = ProductDetailUiState.Success(product)
                } else {
                    if (!isRefresh) _uiState.value = ProductDetailUiState.Error("Producto no encontrado")
                }
            } catch (e: Exception) {
                if (!isRefresh) _uiState.value = ProductDetailUiState.Error(e.message ?: "Error desconocido")
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

    fun deleteCurrentProduct(productId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val success = productRepository.deleteProduct(productId)
            if (success) {
                onSuccess()
            } else {
                Log.e("ProductVM", "Error al eliminar")
            }
        }
    }

    fun updateCurrentProduct(productId: String, name: String, desc: String, price: Double, stock: Int) {
        viewModelScope.launch {
            val request = UpdateProductRequest(
                nombre = name,
                description = desc,
                precio = price,
                stock = stock
            )
            val success = productRepository.updateProduct(productId, request)
            if (success) {
                fetchProduct(productId)
            } else {
                Log.e("ProductVM", "Error al actualizar")
            }
        }
    }

    fun checkIfReviewed(sellerId: Int) {
        val currentUserId = TokenManager.getUserId()?.toString() ?: return

            if (currentUserId == sellerId.toString()) return

        viewModelScope.launch {
            val review = ReviewRepository.checkUserReview(currentUserId, sellerId.toString())
            _myExistingReview.value = review
        }
    }

    fun updateUserReview(reviewId: String, rating: Double, comment: String, onSuccess: () -> Unit) {
        val currentUserId = TokenManager.getUserId()?.toString() ?: return
        viewModelScope.launch {
            val success = ReviewRepository.updateUserReview(reviewId, currentUserId, rating, comment)
            if (success) {
                val currentState = _uiState.value
                if (currentState is ProductDetailUiState.Success) {
                    checkIfReviewed(currentState.product.sellerId)
                }
                onSuccess()
            }
        }
    }

    fun uploadImages(productId: String, uris: List<Uri>, context: Context) {
        viewModelScope.launch {
            val success = productRepository.uploadProductImages(productId, uris, context)
            if (success) {
                fetchProduct(productId, isRefresh = true)
                _toastMessage.send("Imagen añadida correctamente")
            } else {
                _toastMessage.send("Error al subir imagen. Verifica el límite de 10.")
            }
        }
    }

    fun deleteImage(imageId: Int, productId: String) {
        viewModelScope.launch {
            try {
                val success = productRepository.deleteImage(imageId)
                if (success) {
                    fetchProduct(productId, isRefresh = true)
                    _toastMessage.send("Imagen eliminada")
                }
            } catch (e: Exception) {
                val msg = if (e.message?.contains("mínimo 3") == true)
                    "No puedes borrar: Mínimo 3 imágenes requeridas"
                else "Error al borrar imagen"
                _toastMessage.send(msg)
            }
        }
    }
}