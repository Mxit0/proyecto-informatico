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

    @DELETE("api/foros/{id}")
    suspend fun deleteForum(@Path("id") id: Int): GenericResponse

    @DELETE("api/foros/publicaciones/{id}")
    suspend fun deletePost(@Path("id") id: Int): GenericResponse

    @PUT("api/foros/{id}")
    suspend fun updateForum(
        @Path("id") id: Int,
        @Body request: UpdateForoRequest
    ): ForoDetailResponse

    @PUT("api/foros/publicaciones/{id}")
    suspend fun updatePost(
        @Path("id") id: Int,
        @Body request: UpdatePublicacionRequest
    ): CreatePublicacionResponse
}

object ForumService {
    val api: ForumApi by lazy {
        ApiClient.retrofit.create(ForumApi::class.java)
    }
}