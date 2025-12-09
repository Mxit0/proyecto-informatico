package com.example.marketelectronico.ui.forum

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
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
import com.example.marketelectronico.data.remote.Foro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ForumViewModel = viewModel()
) {
    val uiState by viewModel.forumsState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Estado para controlar qué foro se está editando
    var forumToEdit by remember { mutableStateOf<Foro?>(null) }

    // ID del usuario actual para comparar permisos
    val myUserId = viewModel.currentUserId

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Foros") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchForums() }) {
                        Icon(Icons.Default.Refresh, "Recargar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "Crear")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ForumsUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ForumsUiState.Error -> Text("Error: ${state.message}", modifier = Modifier.align(Alignment.Center), color = Color.Red)
                is ForumsUiState.Success -> {
                    if (state.foros.isEmpty()) {
                        Text("No hay foros aún", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp)) {
                            items(state.foros) { foro ->
                                ForumItem(
                                    foro = foro,
                                    isMyForum = foro.idCreador == myUserId, // Verificar dueño
                                    onDelete = { viewModel.deleteForum(foro.id) },
                                    onEdit = { forumToEdit = foro }, // Abrir diálogo de edición
                                    onClick = { navController.navigate("forum_detail/${foro.id}") }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de Crear
    if (showCreateDialog) {
        CreateForumDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { t, d ->
                viewModel.createForum(t, d)
                showCreateDialog = false
            }
        )
    }

    // Diálogo de Editar
    forumToEdit?.let { foro ->
        EditForumDialog(
            foro = foro,
            onDismiss = { forumToEdit = null },
            onConfirm = { newTitle, newDesc ->
                viewModel.updateForum(foro.id, newTitle, newDesc)
                forumToEdit = null
            }
        )
    }
}

@Composable
fun ForumItem(
    foro: Foro,
    isMyForum: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // FOTO DEL CREADOR
                AsyncImage(
                    model = foro.usuario?.foto ?: "https://placehold.co/100",
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                // TEXTOS
                Column(modifier = Modifier.weight(1f)) {
                    Text(foro.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Por: ${foro.usuario?.nombre ?: "Anónimo"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // ACCIONES (Solo si soy el dueño)
                if (isMyForum) {
                    // Editar
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, "Editar", tint = Color.Blue)
                    }
                    // Borrar
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, "Borrar", tint = Color.Red)
                    }
                }
            }

            if (!foro.descripcion.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(foro.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun CreateForumDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Foro") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") })
            }
        },
        confirmButton = { Button(onClick = { if(title.isNotEmpty()) onConfirm(title, desc) }) { Text("Crear") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun EditForumDialog(foro: Foro, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf(foro.titulo) }
    var desc by remember { mutableStateOf(foro.descripcion ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Foro") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") })
            }
        },
        confirmButton = { Button(onClick = { if(title.isNotEmpty()) onConfirm(title, desc) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}