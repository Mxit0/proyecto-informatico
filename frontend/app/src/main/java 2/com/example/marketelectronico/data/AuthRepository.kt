package com.example.marketelectronico.data

import com.example.marketelectronico.data.remote.AuthService
import com.example.marketelectronico.data.remote.LoginRequest
// --- NOVEDAD: Importar el nuevo modelo de request ---
import com.example.marketelectronico.data.remote.RegisterRequest
import com.example.marketelectronico.utils.TokenManager

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
                // Guardar token e información del usuario
                TokenManager.saveToken(res.token)
                if (res.user != null) {
                    TokenManager.saveUserId(res.user.id_usuario)
                    TokenManager.saveUsername(res.user.nombre_usuario)
                }
                Pair(res.token, null)
            } else {
                Pair(null, res.message ?: "Correo o contraseña incorrectos")
            }
        } catch (e: Exception) {
            Pair(null, e.message ?: "Error de red")
        }
    }

    /**
     * Registra un nuevo usuario.
     * Si el registro es exitoso, llama a login() para obtener un token.
     * Devuelve Pair(token, mensajeDeError)
     */
    suspend fun register(nombre: String, correo: String, password: String): Pair<String?, String?> {
        return try {

            val req = RegisterRequest(nombre_usuario = nombre, correo = correo, password = password)

            val res = AuthService.api.register(req)

            if (res.ok) {
                return login(correo, password)
            } else {
                Pair(null, res.message ?: "No se pudo crear el usuario")
            }
        } catch (e: Exception) {
            Pair(null, e.message ?: "Error de red")
        }
    }
}