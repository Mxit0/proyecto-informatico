package com.example.marketelectronico.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.remote.UserProfileDto
import com.example.marketelectronico.data.repository.Order
import com.example.marketelectronico.data.repository.OrderRepository
import com.example.marketelectronico.data.repository.UserRepository
import com.example.marketelectronico.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.repository.ProductRepository


class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository.getInstance()
    private val productRepository = ProductRepository()

    private val _userProfile = MutableStateFlow<UserProfileDto?>(null)
    val userProfile: StateFlow<UserProfileDto?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _userOrders = MutableStateFlow<List<Order>>(emptyList())
    val userOrders: StateFlow<List<Order>> = _userOrders

    private val _myProducts = MutableStateFlow<List<Product>>(emptyList())
    val myProducts: StateFlow<List<Product>> = _myProducts

    init {
        loadUserProfile()
        loadUserOrders()
        loadMyProducts()
    }

    fun onNewProfileImageSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                Log.d("ProfileViewModel", "Nueva imagen de perfil seleccionada: $uri")

                // Subir la foto al backend / Supabase
                userRepository.uploadProfilePhoto(uri, context)

                // Al terminar, recargamos el perfil para traer la nueva URL en userProfile.foto
                loadUserProfile()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al actualizar la foto de perfil"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val profile = userRepository.getUserProfile()
                _userProfile.value = profile
                if (profile == null) {
                    _error.value = "No se pudo cargar el perfil"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar perfil"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserOrders() {
        val currentUserId = TokenManager.getUserId()?.toString()
        if (currentUserId != null) {
            viewModelScope.launch {
                try {
                    val ordersFromApi = OrderRepository.getUserOrders()
                    // Ahora comparamos String con String
                    _userOrders.value = ordersFromApi.filter { it.userId == currentUserId }
                } catch (e: Exception) {
                    _userOrders.value = emptyList()
                }
            }
        } else {
            _userOrders.value = emptyList()
        }
    }

    fun loadMyProducts() {
        val userId = TokenManager.getUserId()
        if (userId != null) {
            viewModelScope.launch {
                try {
                    val products = productRepository.getProductsByUser(userId)
                    _myProducts.value = products
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error cargando mis productos", e)
                }
            }
        }
    }
}
