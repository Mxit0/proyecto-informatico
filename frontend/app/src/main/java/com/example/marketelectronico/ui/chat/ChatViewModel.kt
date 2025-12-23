package com.example.marketelectronico.ui.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.model.Message
import com.example.marketelectronico.data.remote.SocketManager
import com.example.marketelectronico.data.repository.ChatRepository
import com.example.marketelectronico.data.remote.ApiClient
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.marketelectronico.data.remote.UserProfileDto
import com.example.marketelectronico.data.remote.UserService
import java.util.UUID
import com.example.marketelectronico.data.model.MessageStatus
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf<Message>()

    private var currentUserId: Int = -1
    private var currentChatId: Int = -1
    private val repository = ChatRepository(ApiClient.chatApi)

    var chatPartner by mutableStateOf<UserProfileDto?>(null)
        private set

    fun initChat(chatId: Int, userId: Int, token: String) {
        this.currentChatId = chatId
        this.currentUserId = userId

        SocketManager.init(token)
        SocketManager.connect()
        SocketManager.joinChat(chatId)
        SocketManager.markMessagesAsRead(chatId)

        loadHistory(chatId)

        SocketManager.onMessageReceived { message ->
            if (message.senderId == currentUserId.toString()) {
                return@onMessageReceived
            }

            val incomingMsg = message.copy(isSentByMe = false)

            if (messages.none { it.id == incomingMsg.id }) {
                messages.add(0, incomingMsg)
                SocketManager.markMessagesAsRead(chatId)
            }
        }

        SocketManager.onMessagesReadUpdate {
            messages.forEachIndexed { index, msg ->
                if (msg.isSentByMe && msg.status == MessageStatus.SENT) {
                    messages[index] = msg.copy(status = MessageStatus.READ)
                }
            }
        }
    }

    private fun loadHistory(chatId: Int) {
        viewModelScope.launch {
            try {
                val history = repository.getMessages(chatId) // Viene [Viejo -> Nuevo]
                if (history != null) {
                    messages.clear()
                    val processedHistory = history.map { msg ->
                        msg.copy(isSentByMe = msg.senderId == currentUserId.toString())
                    }
                    messages.addAll(processedHistory.reversed())
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
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun sendMessage(text: String) {
        if (text.isNotBlank() && currentChatId != -1) {
            val tempId = UUID.randomUUID().toString()
            val tempMessage = Message(
                id = tempId,
                text = text,
                isSentByMe = true,
                senderId = currentUserId.toString(),
                status = MessageStatus.SENDING
            )

            messages.add(0, tempMessage)

            SocketManager.sendMessage(currentChatId, text) { success ->
                if (success) {
                    val index = messages.indexOfFirst { it.id == tempId }
                    if (index != -1) {
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