package com.example.marketelectronico.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.marketelectronico.data.model.Product

// --- Modelo de datos para Métodos de Pago ---
// Lo ponemos aquí para que sea accesible desde PaymentScreen y AddPaymentMethodScreen
data class PaymentMethod(
    val id: String,
    val alias: String,
    val type: String, // "Visa", "Mastercard"
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

    // Lista de métodos de pago predeterminados
    private val defaultPaymentMethods = listOf(
        PaymentMethod("1", "Tarjeta Principal", "Visa", "4242", "Jorge Campusano"),
        PaymentMethod("2", "Tarjeta del Banco", "Mastercard", "1234", "Jorge Campusano")
    )

    // Lista observable de métodos de pago
    val paymentMethods = mutableStateListOf<PaymentMethod>()

    // Bloque de inicialización para cargar los métodos por defecto
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
