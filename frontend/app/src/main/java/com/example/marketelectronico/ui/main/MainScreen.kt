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
// ... (Tu Composable MainScreen, TechTradeTopBar no cambian)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
    // ... (Tu lógica de 'recommendations', 'news', 'offers' no cambia)
    val recommendations = products.take(5)
    val news = products.drop(5).take(5)
    val offers = products.drop(10)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item { SearchBar(modifier = Modifier.padding(vertical = 8.dp)) }
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

// ... (Tu Composable SearchBar y SectionTitle no cambian)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text("Buscar productos", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray) },
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
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

// --- ¡AQUÍ ESTÁ LA MEJORA! ---
@Composable
private fun ProductRow(products: List<Product>, onProductClick: (String) -> Unit) {

    // Comprueba si la lista está vacía
    if (products.isEmpty()) {
        Text(
            text = "No hay productos en esta categoría.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    } else {
        // Si no está vacía, muestra la fila
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
// ------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    // ... (Tu Composable ProductCard con AsyncImage no cambia)
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

@Composable
fun CategoriesSection(
    categories: List<Category>,
    onCategoryClick: (Int, String) -> Unit // Pasamos ID y Nombre
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
fun CategoryCard(category: Category, onClick: (Int, String) -> Unit) {
    Card(
        onClick = { onClick(category.id, category.nombre) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.size(width = 110.dp, height = 60.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = category.nombre,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}