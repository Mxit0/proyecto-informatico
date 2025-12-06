package com.example.marketelectronico.data.remote

import retrofit2.http.*
import okhttp3.MultipartBody
import com.google.gson.annotations.SerializedName

interface UserApi {
    @GET("usuarios/{id}")
    suspend fun getUserById(@Path("id") userId: Long): UserResponse

    @PATCH("usuarios/{id}/reputacion")
    suspend fun updateReputation(
        @Path("id") userId: Long,
        @Body request: ReputationRequest
    ): UserResponse

    @Multipart
    @PATCH("usuarios/{id}/foto")
    suspend fun updatePhoto(
        @Path("id") userId: Long,
        @Part image: MultipartBody.Part
    ): UserResponse
}

data class UserResponse(
    val ok: Boolean,
    val user: UserProfileDto?,
    val message: String?
)

data class UserProfileDto(
    @SerializedName("id_usuario")
    val id_usuario: Long,
    @SerializedName("nombre_usuario")
    val nombre_usuario: String,
    val correo: String,
    val foto: String?,
    val reputacion: Double?,
    @SerializedName("fecha_registro")
    val fecha_registro: String?
)

data class ReputationRequest(
    val calificacion: Double
)

object UserService {
    val api: UserApi by lazy {
        ApiClient.retrofit.create(UserApi::class.java)
    }
}
