package com.example.marketelectronico.ui.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.marketelectronico.data.model.Message
import com.example.marketelectronico.data.remote.SocketManager

class `ChatViewModel` : ViewModel() {

    // Lista observable de mensajes (Compose la vigila)
    val messages = mutableStateListOf<Message>()

    init {
        // 1. Iniciar y conectar el socket al crear el ViewModel
        SocketManager.init()
        SocketManager.connect()

        // 2. Escuchar mensajes entrantes
        SocketManager.onMessageReceived { message ->
            // Añadir a la lista (debe hacerse en el hilo principal si da error, pero Compose suele manejarlo)
            messages.add(message)
        }
    }

    fun sendMessage(text: String) {
        if (text.isNotBlank()) {
            // 1. Enviar al servidor
            SocketManager.sendMessage(text, "1") // "1" es tu ID temporal

            // 2. (Opcional) Añadirlo localmente de inmediato para que se vea rápido
            // O esperar a que el servidor lo devuelva (depende de la lógica de Joaquín)
        }
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.disconnect()
    }
}