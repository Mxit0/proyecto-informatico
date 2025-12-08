package com.example.marketelectronico.data.remote

import retrofit2.http.*

interface ForumApi {

    // 🟦 Listar foros
    @GET("api/foros")
    suspend fun getForums(): ForosListResponse

    // 🟦 Crear foro
    @POST("api/foros")
    suspend fun createForum(@Body request: CreateForoRequest): ForoDetailResponse

    // 🟦 Obtener detalle de un foro
    @GET("api/foros/{id}")
    suspend fun getForumById(@Path("id") id: String): ForoDetailResponse

    // 🟩 Listar publicaciones de un foro
    @GET("api/foros/{id}/publicaciones")
    suspend fun getForumPosts(@Path("id") id: String): PublicacionesListResponse

    // 🟩 Crear publicación (Esto dispara el Socket en el backend)
    @POST("api/foros/{id}/publicaciones")
    suspend fun createPost(
        @Path("id") id: String,
        @Body request: CreatePublicacionRequest
    ): CreatePublicacionResponse
}

// Singleton de acceso
object ForumService {
    val api: ForumApi by lazy {
        ApiClient.retrofit.create(ForumApi::class.java)
    }
}