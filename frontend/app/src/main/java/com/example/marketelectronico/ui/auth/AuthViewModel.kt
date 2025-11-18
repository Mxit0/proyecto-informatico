package com.example.marketelectronico.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repo: AuthRepository
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
            val (token, error) = repo.login(email, password)
            _ui.value = if (token != null) {
                onSuccess(token)
                AuthUiState(loading = false)
            } else {
                AuthUiState(loading = false, error = error ?: "Error de login")
            }
        }
    }

    fun register(onSuccess: (token: String) -> Unit) {
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            val (token, error) = repo.register(nombre, email, password)
            _ui.value = if (token != null) {
                onSuccess(token)
                AuthUiState(loading = false)
            } else {
                AuthUiState(loading = false, error = error ?: "Error de registro")
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
        val repo = AuthRepository()
        return AuthViewModel(repo) as T
    }
}