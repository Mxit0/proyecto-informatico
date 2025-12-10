package com.example.marketelectronico.ui.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.marketelectronico.data.repository.OrderRepository
import com.example.marketelectronico.data.repository.Order
import com.example.marketelectronico.data.repository.Review
import com.example.marketelectronico.data.repository.ReviewRepository
import com.example.marketelectronico.data.repository.UserRepository
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.clickable
import com.example.marketelectronico.utils.TokenManager
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.marketelectronico.data.model.Product
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userIdArgument: String? = null,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val currentUserId = TokenManager.getUserId()?.toString()
    val isOwnProfile = userIdArgument == null || userIdArgument == currentUserId
    LaunchedEffect(userIdArgument) {
        viewModel.loadData(userIdArgument)
    }
    // Estado del perfil
    val userProfile by viewModel.userProfile.collectAsState()
    val userOrders by viewModel.userOrders.collectAsState()
    val myProducts by viewModel.myProducts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    // Imagen local elegida desde la galería
    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para abrir la galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            localImageUri = uri          // para mostrarla en la UI
            viewModel.onNewProfileImageSelected(uri, context)
        }
    }


    // --- LÓGICA DE LA BOTTOM BAR ---
    val navItems = listOf("Inicio", "Categorías", "Vender", "Mensajes", "Perfil", "Foro")
    val navIcons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.List,
        Icons.Default.AddCircle,
        Icons.Default.Email,
        Icons.Default.Person,
        Icons.Default.Info
    )
    val navRoutes = listOf("main", "categories", "publish", "chat_list", "profile", "forum")

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItems.forEachIndexed { index, label ->
                    val route = navRoutes[index]
                    val selected = currentRoute == route

                    NavigationBarItem(
                        icon = {
                            Icon(
                                navIcons[index],
                                contentDescription = label,
                                tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                label,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                                fontSize = 9.sp
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: $error", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadUserProfile() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserInfoSection(
                        userProfile = userProfile,
                        localImageUri = localImageUri,
                        isEditable = isOwnProfile,
                        onChangePhotoClick = { imagePickerLauncher.launch("image/*") }
                    )
                    ProfileTabs(
                        navController = navController,
                        userProfile = userProfile,
                        userOrders = userOrders,
                        myProducts = myProducts,
                        isOwnProfile = isOwnProfile
                    )
                }
            }
        }
    }
}


