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
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.R
import com.example.marketelectronico.data.model.allSampleProducts
import com.example.marketelectronico.data.repository.Review
import com.example.marketelectronico.data.repository.ReviewRepository
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import java.util.Locale
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ThumbUp
import com.example.marketelectronico.utils.TokenManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductReviewScreen(
    navController: NavController,
    productId: String?
) {
    val currentUserId = TokenManager.getUserId()?.toString() ?: ""
    // Obtener reseñas crudas del repositorio
    val rawReviews = ReviewRepository.allReviews // Lista viva
        .filter { it.productId == productId }

    // --- 1. ESTADO PARA EL ORDENAMIENTO ---
    // Por defecto ordenamos por "Most recent"
    var selectedSortOption by remember { mutableStateOf("Most recent") }

    // --- 2. LÓGICA DE ORDENAMIENTO ---
    // Esta lista se recalcula automáticamente cuando cambia 'rawReviews' o 'selectedSortOption'
    val reviews = remember(rawReviews, selectedSortOption) {
        when (selectedSortOption) {
            "Highest rating" -> rawReviews.sortedByDescending { it.rating }
            "Lowest rating" -> rawReviews.sortedBy { it.rating }
            else -> rawReviews.sortedByDescending { it.date } // "Most recent" (default)
        }
    }

    val totalReviews = rawReviews.size
    val averageRating = if (rawReviews.isNotEmpty()) rawReviews.sumOf { it.rating } / totalReviews else 0.0
    val ratingSummary = (1..5).associateWith { star ->
        if (totalReviews == 0) 0f else rawReviews.count { it.rating.toInt() == star } / totalReviews.toFloat()
    }

    Scaffold(
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                RatingSummary(averageRating, totalReviews, ratingSummary)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- 3. PASAR EL ESTADO A LOS CHIPS ---
            item {
                SortByChips(
                    selectedOption = selectedSortOption,
                    onOptionSelected = { newOption -> selectedSortOption = newOption }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(reviews) { review -> // Asegúrate de usar la lista 'reviews' ya ordenada
                ReviewItem(
                    review = review,
                    currentUserId = currentUserId,
                    onLikeClick = { ReviewRepository.toggleLike(review.id, currentUserId) },
                    onDeleteClick = { ReviewRepository.deleteReview(review.id) },
                    onEditClick = { newComment, newRating ->
                        ReviewRepository.updateReview(review.id, newComment, newRating)
                    }
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
            }
        }
    }
}

@Composable
fun RatingSummary(averageRating: Double, totalReviews: Int, ratingSummary: Map<Int, Float>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(Locale.US, "%.1f", averageRating),
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
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortByChips(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
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
                    selected = selectedOption == label,
                    onClick = { onOptionSelected(label) }, // Notificamos el cambio
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
fun ReviewItem(
    review: Review,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: (String, Double) -> Unit
) {
    // Estado para el diálogo de edición
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.Top) {
            // --- FOTO Y NOMBRE (Tu código existente) ---
            if (review.authorImageUrl != null) {
                AsyncImage(
                    model = review.authorImageUrl,
                    contentDescription = review.author,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                    contentDescription = review.author,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.author,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = ReviewRepository.formatDate(review.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // --- ACCIONES DE DUEÑO (Editar / Borrar) ---
            if (review.authorId == currentUserId && currentUserId.isNotEmpty()) {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                // --- MODIFICADO: Ahora abre el diálogo en lugar de borrar directo
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
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

        Spacer(modifier = Modifier.height(12.dp))

        // --- BOTÓN DE LIKE ---
        val isLiked = review.likedByUserIds.contains(currentUserId)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { if(currentUserId.isNotEmpty()) onLikeClick() } // Solo si está logueado
                .background(if (isLiked) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${review.likesCount} Helpful",
                style = MaterialTheme.typography.labelMedium,
                color = if (isLiked) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }

    // --- DIÁLOGO DE EDICIÓN ---
    if (showEditDialog) {
        EditReviewDialog(
            initialComment = review.comment,
            initialRating = review.rating,
            onDismiss = { showEditDialog = false },
            onConfirm = { comment, rating ->
                onEditClick(comment, rating)
                showEditDialog = false
            }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Reseña") },
            text = { Text("¿Estás seguro de que quieres eliminar esta reseña? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick() // Aquí sí ejecutamos el borrado real
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun RatingBar(rating: Double, starSize: Dp) {
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
@Composable
fun RatingInput(currentRating: Double, onRatingChanged: (Double) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        (1..5).forEach { starIndex ->
            val icon = when {
                currentRating >= starIndex -> Icons.Default.Star
                currentRating >= starIndex - 0.5 -> Icons.Default.StarHalf
                else -> Icons.Default.StarOutline
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
                    .pointerInput(starIndex) {
                        detectTapGestures { offset ->
                            if (offset.x < this.size.width / 2) onRatingChanged(starIndex - 0.5)
                            else onRatingChanged(starIndex.toDouble())
                        }
                    }
            )
        }
    }
}

@Composable
fun EditReviewDialog(
    initialComment: String,
    initialRating: Double,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var comment by remember { mutableStateOf(initialComment) }
    var rating by remember { mutableDoubleStateOf(initialRating) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Reseña") },
        text = {
            Column {
                RatingInput(currentRating = rating, onRatingChanged = { rating = it }) // Reusando tu componente existente
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(comment, rating) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}