package com.example.marketelectronico.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui

    fun login(correo: String, password: String, onSuccess: (token: String) -> Unit) {
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            val (token, error) = repo.login(correo, password)
            _ui.value = if (token != null) {
                onSuccess(token)
                AuthUiState(loading = false)
            } else {
                AuthUiState(loading = false, error = error ?: "Error")
            }
        }
    }
}

class AuthViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = AuthRepository()
        return AuthViewModel(repo) as T
    }
}
