package com.example.marketelectronico.data.repository

import com.example.marketelectronico.data.remote.ApiClient
import com.example.marketelectronico.data.remote.CompatibilityAPI
import com.example.marketelectronico.data.remote.CompatibilityRequest
import com.example.marketelectronico.data.remote.CompatibilityRequestItem
import com.example.marketelectronico.data.remote.CompatibilityResponseWrapper
import com.example.marketelectronico.data.model.Product
import retrofit2.Response

object CompatibilityRepository {
    private val api: CompatibilityAPI = ApiClient.retrofit.create(CompatibilityAPI::class.java)

    suspend fun check(cartProducts: List<Product>): Response<CompatibilityResponseWrapper> {
        val items = cartProducts.map { CompatibilityRequestItem(product_id = String.format("%s", it.id)) }
        val req = CompatibilityRequest(items = items)
        return api.checkCompatibility(req)
    }
}
