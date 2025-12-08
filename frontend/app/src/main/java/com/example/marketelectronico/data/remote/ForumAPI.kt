package com.example.marketelectronico.data.remote

import retrofit2.http.*

interface ForumApi {
    @GET("api/foros")
    suspend fun getForums(): ForosListResponse

    @POST("api/foros")
    suspend fun createForum(@Body request: CreateForoRequest): ForoDetailResponse

    @GET("api/foros/{id}")
    suspend fun getForumById(@Path("id") id: Int): ForoDetailResponse

    @GET("api/foros/{id}/publicaciones")
    suspend fun getForumPosts(@Path("id") id: Int): PublicacionesListResponse

    @POST("api/foros/{id}/publicaciones")
    suspend fun createPost(
        @Path("id") id: Int,
        @Body request: CreatePublicacionRequest
    ): CreatePublicacionResponse
}

object ForumService {
    val api: ForumApi by lazy {
        ApiClient.retrofit.create(ForumApi::class.java)
    }
}