package com.example.marketelectronico.ui.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.model.Message
import com.example.marketelectronico.data.remote.SocketManager
import kotlinx.coroutines.launch
import com.example.marketelectronico.data.repository.ChatRepository
import com.example.marketelectronico.data.remote.ApiClient
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.marketelectronico.data.remote.UserProfileDto
import com.example.marketelectronico.data.remote.UserService
import java.util.UUID
import com.example.marketelectronico.data.model.MessageStatus

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf<Message>()

    // Necesitamos saber quién es el usuario actual para pintar las burbujas
    private var currentUserId: Int = -1
    private var currentChatId: Int = -1

    private val repository = ChatRepository(ApiClient.chatApi)

    var chatPartner by mutableStateOf<UserProfileDto?>(null)
        private set

    // Se llama desde la UI al entrar a la pantalla
    fun initChat(chatId: Int, userId: Int, token: String) {
        this.currentChatId = chatId
        this.currentUserId = userId

        // 1. Conectar Socket
        SocketManager.init(token)
        SocketManager.connect()
        SocketManager.joinChat(chatId)

        SocketManager.markMessagesAsRead(chatId)

        SocketManager.onMessagesReadUpdate {
            // Recorremos la lista y marcamos MIS mensajes enviados como LEÍDOS
            messages.forEachIndexed { index, msg ->
                if (msg.isSentByMe && msg.status == MessageStatus.SENT) {
                    messages[index] = msg.copy(status = MessageStatus.READ)
                }
            }
        }

        // 2. Cargar Historial (HTTP)
        loadHistory(chatId)

        // 3. Escuchar nuevos mensajes
        SocketManager.onMessageReceived { message ->

            // --- CORRECCIÓN ANTIDUPLICADOS ---
            // Si el mensaje lo envié yo, LO IGNORO, porque ya lo agregué
            // manualmente en la función sendMessage() para mostrar "Enviando..."
            if (message.senderId == currentUserId.toString()) {
                return@onMessageReceived
            }
            // ---------------------------------

            // Si llegamos aquí, es un mensaje de la OTRA persona
            val adjustedMessage = message.copy(
                isSentByMe = false // Aseguramos que no es mío
            )

            // Agregamos a la lista si no existe
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

    fun loadChatPartner(userId: Int) {
        viewModelScope.launch {
            try {
                val response = UserService.api.getUserById(userId.toLong())
                if (response.ok) {
                    chatPartner = response.user
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isNotBlank() && currentChatId != -1) {

            // 1. Crear ID temporal y mensaje en estado SENDING
            val tempId = UUID.randomUUID().toString()
            val tempMessage = Message(
                id = tempId,
                text = text,
                isSentByMe = true,
                senderId = currentUserId.toString(),
                status = MessageStatus.SENDING // <-- Estado inicial
            )

            // 2. Agregar a la lista INMEDIATAMENTE (para que el usuario lo vea)
            messages.add(tempMessage)

            // 3. Enviar al servidor con callback
            SocketManager.sendMessage(currentChatId, text) { success ->
                if (success) {
                    // 4. Si el servidor confirma, buscamos el mensaje y lo actualizamos a SENT
                    // Necesitamos buscar por el ID temporal porque el servidor generará uno nuevo real,
                    // pero para la UI basta con actualizar el estado visualmente.
                    val index = messages.indexOfFirst { it.id == tempId }
                    if (index != -1) {
                        // Reemplazamos el elemento para gatillar la recomposición de Compose
                        messages[index] = messages[index].copy(status = MessageStatus.SENT)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.disconnect()
    }
}