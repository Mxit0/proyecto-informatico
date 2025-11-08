package com.example.marketelectronico.ui.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.data.model.allSampleProducts
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme

// --- Datos de Muestra para Reviews (Se quedan en este archivo) ---
data class Review(
    val id: String,
    val author: String,
    val date: String,
    val rating: Double,
    val comment: String,
    val likes: Int,
    val dislikes: Int
)

val sampleReviews = listOf(
    Review("1", "Liam Carter", "2 weeks ago", 5.0, "This processor is a game-changer! It significantly boosted my computer's performance...", 23, 2),
    Review("2", "Sophia Bennett", "1 month ago", 3.5, "The processor works well... however, I encountered some minor issues during installation.", 15, 3),
    Review("3", "Ethan Walker", "2 months ago", 3.0, "The processor is okay for basic tasks, but it didn't meet my expectations for more demanding applications.", 8, 5)
)

val sampleRatingSummary = mapOf(
    5 to 0.40f,
    4 to 0.30f,
    3 to 0.15f,
    2 to 0.10f,
    1 to 0.05f
)
// -------------------------------------------------


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductReviewScreen(
    navController: NavController,
    productId: String?,
    modifier: Modifier = Modifier // <-- Añadido modifier
) {
    val product = allSampleProducts.find { it.id == productId } ?: allSampleProducts.first()
    val reviews = sampleReviews
    val ratingSummary = sampleRatingSummary
    val averageRating = 4.6
    val totalReviews = 124

    // --- LÓGICA DE LA BOTTOM BAR (DINÁMICA) ---
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
    // --- FIN LÓGICA BOTTOM BAR ---

    Scaffold(
        modifier = modifier, // <-- Añadido modifier
        topBar = {
            TopAppBar(
                title = { Text("Reviews", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                // --- LÓGICA DE SELECCIÓN DINÁMICA ---
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItems.forEachIndexed { index, label ->
                    val route = navRoutes[index]
                    val selected = currentRoute == route

                    NavigationBarItem(
                        icon = { Icon(navIcons[index], contentDescription = label, tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray) },
                        label = { Text(label, color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 9.sp) },
                        selected = selected,

                        // --- LÓGICA DE NAVEGACIÓN COMPLETA ---
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Sección de Resumen de Ratings
            item {
                RatingSummary(averageRating, totalReviews, ratingSummary)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sección de "Sort by"
            item {
                SortByChips()
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Lista de Reviews
            items(reviews) { review ->
                ReviewItem(review)
                Divider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
            }
        }
    }
}

// --- COMPONENTES INTERNOS (Sin cambios) ---

@Composable
private fun RatingSummary(averageRating: Double, totalReviews: Int, ratingSummary: Map<Int, Float>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = averageRating.toString(),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            RatingBar(rating = averageRating, starSize = 20.dp)
            Text(
                text = "$totalReviews reviews",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            (5 downTo 1).forEach { star ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(star.toString(), style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { ratingSummary[star] ?: 0f },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                    Text(
                        text = "${((ratingSummary[star] ?: 0f) * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.width(30.dp).padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortByChips() {
    var selectedChip by remember { mutableStateOf("Most recent") }
    val chips = listOf("Most recent", "Highest rating", "Lowest rating")

    Column {
        Text(
            text = "Sort by",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            chips.forEach { label ->
                FilterChip(
                    selected = selectedChip == label,
                    onClick = { selectedChip = label },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = Color.Gray,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = null
                )
            }
        }
    }
}

@Composable
private fun ReviewItem(review: Review) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Placeholder avatar
                contentDescription = review.author,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.author,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = review.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        RatingBar(rating = review.rating, starSize = 16.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = review.comment,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ThumbUp, contentDescription = "Likes", tint = Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(review.likes.toString(), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.ThumbDown, contentDescription = "Dislikes", tint = Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(review.dislikes.toString(), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
private fun RatingBar(rating: Double, starSize: Dp) {
    Row {
        val fullStars = rating.toInt()
        val halfStar = (rating - fullStars) >= 0.5
        val emptyStars = 5 - fullStars - (if (halfStar) 1 else 0)

        repeat(fullStars) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(starSize))
        }
        if (halfStar) {
            Icon(Icons.Default.StarHalf, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(starSize))
        }
        repeat(emptyStars) {
            Icon(Icons.Default.StarOutline, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(starSize))
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF1E1E2F)
@Composable
fun ProductReviewScreenPreview() {
    MarketElectronicoTheme {
        ProductReviewScreen(
            navController = rememberNavController(),
            productId = "1"
        )
    }
}