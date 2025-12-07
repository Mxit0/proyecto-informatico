package com.example.marketelectronico.ui.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.model.ChatPreview
import com.example.marketelectronico.data.remote.ApiClient
import com.example.marketelectronico.data.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatListViewModel : ViewModel() {

    private val repository = ChatRepository(ApiClient.chatApi)

    // Lista observable para la UI
    val chats = mutableStateListOf<ChatPreview>()

    // Se ejecuta al cargar el ViewModel
    init {
        loadChats()
    }

    // Función para recargar la lista (útil al volver a la pantalla)
    fun loadChats() {
        viewModelScope.launch {
            val fetchedChats = repository.getMyChats()
            chats.clear()
            chats.addAll(fetchedChats)
        }
    }
}
