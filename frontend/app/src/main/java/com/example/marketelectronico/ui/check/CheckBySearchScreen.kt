package com.example.marketelectronico.ui.check

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.marketelectronico.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckBySearchScreen(
    navController: NavController,
    viewModel: CheckBySearchViewModel = viewModel()
) {
    val allProducts = viewModel.allProducts
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificar por Búsqueda") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Selecciona dos componentes para analizar su compatibilidad:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ProductSearchSelector(
                label = "Componente 1 (ej. Procesador)",
                allProducts = allProducts,
                selectedProduct = viewModel.selectedProduct1,
                onProductSelected = { viewModel.selectProduct1(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProductSearchSelector(
                label = "Componente 2 (ej. Placa Madre)",
                allProducts = allProducts,
                selectedProduct = viewModel.selectedProduct2,
                onProductSelected = { viewModel.selectProduct2(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.checkCompatibility()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewModel.isChecking &&
                        viewModel.selectedProduct1 != null &&
                        viewModel.selectedProduct2 != null
            ) {
                if (viewModel.isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Verificar Compatibilidad", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (viewModel.showResultDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = { Text(viewModel.checkResultTitle) },
            text = { Text(viewModel.checkResultMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissDialog() }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

/**
 * Componente reutilizable para buscar y seleccionar un producto de una lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSearchSelector(
    label: String,
    allProducts: List<Product>,
    selectedProduct: Product?,
    onProductSelected: (Product?) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    val filteredProducts = remember(query, allProducts) {
        if (query.isEmpty()) emptyList()
        else allProducts.filter { it.name.contains(query, ignoreCase = true) }.take(5)
    }

    Column {
        if (selectedProduct != null) {
            // --- VISTA DE PRODUCTO SELECCIONADO ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = selectedProduct.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedProduct.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$${selectedProduct.price}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = {
                        onProductSelected(null)
                        query = "" // Limpiar búsqueda al quitar selección
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Quitar")
                    }
                }
            }
        } else {
            // --- CAMPO DE BÚSQUEDA ---
            // Usamos un SearchBar simulado con TextField y una lista desplegable manual
            // para mayor control visual que el DockedSearchBar
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    active = true
                },
                label = { Text(label) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // --- LISTA DE RESULTADOS (Dropdown) ---
            AnimatedVisibility(visible = active && filteredProducts.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .heightIn(max = 200.dp), // Altura máxima para que no tape todo
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    LazyColumn {
                        items(filteredProducts) { product ->
                            ListItem(
                                headlineContent = { Text(product.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                leadingContent = {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.LightGray),
                                        contentScale = ContentScale.Crop
                                    )
                                },
                                trailingContent = {
                                    Text("$${product.price}", style = MaterialTheme.typography.bodySmall)
                                },
                                modifier = Modifier.clickable {
                                    onProductSelected(product)
                                    active = false
                                    query = ""
                                }
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}