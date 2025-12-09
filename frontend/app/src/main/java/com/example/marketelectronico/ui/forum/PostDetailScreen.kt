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

    // Estado para editar comentario
    var postToEdit by remember { mutableStateOf<Publicacion?>(null) }

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
                            // Cabecera del Foro
                            Text(state.foro.descripcion ?: "", style = MaterialTheme.typography.bodyLarge)
                            Divider(modifier = Modifier.padding(vertical = 16.dp))
                        }
                        items(state.publicaciones) { post ->
                            PostItem(
                                post = post,
                                isMyPost = post.idUsuario == myUserId, // Chequeo de propiedad
                                onDelete = { viewModel.deletePost(post.id, forumId) },
                                onEdit = { postToEdit = post } // Trigger editar
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
}

@Composable
fun PostItem(
    post: Publicacion,
    isMyPost: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // FOTO USUARIO
                AsyncImage(
                    model = post.usuario?.foto ?: "https://placehold.co/50",
                    contentDescription = null,
                    modifier = Modifier.size(30.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))

                // NOMBRE USUARIO
                Text(
                    post.usuario?.nombre ?: "User #${post.idUsuario}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // ACCIONES (Solo si es mi comentario)
                if (isMyPost) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, "Editar", tint = Color.Blue)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, "Borrar", tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(post.contenido, style = MaterialTheme.typography.bodyMedium)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(post.fechaPublicacion.take(10), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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