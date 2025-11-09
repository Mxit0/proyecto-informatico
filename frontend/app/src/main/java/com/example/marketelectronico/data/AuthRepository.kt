package com.example.marketelectronico.data

import com.example.marketelectronico.data.remote.AuthService
import com.example.marketelectronico.data.remote.LoginRequest

class AuthRepository {
    /**
     * Devuelve Pair(token, mensajeDeError)
     * - Si éxito: Pair(token, null)
     * - Si error: Pair(null, "mensaje")
     */
    suspend fun login(correo: String, password: String): Pair<String?, String?> {
        return try {
            val res = AuthService.api.login(LoginRequest(correo, password))
            if (res.ok && !res.token.isNullOrBlank()) {
                Pair(res.token, null)
            } else {
                Pair(null, res.message ?: "Correo o contraseña incorrectos")
            }
        } catch (e: Exception) {
            Pair(null, e.message ?: "Error de red")
        }
    }
}
