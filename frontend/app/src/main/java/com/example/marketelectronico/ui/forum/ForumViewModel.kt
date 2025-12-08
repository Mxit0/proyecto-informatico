package com.example.marketelectronico.ui.forum

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.remote.*
import com.example.marketelectronico.data.SocketManager // Asegúrate de tener este import
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

// Estados de la UI
sealed interface ForumsUiState {
    data object Loading : ForumsUiState
    data class Success(val foros: List<Foro>) : ForumsUiState
    data class Error(val message: String) : ForumsUiState
}

sealed interface ForumDetailUiState {
    data object Loading : ForumDetailUiState
    data class Success(
        val foro: Foro,
        val publicaciones: List<Publicacion>
    ) : ForumDetailUiState
    data class Error(val message: String) : ForumDetailUiState
}

class ForumViewModel : ViewModel() {

    // Estado Lista de Foros
    private val _forumsState = MutableStateFlow<ForumsUiState>(ForumsUiState.Loading)
    val forumsState = _forumsState.asStateFlow()

    // Estado Detalle (Foro + Posts)
    private val _detailState = MutableStateFlow<ForumDetailUiState>(ForumDetailUiState.Loading)
    val detailState = _detailState.asStateFlow()

    private val gson = Gson()
    private val socket = SocketManager.getSocket()

    init {
        fetchForums()
        setupSocketListener()
    }

    // --- API CALLS ---

    fun fetchForums() {
        viewModelScope.launch {
            _forumsState.value = ForumsUiState.Loading
            try {
                val response = ForumService.api.getForums()
                if (response.ok && response.foros != null) {
                    _forumsState.value = ForumsUiState.Success(response.foros)
                } else {
                    _forumsState.value = ForumsUiState.Error(response.error ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _forumsState.value = ForumsUiState.Error("Fallo de red: ${e.message}")
            }
        }
    }

    fun fetchForumDetail(foroId: String) {
        viewModelScope.launch {
            _detailState.value = ForumDetailUiState.Loading
            try {
                // 1. Obtener info del foro
                val foroRes = ForumService.api.getForumById(foroId)
                // 2. Obtener publicaciones
                val postsRes = ForumService.api.getForumPosts(foroId)

                if (foroRes.ok && foroRes.foro != null && postsRes.ok && postsRes.publicaciones != null) {
                    _detailState.value = ForumDetailUiState.Success(
                        foro = foroRes.foro,
                        publicaciones = postsRes.publicaciones
                    )
                    // Unirse a la sala de Socket para recibir notificaciones
                    joinForumRoom(foroId)
                } else {
                    _detailState.value = ForumDetailUiState.Error("No se pudo cargar el foro completo.")
                }
            } catch (e: Exception) {
                _detailState.value = ForumDetailUiState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    fun createPost(foroId: String, contenido: String) {
        viewModelScope.launch {
            try {
                // Enviamos a la API (Esto guardará en DB y el backend emitirá el socket)
                val req = CreatePublicacionRequest(contenido = contenido)
                ForumService.api.createPost(foroId, req)
                // No necesitamos añadirlo manual a la lista, el Socket lo hará
            } catch (e: Exception) {
                Log.e("ForumViewModel", "Error creating post: ${e.message}")
            }
        }
    }

    fun createForum(titulo: String, descripcion: String) {
        viewModelScope.launch {
            try {
                val req = CreateForoRequest(titulo, descripcion)
                val res = ForumService.api.createForum(req)
                if (res.ok) {
                    fetchForums() // Recargar lista
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- SOCKETS ---

    private fun joinForumRoom(foroId: String) {
        // Emitimos evento para unirnos a la sala específica del foro
        val data = JSONObject()
        data.put("room", "foro_$foroId") // Coincide con backend: io.to(`foro_${foroId}`)
        socket?.emit("join_chat", data)
    }

    private fun setupSocketListener() {
        // Escuchamos el evento exacto que definiste en el backend: "new_forum_post"
        socket?.on("new_forum_post") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as JSONObject
                    val newPost = gson.fromJson(data.toString(), Publicacion::class.java)

                    // Actualizar UI si estamos en la pantalla de detalle
                    val currentState = _detailState.value
                    if (currentState is ForumDetailUiState.Success) {
                        // Verificamos que el post sea de este foro (por seguridad)
                        if (currentState.foro.id == newPost.idForo) {
                            val updatedList = currentState.publicaciones + newPost
                            _detailState.value = currentState.copy(publicaciones = updatedList)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Socket", "Error parsing forum post: ${e.message}")
                }
            }
        }
    }
}