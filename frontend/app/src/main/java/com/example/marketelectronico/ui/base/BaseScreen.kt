package com.example.marketelectronico.ui.base

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseScreen(
    title: String,
    // --- INICIO DE LA CORRECCIÓN ---
    // 1. Hacemos que el NavController sea "opcional" (nullable)
    //    y tenga un valor por defecto de "null".
    navController: NavController? = null,
    // --- FIN DE LA CORRECCIÓN ---
    modifier: Modifier = Modifier,
    content: @Composable (padding: PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),

                navigationIcon = {
                    // --- INICIO DE LA CORRECCIÓN ---
                    // 2. Solo mostramos la flecha de "atrás" si el NavController
                    //    NO es nulo Y si podemos volver atrás.
                    if (navController != null && navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                    // --- FIN DE LA CORRECCIÓN ---
                }
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Preview(showBackground = true)
@Composable
fun BaseScreenPreview() {
    // Le pasamos un NavController de prueba a la vista previa
    MarketElectronicoTheme {
        BaseScreen(
            title = "Vista Previa",
            navController = rememberNavController()
        ) { padding ->
            Text(
                text = "El contenido de la pantalla va aquí.",
                modifier = Modifier.padding(padding)
            )
        }
    }
}