package com.example.marketelectronico.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.HTTP
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Query
interface ReviewAPI {

    @GET("api/reviews/product/{productId}")
    suspend fun getReviewsByProduct(@Path("productId") productId: String): Response<List<ReviewDto>>

    @GET("api/reviews/user/{userId}/history")
    suspend fun getUserReviewHistory(@Path("userId") userId: String): Response<List<ReviewDto>>

    @POST("api/reviews")
    suspend fun addReview(@Body request: CreateReviewRequest): Response<ReviewDto>

    @HTTP(method = "DELETE", path = "api/reviews/{reviewId}", hasBody = true)
    suspend fun deleteReview(@Path("reviewId") reviewId: String, @Body userIdWrapper: Map<String, String>): Response<Any>

    @POST("api/reviews/{reviewId}/like")
    suspend fun toggleLike(@Path("reviewId") reviewId: String, @Body userIdWrapper: Map<String, String>): Response<Any>

    @PUT("api/reviews/{reviewId}")
    suspend fun updateReview(@Path("reviewId") reviewId: String, @Body body: UpdateReviewRequest): Response<Any>

    @POST("api/reviews/user")
    suspend fun addUserReview(@Body request: CreateUserReviewRequest): Response<Any>

    @GET("api/reviews/user/{userId}")
    suspend fun getUserReviews(@Path("userId") userId: String): Response<List<UserReviewDto>>

    @GET("api/reviews/user/{userId}/average")
    suspend fun getUserRating(@Path("userId") userId: String): Response<UserRatingResponse>

    @DELETE("api/reviews/user/{reviewId}")
    suspend fun deleteUserReview(
        @Path("reviewId") reviewId: String,
        @Query("userId") userId: String // <--- Ahora viaja en la URL
    ): Response<Any>

    @PUT("api/reviews/user/{reviewId}")
    suspend fun updateUserReview(@Path("reviewId") reviewId: String, @Body body: UpdateReviewRequest): Response<Any>

    @GET("api/reviews/user/check")
    suspend fun checkUserReview(
        @Query("authorId") authorId: String,
        @Query("targetId") targetId: String
    ): Response<ReviewCheckResponse>
}