package com.example.marketelectronico.data.repository

import android.util.Log
import com.example.marketelectronico.data.remote.UserProfileDto
import com.example.marketelectronico.data.remote.UserService
import com.example.marketelectronico.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class UserRepository {
    private val userApi = UserService.api

    
    private val _currentUser = MutableStateFlow<UserProfileDto?>(null)
    val currentUser: StateFlow<UserProfileDto?> = _currentUser.asStateFlow()
    

    suspend fun getUserProfile(): UserProfileDto? {
        return try {
            val userId = TokenManager.getUserId()
            Log.d("UserRepository", "Obteniendo perfil del usuario: $userId")

            if (userId != null) {
                val response = userApi.getUserById(userId)
                Log.d("UserRepository", "Respuesta recibida: $response")

                
                if (response.ok && response.user != null) {
                    _currentUser.value = response.user
                }
                

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

    suspend fun uploadProfilePhoto(imageUri: Uri, context: Context) {
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver

            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            val inputStream = contentResolver.openInputStream(imageUri)
                ?: throw IllegalStateException("No se pudo abrir la imagen")

            val bytes = inputStream.use { it.readBytes() }

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val fileName = "profile_${System.currentTimeMillis()}.jpg"

            val part = MultipartBody.Part.createFormData(
                name = "photo",
                filename = fileName,
                body = requestBody
            )

            Log.d("UserRepository", "Subiendo foto de perfil: $fileName ($mimeType)")

            val response = userApi.uploadProfilePhoto(part)

            if (!response.isSuccessful) {
                throw Exception("Error subiendo foto: ${response.code()} ${response.message()}")
            }

            val body = response.body()
            Log.d("UserRepository", "Foto subida. URL recibida: ${body?.foto}")
        }
    }


    
    fun setUser(user: UserProfileDto?) {
        _currentUser.value = user
    }

    fun clearSession() {
        _currentUser.value = null
    
        TokenManager.clear()
    }

    companion object {
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository {
            return instance ?: UserRepository().also { instance = it }
        }
    }

    suspend fun updateFcmToken(token: String) {
        try {
            val userId = TokenManager.getUserId()
            if (userId != null) {
                
                val request = com.example.marketelectronico.data.remote.FcmTokenRequest(token)

                
                val response = userApi.saveFcmToken(request)

                if (response.isSuccessful) {
                    Log.d("UserRepository", "Token FCM actualizado en backend")
                } else {
                    Log.e("UserRepository", "Error actualizando token: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getUserById(userId: Long): com.example.marketelectronico.data.remote.UserResponse {
        return try {
            userApi.getUserById(userId)
        } catch (e: Exception) {
            com.example.marketelectronico.data.remote.UserResponse(
                ok = false,
                user = null,
                message = e.message
            )
        }
    }
}
