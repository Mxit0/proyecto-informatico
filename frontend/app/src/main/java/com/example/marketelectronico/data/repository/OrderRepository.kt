package com.example.marketelectronico.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.marketelectronico.data.model.Product
import java.util.Date
import java.util.UUID

// 1. Define qué es una "Orden"
data class Order(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val items: List<Product>,
    val date: Date = Date(),
    val totalAmount: Double
)

/**
 * Repositorio (Singleton) para gestionar el historial de órdenes completadas.
 * En una app real, esto estaría en una base de datos (Room, Firestore).
 */
object OrderRepository {

    // Lista observable de órdenes completadas
    val orders = mutableStateListOf<Order>()

    /**
     * Añade una nueva orden completada al historial.
     */
    fun addOrder(order: Order) {
        orders.add(0, order) // Añade al principio de la lista
    }

    /**
     * Busca una orden por su ID.
     */
    fun findOrderById(orderId: String?): Order? {
        return orders.find { it.id == orderId }
    }
}