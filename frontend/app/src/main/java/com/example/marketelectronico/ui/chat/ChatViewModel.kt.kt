package com.example.marketelectronico.ui.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.model.Message
import com.example.marketelectronico.data.remote.ChatApi // Tu interfaz Retrofit
import com.example.marketelectronico.data.remote.SocketManager
import kotlinx.coroutines.launch
import com.example.marketelectronico.data.repository.ChatRepository
import com.example.marketelectronico.data.remote.ApiClient

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf<Message>()

    // Necesitamos saber quién es el usuario actual para pintar las burbujas
    private var currentUserId: Int = -1
    private var currentChatId: Int = -1

    private val repository = ChatRepository(ApiClient.chatApi)

    // Se llama desde la UI al entrar a la pantalla
    fun initChat(chatId: Int, userId: Int, token: String) {
        this.currentChatId = chatId
        this.currentUserId = userId

        // 1. Conectar Socket
        SocketManager.init(token)
        SocketManager.connect()
        SocketManager.joinChat(chatId)

        // 2. Cargar Historial (HTTP)
        loadHistory(chatId)

        // 3. Escuchar nuevos mensajes
        SocketManager.onMessageReceived { message ->
            // Ajustar isSentByMe basado en el senderId que viene del socket
            val adjustedMessage = message.copy(
                isSentByMe = (message.senderId == currentUserId.toString())
            )
            // Agregamos a la lista (Compose detecta el cambio)
            // Verificamos duplicados por si acaso
            if (messages.none { it.id == adjustedMessage.id }) {
                messages.add(adjustedMessage)
            }
        }
    }

    private fun loadHistory(chatId: Int) {
        viewModelScope.launch {
            try {
                // LLAMADA REAL AL REPOSITORIO
                val history = repository.getMessages(chatId)

                if (history != null) {
                    messages.clear()
                    // Marcamos cuáles son míos comparando con currentUserId
                    val processedHistory = history.map { msg ->
                        msg.copy(isSentByMe = msg.senderId == currentUserId.toString())
                    }
                    messages.addAll(processedHistory)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isNotBlank() && currentChatId != -1) {
            // Enviar al socket
            SocketManager.sendMessage(currentChatId, text)

            // Opcional: Agregar optimísticamente a la lista UI
            // (o esperar a que el socket devuelva el evento "new_message")
        }
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.disconnect()
    }
}