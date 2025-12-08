package com.example.marketelectronico.ui.forum

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.marketelectronico.data.remote.Foro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ForumViewModel = viewModel()
) {
    val uiState by viewModel.forumsState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

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
            FloatingActionButton(onClick = { showDialog = true }) {
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
                                ForumItem(foro) {
                                    navController.navigate("forum_detail/${foro.id}")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateForumDialog(
            onDismiss = { showDialog = false },
            onConfirm = { t, d ->
                viewModel.createForum(t, d)
                showDialog = false
            }
        )
    }
}

@Composable
fun ForumItem(foro: Foro, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(foro.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (!foro.descripcion.isNullOrEmpty()) {
                Text(foro.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
            Text("ID: ${foro.id}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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