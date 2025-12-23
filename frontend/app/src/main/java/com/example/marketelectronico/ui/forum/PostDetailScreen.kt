package com.example.marketelectronico.ui.forum

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.marketelectronico.data.remote.Publicacion
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    forumId: Int,
    modifier: Modifier = Modifier,
    viewModel: ForumViewModel = viewModel()
) {
    val uiState by viewModel.detailState.collectAsState()
    var replyText by remember { mutableStateOf("") }

    var postToEdit by remember { mutableStateOf<Publicacion?>(null) }

    var postToDelete by remember { mutableStateOf<Publicacion?>(null) }

    val myUserId = viewModel.currentUserId

    LaunchedEffect(forumId) {
        viewModel.fetchForumDetail(forumId)
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState is ForumDetailUiState.Success) (uiState as ForumDetailUiState.Success).foro.titulo else "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe una respuesta...") }
                )
                IconButton(onClick = {
                    if (replyText.isNotBlank()) {
                        viewModel.createPost(forumId, replyText)
                        replyText = ""
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Enviar")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ForumDetailUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ForumDetailUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
                is ForumDetailUiState.Success -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        item {
                            Text(state.foro.descripcion ?: "", style = MaterialTheme.typography.bodyLarge)
                            Divider(modifier = Modifier.padding(vertical = 16.dp))
                        }
                        items(state.publicaciones) { post ->
                            PostItem(
                                post = post,
                                isMyPost = post.idUsuario == myUserId,
                                onDelete = { postToDelete = post },
                                onEdit = { postToEdit = post }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }

    // Diálogo para editar comentario
    postToEdit?.let { post ->
        EditPostDialog(
            currentContent = post.contenido,
            onDismiss = { postToEdit = null },
            onConfirm = { newContent ->
                viewModel.updatePost(post.id, forumId, newContent)
                postToEdit = null
            }
        )
    }

    postToDelete?.let { post ->
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            title = { Text("Eliminar Comentario") },
            text = { Text("¿Estás seguro de que deseas eliminar este comentario?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePost(post.id, forumId)
                        postToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { postToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PostItem(
    post: Publicacion,
    isMyPost: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Top) {
                    AsyncImage(
                    model = post.usuario?.foto,
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = post.usuario?.nombre ?: "Usuario #${post.idUsuario}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (!isMyPost) {
                            Text(
                                text = post.fechaPublicacion.take(10),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // CONTENIDO DEL COMENTARIO
                    Text(
                        text = post.contenido,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (isMyPost) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = post.fechaPublicacion.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Borrar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditPostDialog(currentContent: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var content by remember { mutableStateOf(currentContent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Comentario") },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenido") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { Button(onClick = { if(content.isNotBlank()) onConfirm(content) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}