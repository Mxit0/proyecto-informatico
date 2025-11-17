package com.example.marketelectronico.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

// --- Interfaz (Sin cambios) ---

interface ProductApi {
    @GET("productos")
    suspend fun getAllProducts(): List<ProductResponse>

    @GET("productos/{id}")
    suspend fun getProductById(@Path("id") id: String): ProductResponse
}

// --- Modelos de Datos (DTOs) ---

data class ProductResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val categoria: String,
    @SerializedName("id_usuario")
    val idUsuario: Int

    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
    // Se eliminó la lista de imágenes que causaba el error 500
    // @SerializedName("producto_imagenes")
    // val imagenes: List<ImageResponse>
    // ----------------------------
)

// --- Ya no necesitamos esta clase ---
// data class ImageResponse( ... )

// --- Servicio (Sin cambios) ---

object ProductService {
    val api: ProductApi by lazy {
        ApiClient.retrofit.create(ProductApi::class.java)
    }
}