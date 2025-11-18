package com.example.marketelectronico.data.repository

import com.example.marketelectronico.data.remote.UserService
import com.example.marketelectronico.data.remote.UserProfileDto
import com.example.marketelectronico.utils.TokenManager
import android.util.Log

class UserRepository {
    private val userApi = UserService.api

    suspend fun getUserProfile(): UserProfileDto? {
        return try {
            val userId = TokenManager.getUserId()
            Log.d("UserRepository", "Obteniendo perfil del usuario: $userId")
            
            if (userId != null) {
                val response = userApi.getUserById(userId)
                Log.d("UserRepository", "Respuesta recibida: $response")
                response.user
            } else {
                Log.e("UserRepository", "No hay ID de usuario guardado")
                null
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al obtener perfil", e)
            e.printStackTrace()
            null
        }
    }

    companion object {
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository {
            return instance ?: UserRepository().also { instance = it }
        }
    }
}
