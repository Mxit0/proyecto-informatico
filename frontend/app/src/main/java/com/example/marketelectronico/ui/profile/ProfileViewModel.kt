package com.example.marketelectronico.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.remote.UserProfileDto
import com.example.marketelectronico.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.marketelectronico.data.repository.ProductRepository
import com.example.marketelectronico.data.model.Product

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository.getInstance()

    private val _userProfile = MutableStateFlow<UserProfileDto?>(null)
    val userProfile: StateFlow<UserProfileDto?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _userProducts = MutableStateFlow<List<Product>>(emptyList())
    val userProducts: StateFlow<List<Product>> = _userProducts

    private val productRepository = ProductRepository()

    init {
        loadUserProfile()
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
                // Si cargó el perfil, también cargamos sus publicaciones
                profile?.let {
                    loadUserProducts(it.id_usuario.toInt())
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar perfil"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserProducts(userId: Int) {
        viewModelScope.launch {
            try {
                val products = productRepository.getProductsByUser(userId)
                _userProducts.value = products
            } catch (e: Exception) {
                e.printStackTrace()
                _userProducts.value = emptyList()
            }
        }
    }
}
