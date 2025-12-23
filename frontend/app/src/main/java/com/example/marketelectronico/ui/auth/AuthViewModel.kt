package com.example.marketelectronico.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.AuthRepository
import com.example.marketelectronico.data.repository.UserRepository
import com.example.marketelectronico.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui

    var nombre by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isRegisterTabSelected by mutableStateOf(false)

    fun login(onSuccess: (token: String) -> Unit) {
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)

            val (token, error) = authRepo.login(email, password)

            if (token != null) {
                val userProfile = userRepo.getUserProfile()

                if (userProfile != null) {
                    onSuccess(token)
                    _ui.value = AuthUiState(loading = false)
                } else {
                    _ui.value = AuthUiState(loading = false, error = "Error al cargar perfil de usuario")
                }
            } else {
                _ui.value = AuthUiState(loading = false, error = error ?: "Error de login")
            }
        }
    }

    fun register(onSuccess: (token: String) -> Unit) {
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            val (token, error) = authRepo.register(nombre, email, password)

            if (token != null) {
                userRepo.getUserProfile()
                onSuccess(token)
                _ui.value = AuthUiState(loading = false)
            } else {
                _ui.value = AuthUiState(loading = false, error = error ?: "Error de registro")
            }
        }
    }

    fun clearError() {
        _ui.value = AuthUiState(loading = false, error = null)
    }
}

class AuthViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepo = AuthRepository()
        val userRepo = UserRepository.getInstance()
        return AuthViewModel(authRepo, userRepo) as T
    }
}