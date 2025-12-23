package com.example.marketelectronico.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.marketelectronico.data.repository.ReviewRepository
import com.example.marketelectronico.utils.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewScreen(
    navController: NavController,
    sellerId: String,
    sellerName: String
) {
    val scope = rememberCoroutineScope()
    var rating by remember { mutableDoubleStateOf(0.0) }
    var comment by remember { mutableStateOf("") }

    val currentUserId = TokenManager.getUserId()?.toString()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calificar a $sellerName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("¿Qué tal fue tu experiencia con este vendedor?", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            RatingInput(currentRating = rating, onRatingChanged = { rating = it })

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Escribe tu opinión...") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (currentUserId != null) {
                        scope.launch {
                            val success = ReviewRepository.addUserReview(
                                authorId = currentUserId,
                                targetUserId = sellerId,
                                rating = rating,
                                comment = comment
                            )
                            if (success) {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("refresh_reviews", true)

                                navController.popBackStack()
                            } else {
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = rating > 0.0 && currentUserId != null
            ) {
                Text("Publicar Reseña")
            }
        }
    }
}