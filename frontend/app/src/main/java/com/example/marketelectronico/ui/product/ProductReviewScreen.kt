package com.example.marketelectronico.ui.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import kotlin.math.floor

// --- Modelo de datos para una Review (Duplicado para la preview) ---
data class Review(
    val id: String,
    val userName: String,
    val userAvatarUrl: String,
    val date: String,
    val rating: Int,
    val reviewText: String,
    val likes: Int,
    val dislikes: Int
)

// --- Datos de muestra para las Reviews (Duplicado para la preview) ---
val sampleReviews = listOf(
    Review("1", "Liam Carter", "", "2 weeks ago", 5, "This processor is a game-changer! It significantly boosted my computer's performance...", 23, 2),
    Review("2", "Sophia Bennett", "", "1 month ago", 3, "The processor works well and provides a noticeable improvement in speed. However, I encountered some minor issues...", 15, 3),
    Review("3", "Ethan Walker", "", "2 months ago", 2, "The processor is okay for basic tasks, but it didn't meet my expectations for more demanding applications...", 8, 5)
)

/**
 * Pantalla de Reviews del Producto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductReviewScreen(
    navController: NavController,
    productId: String? // Para saber de qué producto cargar reviews
) {
    // --- Lógica de la Barra de Navegación Inferior ---
    var selectedItem by remember { mutableIntStateOf(0) }
    val navItems = listOf("Inicio", "Categorías", "Vender", "Mensajes", "Perfil", "Foro")
    val navIcons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.List,
        Icons.Default.AddCircle,
        Icons.Default.Email,
        Icons.Default.Person,
        Icons.Default.Info
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reviews", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Botón de retroceso
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onBackground)
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
                navItems.forEachIndexed { index, label ->
                    NavigationBarItem(
                        icon = { Icon(navIcons[index], contentDescription = label, tint = if (selectedItem == index) MaterialTheme.colorScheme.primary else Color.Gray) },
                        label = { Text(label, color = if (selectedItem == index) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 9.sp) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index } // TODO: Conectar al NavController
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
            // --- 1. Resumen de Ratings ---
            item {
                ReviewSummaryHeader()
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- 2. Filtros de "Sort by" ---
            item {
                SortByChips()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- 3. Lista de Reviews ---
            items(sampleReviews) { review ->
                ReviewItem(review = review)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.surface)
            }
        }
    }
}

// --- 1. Resumen de Ratings (Header) ---
@Composable
fun ReviewSummaryHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // --- Lado Izquierdo (Rating general) ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "4.6",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            StarRating(rating = 4.6, starSize = 16.dp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "124 reviews",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // --- Lado Derecho (Histograma) ---
        Column(modifier = Modifier.fillMaxWidth()) {
            RatingBar(label = "5", percentage = 0.40f)
            RatingBar(label = "4", percentage = 0.30f)
            RatingBar(label = "3", percentage = 0.15f)
            RatingBar(label = "2", percentage = 0.10f)
            RatingBar(label = "1", percentage = 0.05f)
        }
    }
}

// --- 2. Filtros "Sort By" ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortByChips() {
    var selectedChip by remember { mutableIntStateOf(0) }
    val chipLabels = listOf("Most recent", "Highest rating", "Lowest rating")

    Column {
        Text(
            text = "Sort by",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chipLabels.size) { index ->
                FilterChip(
                    selected = selectedChip == index,
                    onClick = { selectedChip = index },
                    label = { Text(chipLabels[index]) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

// --- 3. Item individual de Review ---
@Composable
fun ReviewItem(review: Review) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // --- Header del Review (Avatar, Nombre, Rating) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Placeholder
                contentDescription = "Avatar de ${review.userName}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.userName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = review.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            StarRating(rating = review.rating.toDouble(), starSize = 14.dp)
        }

        // --- Texto del Review ---
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = review.reviewText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
        )

        // --- Botones de Like/Dislike ---
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.ThumbUp,
                contentDescription = "Like",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = review.likes.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                Icons.Filled.ThumbDown,
                contentDescription = "Dislike",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = review.dislikes.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}


// --- Componentes Helper ---

/**
 * Muestra las estrellas de un rating
 */
@Composable
fun StarRating(rating: Double, starSize: androidx.compose.ui.unit.Dp = 20.dp, starColor: Color = Color.Yellow) {
    Row {
        val fullStars = floor(rating).toInt()
        val halfStar = (rating - fullStars) >= 0.5
        val emptyStars = 5 - fullStars - (if (halfStar) 1 else 0)

        repeat(fullStars) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = starColor, modifier = Modifier.size(starSize))
        }
        if (halfStar) {
            Icon(Icons.Filled.StarHalf, contentDescription = null, tint = starColor, modifier = Modifier.size(starSize))
        }
        repeat(emptyStars) {
            Icon(Icons.Filled.StarOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(starSize))
        }
    }
}

/**
 * Muestra una barra del histograma
 */
@Composable
fun RatingBar(label: String, percentage: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(20.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${(percentage * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.width(30.dp) // Ancho fijo para alinear
        )
    }
}


// --- Vista Previa ---
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

