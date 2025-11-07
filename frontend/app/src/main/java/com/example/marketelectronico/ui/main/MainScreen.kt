package com.example.marketelectronico.ui.main

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
import com.example.marketelectronico.data.model.Product
import com.example.marketelectronico.data.model.sampleNews
import com.example.marketelectronico.data.model.sampleOffers
import com.example.marketelectronico.data.model.sampleRecommendations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Esta pantalla es "Inicio", así que el item 0 está seleccionado
    val selectedItem by remember { mutableIntStateOf(0) }

    // Esta es la lista de 'master', la respetamos
    val navItems = listOf("Inicio", "Categorías", "Vender", "Mensajes", "Perfil", "Foro")
    val navIcons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.List,
        Icons.Default.AddCircle,
        Icons.Default.Email,
        Icons.Default.Person,
        Icons.Default.Info // Respetamos el ícono 'Info' de master
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TechTradeTopBar(
                onCartClick = { navController.navigate("cart") }, // Navega al carrito
                onNotificationsClick = { navController.navigate("notifications") }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navItems.forEachIndexed { index, label ->
                    NavigationBarItem(
                        icon = { Icon(navIcons[index], contentDescription = label, tint = if (selectedItem == index) MaterialTheme.colorScheme.primary else Color.Gray) },
                        label = { Text(label, color = if (selectedItem == index) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 9.sp) },
                        selected = selectedItem == index,
                        
                        onClick = {
                            // selectedItem = index (No es necesario en esta pantalla)
                            
                            val route = when (label) {
                                "Inicio" -> "main"
                                // Tus pantallas (SÍ navegan)
                                "Mensajes" -> "chat_list"
                                "Perfil" -> "profile"
                                "Foro" -> "forum"
                                // Pantallas de compañeros (aún no implementadas)
                                "Categorías" -> "categories" 
                                "Vender" -> "publish"
                                else -> "main"
                            }

                            // Navegamos a la ruta
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
        MainScreenContent(
            modifier = Modifier.padding(innerPadding),
            navController = navController
        )
    }
}

// --- BARRA SUPERIOR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechTradeTopBar(onCartClick: () -> Unit, onNotificationsClick: () -> Unit) {
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

// --- CONTENIDO DE LA PANTALLA (LazyColumn) ---
@Composable
fun MainScreenContent(modifier: Modifier = Modifier, navController: NavController) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item { SearchBar(modifier = Modifier.padding(vertical = 8.dp)) }
        item { SectionTitle("Recomendaciones para ti") }
        item {
            ProductRow(
                products = sampleRecommendations,
                onProductClick = { productId ->
                    navController.navigate("product_detail/$productId")
                }
            )
        }
        item { SectionTitle("Novedades") }
        item {
            ProductRow(
                products = sampleNews,
                onProductClick = { productId ->
                    navController.navigate("product_detail/$productId")
                }
            )
        }
        item { SectionTitle("Ofertas destacadas") }
        item {
            ProductRow(
                products = sampleOffers,
                onProductClick = { productId ->
                    navController.navigate("product_detail/$productId")
                })
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- BARRA DE BÚSQUEDA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(modifier: Modifier = Modifier) {
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

// --- TÍTULO DE SECCIÓN ---
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
    )
}

// --- FILA DE PRODUCTOS ---
@Composable
fun ProductRow(products: List<Product>, onProductClick: (String) -> Unit) {
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

// --- TARJETA DE PRODUCTO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.width(150.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Placeholder
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
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