package com.example.marketelectronico.ui.check

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.repository.CompatibilityRepository
import com.example.marketelectronico.data.repository.ProductRepository
import kotlinx.coroutines.launch

class CheckBySearchViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    // Lista completa de productos para buscar
    var allProducts by mutableStateOf<List<Product>>(emptyList())
        private set

    // Estado de la verificación
    var isChecking by mutableStateOf(false)
    var checkResultTitle by mutableStateOf("")
    var checkResultMessage by mutableStateOf("")
    var showResultDialog by mutableStateOf(false)

    // Productos seleccionados
    var selectedProduct1 by mutableStateOf<Product?>(null)
    var selectedProduct2 by mutableStateOf<Product?>(null)

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            // Cargamos todos los productos para poder filtrar localmente
            allProducts = productRepository.getAllProducts()
        }
    }

    fun selectProduct1(product: Product?) {
        selectedProduct1 = product
    }

    fun selectProduct2(product: Product?) {
        selectedProduct2 = product
    }

    fun checkCompatibility() {
        val p1 = selectedProduct1
        val p2 = selectedProduct2

        if (p1 == null || p2 == null) {
            checkResultTitle = "Faltan datos"
            checkResultMessage = "Por favor selecciona dos productos para comparar."
            showResultDialog = true
            return
        }

        viewModelScope.launch {
            isChecking = true
            try {
                // Reutilizamos el repositorio de compatibilidad existente
                // Enviamos una lista con los dos productos seleccionados
                val response = CompatibilityRepository.check(listOf(p1, p2))

                if (response.isSuccessful && response.body() != null) {
                    val wrapper = response.body()!!
                    if (wrapper.success && wrapper.data != null) {
                        val data = wrapper.data
                        if (data.compatible) {
                            checkResultTitle = "¡Son Compatibles!"
                            checkResultMessage = "Estos componentes deberían funcionar bien juntos."
                        } else {
                            checkResultTitle = "Incompatibilidad Detectada"
                            checkResultMessage = data.explanation
                                ?: data.issues?.joinToString("\n")
                                        ?: "Hay problemas de compatibilidad entre estos productos."
                        }
                    } else {
                        checkResultTitle = "Error"
                        checkResultMessage = wrapper.error ?: "Respuesta desconocida del servidor"
                    }
                } else {
                    checkResultTitle = "Error de Servidor"
                    checkResultMessage = "Código: ${response.code()}"
                }
            } catch (e: Exception) {
                checkResultTitle = "Error"
                checkResultMessage = e.message ?: "Ocurrió un error inesperado"
            } finally {
                isChecking = false
                showResultDialog = true
            }
        }
    }

    fun dismissDialog() {
        showResultDialog = false
    }
}