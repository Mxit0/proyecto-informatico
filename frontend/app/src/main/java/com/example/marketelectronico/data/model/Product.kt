package com.example.marketelectronico.data.model

/**
 * Este es el Modelo de Datos (Data Model) para un Producto.
 * Representa la "forma" de los datos que vendrán de tu API o base de datos.
 * [cite: MainActivity.kt]
 */
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String
)