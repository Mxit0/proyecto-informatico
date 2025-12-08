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

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository.getInstance()

    private val _userProfile = MutableStateFlow<UserProfileDto?>(null)
    val userProfile: StateFlow<UserProfileDto?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _userOrders = MutableStateFlow<List<Order>>(emptyList())
    val userOrders: StateFlow<List<Order>> = _userOrders

    init {
        loadUserProfile()
        loadUserOrders()
    }

    fun onNewProfileImageSelected(uri: Uri) {
        // por hacer: subida real al backend/Supabase
        Log.d("ProfileViewModel", "Nueva imagen de perfil seleccionada: $uri")
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
            // Filtramos la lista global buscando solo las que coincidan con el ID
            val myOrders = OrderRepository.orders.filter { it.userId == currentUserId }
            _userOrders.value = myOrders
        } else {
            _userOrders.value = emptyList()
        }
    }
}