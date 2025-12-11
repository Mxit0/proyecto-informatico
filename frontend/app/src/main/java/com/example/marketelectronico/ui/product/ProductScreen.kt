package com.example.marketelectronico.ui.product

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border // Agregado para el borde de la foto
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.data.model.Product
// import com.example.marketelectronico.data.model.allSampleProducts // Ya no se usa
import com.example.marketelectronico.data.model.sampleProduct1
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import com.example.marketelectronico.data.repository.CartRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage // <-- IMPORTANTE: COIL
import kotlinx.coroutines.flow.collectLatest
import com.example.marketelectronico.utils.TokenManager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.clickable
import com.example.marketelectronico.data.repository.Review
import com.example.marketelectronico.data.repository.ReviewRepository
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

/**
 * Pantalla de Detalles del Producto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    navController: NavController,
    productId: String?,
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = viewModel()
) {
    // --- 4. OBSERVAR ESTADO Y CARGAR DATOS ---
    val uiState by viewModel.uiState.collectAsState()

    val rawId = TokenManager.getUserId()
    val currentUserId = rawId?.toString()?.toIntOrNull() ?: -1

    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collectLatest { route ->
            navController.navigate(route)
        }
    }

    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.fetchProduct(productId)
        }
    }
    // ----------------------------------------

    // --- Lógica de la Bottom Bar (sin cambios) ---
    val navItems = listOf("Inicio", "Categorías", "Vender", "Mensajes", "Perfil", "Foro")
    val navIcons = listOf(Icons.Default.Home, Icons.AutoMirrored.Filled.List, Icons.Default.AddCircle, Icons.Default.Email, Icons.Default.Person, Icons.Default.Info)
    val navRoutes = listOf("main", "categories", "publish", "chat_list", "profile", "forum")
    // --- FIN LÓGICA BOTTOM BAR ---

    val myExistingReview by viewModel.myExistingReview.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is ProductDetailUiState.Success) {
            val product = (uiState as ProductDetailUiState.Success).product
            viewModel.checkIfReviewed(product.sellerId)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // CAMBIO CLAVE:
                // No usamos la variable 'uiState' de arriba (que puede ser vieja),
                // sino que pedimos el valor ACTUAL al ViewModel en este instante exacto.
                val currentState = viewModel.uiState.value

                if (currentState is ProductDetailUiState.Success) {
                    // Ahora sí tenemos el producto cargado y podemos chequear
                    viewModel.checkIfReviewed(currentState.product.sellerId)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val currentBackStackEntry = navController.currentBackStackEntry
    val refreshReviews = currentBackStackEntry?.savedStateHandle
        ?.getStateFlow("refresh_reviews", false)
        ?.collectAsState()

    LaunchedEffect(refreshReviews?.value) {
        if (refreshReviews?.value == true) {
            if (uiState is ProductDetailUiState.Success) {
                val sellerId = (uiState as ProductDetailUiState.Success).product.sellerId
                viewModel.checkIfReviewed(sellerId)

                // Reseteamos el valor a false para no recargar infinitamente
                currentBackStackEntry?.savedStateHandle?.set("refresh_reviews", false)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Acción de compartir */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItems.forEachIndexed { index, label ->
                    val route = navRoutes[index]
                    val selected = currentRoute == route

                    NavigationBarItem(
                        icon = { Icon(navIcons[index], contentDescription = label, tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray) },
                        label = { Text(label, color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 9.sp) },
                        selected = selected,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        // --- 5. MANEJO DE ESTADO ---
        when (val state = uiState) {
            is ProductDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProductDetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ProductDetailUiState.Success -> {
                ProductDetailsContent(
                    product = state.product,
                    navController = navController,
                    currentUserId = currentUserId,
                    paddingValues = innerPadding,
                    onContactSeller = { sellerId ->
                        viewModel.contactSeller(sellerId)
                    },
                    onDelete = { id ->
                        viewModel.deleteCurrentProduct(id) {
                            // Al borrar exitosamente, volvemos atrás
                            navController.popBackStack()
                        }
                    },
                    onUpdate = { id, name, desc, price, stock ->
                        viewModel.updateCurrentProduct(id, name, desc, price, stock)
                    },
                    onSellerClick = { sellerId ->
                        // Navegamos a una nueva ruta pasando el ID
                        navController.navigate("profile_public/$sellerId")
                    },
                    myExistingReview = myExistingReview,
                    onUpdateReview = { id, rating, comment -> // <--- Callback para editar
                        viewModel.updateUserReview(id, rating, comment) {
                            viewModel.checkIfReviewed(state.product.sellerId) // Recargar
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProductDetailsContent(
    product: Product,
    currentUserId: Int,
    navController: NavController,
    paddingValues: PaddingValues,
    onContactSeller: (Int) -> Unit,
    onDelete: (String) -> Unit,
    onUpdate: (String, String, String, Double, Int) -> Unit,
    onSellerClick: (Int) -> Unit,
    myExistingReview: Review?,
    onUpdateReview: (String, Double, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showNoStockDialog by remember { mutableStateOf(false) }

    var showEditReviewDialog by remember { mutableStateOf(false) }

    // --- GALERÍA DE IMÁGENES ---
    // Lista final de imágenes: si no hay imageUrls, usamos solo imageUrl
    val imageList = remember(product) {
        val baseList = if (product.imageUrls.isNotEmpty()) {
            product.imageUrls
        } else {
            listOf(product.imageUrl)
        }
        baseList.filter { it.isNotBlank() }
    }

    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val mainImageUrl = imageList.getOrNull(selectedIndex) ?: product.imageUrl

    val finalImages = remember(product) {
        if (product.imageUrls.isNotEmpty()) {
            product.imageUrls.filter { it.isNotBlank() }
        } else {
            listOf(product.imageUrl).filter { it.isNotBlank() }
        }
    }
    // --- FIN GALERÍA DE IMÁGENES ---

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        // --- GALERÍA: MINIATURAS + IMAGEN PRINCIPAL ---
        ProductImageCarousel(
            images = finalImages,
            productName = product.name
        )
        // --- FIN GALERÍA ---

        Column(modifier = Modifier.padding(16.dp)) {
            if (!product.active) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Este producto ya no está disponible.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${product.price}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- Sección del Vendedor ---
            Text(
                text = "Vendedor",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Botones Superiores (Editar/Borrar si es dueño, Comprar/Contactar si no)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (product.sellerId == currentUserId) {
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Editar")
                    }

                    // Botón Borrar
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Borrar")
                    }
                } else {
                    val currentStock = product.specifications["Stock"]?.toIntOrNull() ?: 0
                    val canBuy = currentStock > 0 && product.active
                    Button(
                        onClick = {
                            if (currentStock > 0) {
                                CartRepository.addToCart(product)
                                showDialog = true
                            } else {
                                showNoStockDialog = true
                            }
                            if (canBuy) {
                                CartRepository.addToCart(product)
                                showDialog = true
                            } else if (!product.active) {
                                // Opcional: Mostrar mensaje "Producto descontinuado"
                            } else {
                                showNoStockDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if ((product.specifications["Stock"]?.toIntOrNull() ?: 0) > 0) MaterialTheme.colorScheme.primary else Color.Gray
                        ),
                        enabled = product.active,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        // Opcional: Cambiar texto si está agotado
                        val stock = product.specifications["Stock"]?.toIntOrNull() ?: 0
                        when {
                            !product.active -> "No Disponible"
                            currentStock > 0 -> "Añadir al Carrito"
                            else -> "Agotado"
                        }
                    }

                    OutlinedButton(
                        onClick = { onContactSeller(product.sellerId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Contactar")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 👇 NUEVA ESTRUCTURA VISUAL
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable { onSellerClick(product.sellerId) }
                    .padding(12.dp)
            ) {
                // 👇 FOTO CON COIL
                AsyncImage(
                    model = product.sellerImageUrl ?: "https://i.pravatar.cc/150?u=${product.sellerId}",
                    contentDescription = "Avatar del vendedor",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.sellerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${product.sellerRating} (${product.sellerReviews} reviews)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Descripción
            Text(
                text = "Descripción",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Especificaciones
            Text(
                text = "Especificaciones",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            val specsList = product.specifications.entries.toList()
            specsList.chunked(2).forEach { rowSpecs ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowSpecs.forEach { (key, value) ->
                        SpecificationItem(label = key, value = value, modifier = Modifier.weight(1f))
                    }
                    if (rowSpecs.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTONES INFERIORES (Reviews y Calificar) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Reviews del Producto
                OutlinedButton(
                    onClick = { navController.navigate("product_reviews/${product.id}") },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(MaterialTheme.colorScheme.primary))
                ) {
                    Text(
                        text = "Reviews Producto",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }

                // Botón Calificar / Editar Vendedor
                // (Solo mostrar si NO soy el dueño)
                if (product.sellerId != currentUserId) {
                    if (myExistingReview != null) {
                        // MODO EDITAR
                        Button(
                            onClick = { showEditReviewDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                text = "Editar tu Review",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // MODO CALIFICAR (CREAR)
                        OutlinedButton(
                            onClick = { navController.navigate("add_seller_review/${product.sellerId}/${product.sellerName}") },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(MaterialTheme.colorScheme.primary))
                        ) {
                            Text(
                                text = "Calificar Vendedor",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Relleno si soy el dueño para que el botón de la izquierda no se estire solo
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    if (showNoStockDialog) {
        AlertDialog(
            onDismissRequest = { showNoStockDialog = false },
            title = { Text("Producto Agotado") },
            text = { Text("Lo sentimos, este producto no tiene stock disponible por el momento.") },
            confirmButton = { TextButton(onClick = { showNoStockDialog = false }) { Text("Entendido") } },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Yellow) }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar producto?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(product.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") } }
        )
    }

    if (showEditDialog) {
        EditProductDialog(
            product = product,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, desc, price, stock ->
                showEditDialog = false
                onUpdate(product.id, name, desc, price, stock)
            }
        )
    }

    // --- DIÁLOGO DE "AÑADIDO AL CARRITO" ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "¡Producto Añadido!") },
            text = { Text(text = "El producto ha sido añadido a tu carrito.") },
            confirmButton = { TextButton(onClick = { showDialog = false; navController.navigate("cart") }) { Text("Ir al Carrito") } },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Seguir Comprando") } }
        )
    }

    // DIÁLOGO DE EDICIÓN DE REVIEW DE VENDEDOR
    if (showEditReviewDialog && myExistingReview != null) {
        var newRating by remember { mutableDoubleStateOf(myExistingReview!!.rating) }
        var newComment by remember { mutableStateOf(myExistingReview!!.comment) }

        AlertDialog(
            onDismissRequest = { showEditReviewDialog = false },
            title = { Text("Editar tu opinión sobre el vendedor") },
            text = {
                Column {
                    com.example.marketelectronico.ui.review.RatingInput(
                        currentRating = newRating,
                        onRatingChanged = { newRating = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        label = { Text("Comentario") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showEditReviewDialog = false
                    onUpdateReview(myExistingReview!!.id, newRating, newComment)
                }) { Text("Actualizar") }
            },
            dismissButton = { TextButton(onClick = { showEditReviewDialog = false }) { Text("Cancelar") } }
        )
    }
}

// Composable para un item de especificación
@Composable
fun SpecificationItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).padding(12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var desc by remember { mutableStateOf(product.description) }
    var priceStr by remember { mutableStateOf(product.price.toString()) }
    var stockStr by remember { mutableStateOf(product.specifications["Stock"] ?: "1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") })
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val price = priceStr.toDoubleOrNull() ?: product.price
                val stock = stockStr.toIntOrNull() ?: 1
                onConfirm(name, desc, price, stock)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductImageCarousel(
    images: List<String>,
    productName: String
) {
    // Si no hay imágenes, mostramos un placeholder
    if (images.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp) // Altura más generosa para ver bien el producto
            .background(MaterialTheme.colorScheme.surface) // Fondo limpio
    ) {
        // --- CARRUSEL DESLIZABLE ---
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = images[page],
                contentDescription = "$productName imagen ${page + 1}",
                //contentScale = ContentScale.Fit, // Fit para ver el producto entero, Crop para llenar
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_menu_gallery),
                modifier = Modifier
                    .fillMaxSize()
                    //.padding(16.dp) // Un poco de aire para que no toque los bordes
            )
        }

        // --- INDICADOR DE PUNTOS (DOTS) ---
        // Solo mostramos los puntos si hay más de una imagen
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(images.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp) // Tamaño del punto
                    )
                }
            }
        }

        // --- CONTADOR NUMÉRICO (Opcional, estilo MercadoLibre) ---
        // Descomenta esto si prefieres "1/5" en vez de puntitos

        if (images.size > 1) {
            Text(
                text = "${pagerState.currentPage + 1} / ${images.size}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

    }
}


// --- Vista Previa ---
@Preview(showBackground = true, backgroundColor = 0xFF1E1E2F)
@Composable
fun ProductScreenPreview() {
    MarketElectronicoTheme {
        ProductDetailsContent(
            product = sampleProduct1,
            currentUserId = 1,
            navController = rememberNavController(),
            paddingValues = PaddingValues(0.dp),
            onContactSeller = {},
            onDelete = {},
            onUpdate = { _, _, _, _, _ -> },
            onSellerClick = {},
            myExistingReview = null,
            onUpdateReview = { _, _, _ -> }
        )
    }
}