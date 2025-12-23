package com.example.marketelectronico.ui.forum

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketelectronico.data.remote.*
import com.example.marketelectronico.utils.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed interface ForumsUiState {
    data object Loading : ForumsUiState
    data class Success(val foros: List<Foro>) : ForumsUiState
    data class Error(val message: String) : ForumsUiState
}

sealed interface ForumDetailUiState {
    data object Loading : ForumDetailUiState
    data class Success(val foro: Foro, val publicaciones: List<Publicacion>) : ForumDetailUiState
    data class Error(val message: String) : ForumDetailUiState
}

class ForumViewModel : ViewModel() {

    private val _forumsState = MutableStateFlow<ForumsUiState>(ForumsUiState.Loading)
    val forumsState = _forumsState.asStateFlow()

    private val _detailState = MutableStateFlow<ForumDetailUiState>(ForumDetailUiState.Loading)
    val detailState = _detailState.asStateFlow()

    private val gson = Gson()
    private val socket = SocketManager.getSocket()

    val currentUserId: Int
        get() = try {
            TokenManager.getUserId()?.toInt() ?: -1
        } catch (e: Exception) {
            -1
        }

    init {
        SocketManager.connect()
        fetchForums()
        setupSocketListener()
    }

    fun fetchForums() {
        viewModelScope.launch {
            _forumsState.value = ForumsUiState.Loading
            try {
                val res = ForumService.api.getForums()
                if (res.ok && res.foros != null) _forumsState.value = ForumsUiState.Success(res.foros)
                else _forumsState.value = ForumsUiState.Error(res.error ?: "Error desconocido")
            } catch (e: Exception) {
                _forumsState.value = ForumsUiState.Error("Error de red: ${e.message}")
            }
        }
    }

    fun fetchForumDetail(id: Int) {
        viewModelScope.launch {
            _detailState.value = ForumDetailUiState.Loading
            try {
                val fRes = ForumService.api.getForumById(id)
                val pRes = ForumService.api.getForumPosts(id)

                if (fRes.ok && fRes.foro != null && pRes.ok && pRes.publicaciones != null) {
                    _detailState.value = ForumDetailUiState.Success(fRes.foro, pRes.publicaciones)
                    joinForumRoom(id)
                } else {
                    _detailState.value = ForumDetailUiState.Error("No se pudo cargar el foro")
                }
            } catch (e: Exception) {
                _detailState.value = ForumDetailUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun createForum(titulo: String, desc: String) {
        viewModelScope.launch {
            try {
                val req = CreateForoRequest(titulo, desc, currentUserId)
                val res = ForumService.api.createForum(req)
                if (res.ok) fetchForums()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun createPost(foroId: Int, content: String) {
        viewModelScope.launch {
            try {
                val req = CreatePublicacionRequest(content, currentUserId)
                ForumService.api.createPost(foroId, req)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun deleteForum(foroId: Int) {
        viewModelScope.launch {
            try {
                val res = ForumService.api.deleteForum(foroId)
                if (res.ok) fetchForums()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun deletePost(postId: Int, foroId: Int) {
        viewModelScope.launch {
            try {
                val res = ForumService.api.deletePost(postId)
                if (res.ok) fetchForumDetail(foroId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun joinForumRoom(foroId: Int) {
        val data = JSONObject()
        data.put("room", "foro_$foroId")
        socket?.emit("join_chat", data)
    }

    private fun setupSocketListener() {
        socket?.on("new_forum_post") { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as JSONObject
                    val newPost = gson.fromJson(data.toString(), Publicacion::class.java)
                    val current = _detailState.value
                    if (current is ForumDetailUiState.Success && current.foro.id == newPost.idForo) {
                        val updatedList = current.publicaciones + newPost
                        _detailState.value = current.copy(publicaciones = updatedList)
                    }
                } catch (e: Exception) { Log.e("Socket", "Error parsing: ${e.message}") }
            }
        }
    }

    fun updateForum(foroId: Int, titulo: String, desc: String) {
        viewModelScope.launch {
            try {
                val req = UpdateForoRequest(titulo, desc)
                val res = ForumService.api.updateForum(foroId, req)
                if (res.ok) fetchForums()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updatePost(postId: Int, foroId: Int, content: String) {
        viewModelScope.launch {
            try {
                val req = UpdatePublicacionRequest(content)
                val res = ForumService.api.updatePost(postId, req)
                if (res.ok) fetchForumDetail(foroId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}