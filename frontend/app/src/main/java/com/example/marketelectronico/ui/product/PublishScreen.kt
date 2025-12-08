package com.example.marketelectronico.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.marketelectronico.ui.base.BaseScreen
import com.example.marketelectronico.ui.publish.PublishViewModel
import com.example.marketelectronico.ui.publish.PublishUiState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PublishViewModel = viewModel()
) {
    val context = LocalContext.current // Agregar esto

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoriaExpanded by remember { mutableStateOf(false) }
    var selectedCategoriaId by remember { mutableStateOf<Int?>(null) }
    var selectedCategoriaNombre by remember { mutableStateOf<String?>(null) }

    var selectedImages by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        selectedImages = uris
    }

    val categories by viewModel.categories.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Manejar el estado de éxito
    LaunchedEffect(uiState) {
        if (uiState is PublishUiState.Success) {
            // Limpiar el formulario
            nombre = ""
            precio = ""
            descripcion = ""
            selectedCategoriaId = null
            selectedCategoriaNombre = null
            selectedImages = emptyList() // Limpiar imágenes al publicar

            // Navegar de vuelta
            navController.popBackStack()
        }
    }

    BaseScreen(
        title = "Publicar Producto",
        navController = navController,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Campo: Nombre del producto
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del producto *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Precio
            OutlinedTextField(
                value = precio,
                onValueChange = {
                    // Solo permitir números y punto decimal
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        precio = it
                    }
                },
                label = { Text("Precio *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Text("$", modifier = Modifier.padding(start = 16.dp))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción del producto *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Categoría (Dropdown)
            ExposedDropdownMenuBox(
                expanded = categoriaExpanded,
                onExpandedChange = { categoriaExpanded = !categoriaExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategoriaNombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                ExposedDropdownMenu(
                    expanded = categoriaExpanded,
                    onDismissRequest = { categoriaExpanded = false }
                ) {
                    if (categories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Cargando categorías...") },
                            onClick = {}
                        )
                    } else {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.nombre) },
                                onClick = {
                                    selectedCategoriaId = category.id
                                    selectedCategoriaNombre = category.nombre
                                    categoriaExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Sección de imágenes
            Text(
                text = "Imágenes del producto (mínimo 3) *",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (selectedImages.isEmpty()) {
                Button(
                    onClick = { launcher.launch(PickVisualMediaRequest()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seleccionar imágenes")
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedImages.size) { index ->
                        Box {
                            AsyncImage(
                                model = selectedImages[index],
                                contentDescription = "Imagen ${index + 1}",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            IconButton(
                                onClick = { selectedImages = selectedImages.filterIndexed { i, _ -> i != index } },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, "Eliminar")
                            }
                        }
                    }
                }

                if (selectedImages.size < 3) {
                    Text(
                        text = "Faltan ${3 - selectedImages.size} imagen(es) más",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = { launcher.launch(PickVisualMediaRequest()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agregar más imágenes")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Mostrar error si existe
            if (uiState is PublishUiState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = (uiState as PublishUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botón: Publicar
            Button(
                onClick = {
                    viewModel.publishProduct(
                        nombre = nombre,
                        precio = precio,
                        descripcion = descripcion,
                        categoriaId = selectedCategoriaId,
                        imageUris = selectedImages,
                        context = context // Pasar el contexto
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState !is PublishUiState.Loading && selectedImages.size >= 3
            ) {
                if (uiState is PublishUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Publicar Producto",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
