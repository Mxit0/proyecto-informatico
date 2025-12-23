package com.example.marketelectronico.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.marketelectronico.data.model.Product


data class PaymentMethod(
    val id: String,
    val alias: String,
    val type: String,
    val lastFour: String,
    val cardholderName: String
)

/**
 * Repositorio (Singleton) para gestionar los métodos de pago.
 *
 * Mantiene una lista observable de los métodos de pago del usuario.
 * En una app real, esto se conectaría a Firebase/Firestore.
 */
object PaymentRepository {

    private val defaultPaymentMethods = listOf(
        PaymentMethod("1", "Tarjeta Principal", "Visa", "4242", "Jorge Campusano"),
        PaymentMethod("2", "Tarjeta del Banco", "Mastercard", "1234", "Jorge Campusano")
    )

    val paymentMethods = mutableStateListOf<PaymentMethod>()

    init {
        paymentMethods.addAll(defaultPaymentMethods)
    }

/**
     * Añade un nuevo método de pago a la lista.
     */
    fun addMethod(method: PaymentMethod) {
        paymentMethods.add(method)
    }
}
