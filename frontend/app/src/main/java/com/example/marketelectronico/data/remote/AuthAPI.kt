package com.example.marketelectronico.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse
}

data class LoginRequest(
    val correo: String,
    val password: String
)

data class LoginResponse(
    val ok: Boolean,
    val user: UserDto?,
    val token: String?,
    val message: String?
)

data class UserDto(
    val id_usuario: Long,
    val nombre_usuario: String,
    val correo: String,
    val foto: String?
)

object AuthService {
    val api: AuthApi by lazy {
        ApiClient.retrofit.create(AuthApi::class.java)
    }
}
