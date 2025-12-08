package com.example.marketelectronico.ui.forum

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.marketelectronico.data.remote.Publicacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    forumId: Int, // <-- Parámetro obligatorio primero
    modifier: Modifier = Modifier, // <-- Modifier opcional después
    viewModel: ForumViewModel = viewModel()
) {
    val uiState by viewModel.detailState.collectAsState()
    var replyText by remember { mutableStateOf("") }

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
                            PostItem(post)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Publicacion) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Usuario #${post.idUsuario}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(post.fechaPublicacion.take(10), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(post.contenido, style = MaterialTheme.typography.bodyMedium)
        }
    }
}