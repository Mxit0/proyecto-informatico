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
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ForumViewModel = viewModel()
) {
    val uiState by viewModel.forumsState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    var forumToEdit by remember { mutableStateOf<Foro?>(null) }

    var forumToDelete by remember { mutableStateOf<Foro?>(null) }

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
                                    isMyForum = foro.idCreador == myUserId,
                                    onDelete = { forumToDelete = foro },
                                    onEdit = { forumToEdit = foro },
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

    if (showCreateDialog) {
        CreateForumDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { t, d ->
                viewModel.createForum(t, d)
                showCreateDialog = false
            }
        )
    }

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

    forumToDelete?.let { foro ->
        AlertDialog(
            onDismissRequest = { forumToDelete = null },
            title = { Text("Eliminar Foro") },
            text = { Text("¿Estás seguro de que deseas eliminar el foro \"${foro.titulo}\"? Esta acción también borrará todos los comentarios.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteForum(foro.id)
                        forumToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { forumToDelete = null }) {
                    Text("Cancelar")
                }
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
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = foro.usuario?.foto,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = foro.usuario?.nombre ?: "Anónimo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = foro.titulo,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!foro.descripcion.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = foro.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isMyForum) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Borrar", fontSize = 12.sp)
                    }
                }
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