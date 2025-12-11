package com.example.marketelectronico.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class CompatibilityRequestItem(
    val product_id: String
)

data class CompatibilityRequest(
    val items: List<CompatibilityRequestItem>
)

data class CompatibilityPairDto(
    val a: Int,
    val b: Int,
    val compatible: Boolean,
    val explanation: String?
)

data class CompatibilityResultDto(
    val compatible: Boolean,
    val issues: List<String>?,
    val recommendations: List<String>?,
    val explanation: String?,
    val compatibility_score: Double?,
    val pairs: List<CompatibilityPairDto>?
)

data class CompatibilityResponseWrapper(
    val success: Boolean,
    val data: CompatibilityResultDto?,
    val error: String?
)

interface CompatibilityAPI {
    @POST("api/compatibility/check")
    suspend fun checkCompatibility(@Body req: CompatibilityRequest): Response<CompatibilityResponseWrapper>
}
