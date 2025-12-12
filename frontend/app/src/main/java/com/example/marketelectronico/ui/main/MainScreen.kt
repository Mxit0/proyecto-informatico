package com.example.marketelectronico.ui.main

// ... (Todos tus imports: Image, Layout, LazyColumn, Icons, etc.)
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.marketelectronico.data.model.Product
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.text.style.TextAlign
import com.example.marketelectronico.data.model.sampleProduct1
import androidx.compose.foundation.clickable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import com.example.marketelectronico.data.model.Category
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

// ... (Tu Composable MainScreen, TechTradeTopBar no cambian)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navItems = listOf("Inicio", "Check", "Vender", "Mensajes", "Perfil", "Foro")
    val navIcons = listOf(
        Icons.Default.Home,
        Icons.Filled.Check,
        Icons.Default.AddCircle,
        Icons.Default.Email,
        Icons.Default.Person,
        Icons.Default.Info
    )
    val navRoutes = listOf("main", "check", "publish", "chat_list", "profile", "forum")

    Scaffold(
        modifier = modifier,
        topBar = {
            TechTradeTopBar(
                onCartClick = { navController.navigate("cart") },
                onNotificationsClick = { /* navController.navigate("notifications") */ }
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

        when (val state = uiState) {
            is ProductListUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProductListUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ProductListUiState.Success -> {
                val categories by viewModel.categories.collectAsState()
                MainScreenContent(
                    products = state.products,
                    categories = categories,
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                    onCategorySelected = { categoryId -> viewModel.selectCategory(categoryId) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TechTradeTopBar(onCartClick: () -> Unit, onNotificationsClick: () -> Unit) {
    TopAppBar(
        title = { Text("TechTrade", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
        navigationIcon = {
            IconButton(onClick = onNotificationsClick) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        actions = {
            IconButton(onClick = onCartClick) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun MainScreenContent(
    products: List<Product>,
    categories: List<Category>,
    modifier: Modifier = Modifier,
    navController: NavController,
    onCategorySelected: (Int?) -> Unit = {}
) {
    // 1. Estado de la búsqueda
    var searchQuery by remember { mutableStateOf("") }

    // 2. Filtramos los productos si hay texto
    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isEmpty()) {
            emptyList()
        } else {
            products.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Datos para la vista normal
    val recommendations = products.take(5)
    val news = products.drop(5).take(5)
    val offers = products.drop(10)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        // Si estamos buscando, añadimos espacio extra entre items
        verticalArrangement = if (searchQuery.isNotEmpty()) Arrangement.spacedBy(16.dp) else Arrangement.Top
    ) {
        // --- BARRA DE BÚSQUEDA ---
        item {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // --- LÓGICA DE VISUALIZACIÓN ---
        if (searchQuery.isNotEmpty()) {
            // === MODO BÚSQUEDA ===
            if (filteredProducts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron productos.", color = Color.Gray)
                    }
                }
            } else {
                item {
                    Text(
                        "Resultados para \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(filteredProducts) { product ->
                    // Usamos una tarjeta horizontal para los resultados (definida más abajo)
                    SearchProductItem(
                        product = product,
                        onClick = { navController.navigate("product_detail/${product.id}") }
                    )
                }
            }
        } else {
            // === MODO NORMAL (TU DISEÑO ORIGINAL) ===
            item {
                if (categories.isNotEmpty()) {
                    CategoriesSection(
                        categories = categories,
                        onCategoryClick = { id, nombre ->
                            val encodedName = Uri.encode(nombre)
                            navController.navigate("category_products/$id/$encodedName")
                        }
                    )
                }
            }
            item { SectionTitle("Recomendaciones para ti") }
            item {
                ProductRow(
                    products = recommendations,
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    }
                )
            }
            item { SectionTitle("Novedades") }
            item {
                ProductRow(
                    products = news,
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    }
                )
            }
            item { SectionTitle("Ofertas destacadas") }
            item {
                ProductRow(
                    products = offers,
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    })
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Buscar productos", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Borrar", tint = Color.Gray)
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
    )
}

@Composable
private fun ProductRow(products: List<Product>, onProductClick: (String) -> Unit) {
    if (products.isEmpty()) {
        Text(
            text = "No hay productos en esta categoría.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.width(150.dp)
    ) {
        Column {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_menu_gallery),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .align(Alignment.CenterHorizontally)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${product.price}",
                    color = MaterialTheme.typography.bodyMedium.color,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun CategoriesSection(
    categories: List<Category>,
    onCategoryClick: (Int, String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionTitle(title = "Categorías")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(categories) { category ->
                CategoryCard(category, onCategoryClick)
            }
        }
    }
}

@Composable
fun getIconForCategory(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "laptops" -> Icons.Default.Laptop
        "celulares" -> Icons.Default.PhoneAndroid
        "accesorios" -> Icons.Default.Headset
        "monitores" -> Icons.Default.DesktopWindows
        "teclados" -> Icons.Default.Keyboard
        else -> Icons.Default.Category
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(category: Category, onClick: (Int, String) -> Unit) {
    Card(
        onClick = { onClick(category.id, category.nombre) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.width(90.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getIconForCategory(category.nombre),
                contentDescription = category.nombre,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.nombre,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SearchProductItem(product: Product, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_menu_gallery),
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(Color.LightGray) // <--- Ahora funcionará gracias al import
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // CAMBIO VISUAL: Usamos flecha a la derecha para indicar "ir al detalle"
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp),
                tint = Color.Gray
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF1E1E2F)
@Composable
fun MainScreenPreview() {
    MarketElectronicoTheme {
        MainScreenContent(
            products = listOf(sampleProduct1),
            categories = listOf(
                Category(id = 1, nombre = "Laptops"),
                Category(id = 2, nombre = "Celulares"),
                Category(id = 3, nombre = "Accesorios")
            ),
            navController = rememberNavController(),
            modifier = Modifier
        )
    }
}