// --- SECCIÓN DE INFORMACIÓN DEL USUARIO (De tu compañero) ---
@Composable
private fun UserInfoSection(
    userProfile: com.example.marketelectronico.data.remote.UserProfileDto?,
    localImageUri: Uri?,
    isEditable: Boolean,
    onChangePhotoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Usamos Box para superponer el icono de lápiz sobre la foto
        Box(contentAlignment = Alignment.BottomEnd) {

            // DEFINIR EL MODIFICADOR COMÚN PARA LA IMAGEN
            val imageModifier = Modifier
                .size(100.dp) // Tamaño fijo y más grande
                .clip(CircleShape)
                .background(Color.LightGray) // Fondo por si la imagen es transparente o carga
                .then(
                    // Solo agregamos el efecto de click si es editable
                    if (isEditable) Modifier.clickable { onChangePhotoClick() } else Modifier
                )

            // MOSTRAR LA IMAGEN SEGÚN EL ESTADO
            when {
                // Caso A: Imagen seleccionada de la galería (local)
                localImageUri != null -> {
                    AsyncImage(
                        model = localImageUri,
                        contentDescription = "Foto de Perfil",
                        modifier = imageModifier,
                        contentScale = ContentScale.Crop
                    )
                }
                // Caso B: Foto guardada en el backend
                userProfile?.foto != null -> {
                    AsyncImage(
                        model = userProfile.foto,
                        contentDescription = "Foto de Perfil",
                        modifier = imageModifier,
                        contentScale = ContentScale.Crop
                    )
                }
                // Caso C: Sin foto (Placeholder)
                else -> {
                    Image(
                        painter = painterResource(id = android.R.drawable.ic_menu_camera),
                        contentDescription = "Foto de Perfil",
                        modifier = imageModifier.padding(24.dp), // Padding interno para que el icono no se estire
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // EL ICONO DE EDITAR (LÁPIZ) - Superpuesto
            if (isEditable) {
                // Creamos un pequeño círculo azul para el lápiz
                Box(
                    modifier = Modifier
                        .padding(4.dp) // Un pequeño margen respecto al borde de la foto
                        .size(24.dp)   // Tamaño del círculo del botón
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onChangePhotoClick() }, // Click también aquí
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar foto",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp) // Tamaño del icono de lápiz dentro del círculo
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Textos del perfil
        Text(
            text = userProfile?.nombre_usuario ?: "Cargando...",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = userProfile?.correo ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}


// --- PESTAÑAS DEL PERFIL ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileTabs(
    navController: NavController,
    userProfile: com.example.marketelectronico.data.remote.UserProfileDto?,
    userOrders: List<Order>,
    myProducts: List<Product>,
    isOwnProfile: Boolean
) {
    val tabTitles = remember(isOwnProfile) {
        if (isOwnProfile) {
            // Si es mi perfil, veo todo
            listOf("Mi Nota", "Ventas", "Compras", "Reviews")
        } else {
            // Si es perfil ajeno, ocultamos "Compras"
            listOf("Mi Nota", "Ventas", "Reviews")
        }
    }
    val pagerState = rememberPagerState { tabTitles.size }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxHeight()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(text = title, fontSize = 12.sp) } // Ajusté un poco la fuente
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // 2. SWITCH BASADO EN EL NOMBRE DE LA PESTAÑA (NO EN EL ÍNDICE)
                // Como el índice cambia si quitamos pestañas, usamos el título para saber qué mostrar.
                when (tabTitles[pageIndex]) {
                    "Mi Nota" -> MyRatingPage(reputation = userProfile?.reputacion)

                    "Ventas" -> MySalesPage(
                        products = myProducts,
                        onProductClick = { productId ->
                            navController.navigate("product_detail/$productId")
                        }
                    )

                    "Compras" -> PurchasesHistoryPage(
                        orders = userOrders,
                        onOrderClick = { orderId ->
                            navController.navigate("order_detail/$orderId")
                        }
                    )

                    "Reviews" -> ReviewsHistoryPage(
                        onReviewClick = { productId ->
                            navController.navigate("product_detail/$productId")
                        }
                    )
                }
            }
        }

        // El botón de cerrar sesión solo aparece si es tu perfil
        if (isOwnProfile) {
            Button(
                onClick = { navController.navigate("login") { popUpTo(0) { inclusive = true } } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Cerrar Sesión")
            }
        }
    }
}

@Composable
private fun MyRatingPage(reputation: Double?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tu Reputación como Vendedor", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%.1f", reputation ?: 0.0), // Mostramos la reputación real
                style = MaterialTheme.typography.headlineLarge
            )
        }
        Text("(Basado en tus ventas)", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PurchasesHistoryPage(
    orders: List<Order>,
    onOrderClick: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Historial de Compras", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (orders.isEmpty()) {
            Text("Aún no tienes compras.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    OrderHistoryItem(
                        order = order,
                        onClick = { onOrderClick(order.id) } // Pasamos el evento click
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewsHistoryPage(onReviewClick: (String) -> Unit) {
    // Estado para las reseñas
    var myReviews by remember { mutableStateOf<List<Review>>(emptyList()) }

    val currentUserId = TokenManager.getUserId()?.toString()

    // Llamada asíncrona correcta
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            myReviews = ReviewRepository.getReviewsByUser(currentUserId)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Reviews que has Escrito", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (myReviews.isEmpty()) {
            Text("Aún no has escrito reviews.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myReviews) { review ->
                    MyReviewItem(
                        review = review,
                        onClick = {
                            // 2. Al hacer click, pasamos el ID del producto
                            onReviewClick(review.productId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MyReviewItem(review: Review, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Imagen del producto (usando Coil)
            AsyncImage(
                model = review.productImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Para: ${review.productName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Estrellas
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val rating = review.rating
                    val fullStars = rating.toInt()
                    val halfStar = (rating - fullStars) >= 0.5
                    val emptyStars = 5 - fullStars - (if (halfStar) 1 else 0)

                    repeat(fullStars) { Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
                    if (halfStar) { Icon(Icons.Default.StarHalf, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
                    repeat(emptyStars) { Icon(Icons.Default.StarOutline, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = ReviewRepository.formatDate(review.date), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OrderHistoryItem(
    order: Order,
    onClick: () -> Unit // Nuevo parámetro
) {
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ... (El contenido de texto sigue igual) ...
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${order.totalAmount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = dateFormatter.format(order.date),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Texto indicativo
            Text(
                text = "Ver detalles de la compra >",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MySalesPage(
    products: List<Product>,
    onProductClick: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Mis Productos en Venta", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (products.isEmpty()) {
            Text("No tienes productos publicados.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    MyProductItem(product = product, onClick = { onProductClick(product.id) })
                }
            }
        }
    }
}

@Composable
private fun MyProductItem(product: Product, onClick: () -> Unit) {
    // Calculamos si hay stock para cambiar el color del texto
    val stock = product.specifications["Stock"]?.toIntOrNull() ?: 0
    val hasStock = stock > 0

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Imagen
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Indicador de Stock
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if(hasStock) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if(hasStock) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if(hasStock) "Stock: $stock" else "Agotado",
                        style = MaterialTheme.typography.bodySmall,
                        color = if(hasStock) Color.Gray else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MarketElectronicoTheme {
        ProfileScreen(navController = rememberNavController())
    }
}