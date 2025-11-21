package com.example.marketelectronico.ui.publish

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.marketelectronico.data.repository.ProductRepository
import com.example.marketelectronico.utils.TokenManager
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(navController: NavController) {
    val repo = ProductRepository()
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    var categories by remember { mutableStateOf<List<com.example.marketelectronico.data.model.Category>>(emptyList()) }
    var loadingCats by remember { mutableStateOf(true) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loadingCats = true
            try {
            categories = repo.getCategories()
            if (categories.isNotEmpty()) {
                selectedCategoryId = categories.first().id
                selectedCategoryName = categories.first().nombre
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loadingCats = false
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Publicar producto") })
    }) { inner ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .padding(16.dp), verticalArrangement = Arrangement.Top) {

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = precioText,
                onValueChange = { precioText = it },
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Now this will be resolved
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (loadingCats) {
                Text("Cargando categorías...")
            } else {
                // Dropdown sencillo usando Box + DropdownMenu para evitar problemas de compatibilidad
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategoryName ?: "",
                        onValueChange = { /* readOnly */ },
                        label = { Text("Categoría") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Abrir categorías")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (categories.isEmpty()) {
                            DropdownMenuItem(text = { Text("No hay categorías") }, onClick = { expanded = false })
                        } else {
                            categories.forEach { c ->
                                DropdownMenuItem(text = { Text(c.nombre) }, onClick = {
                                    selectedCategoryId = c.id
                                    selectedCategoryName = c.nombre
                                    expanded = false
                                })
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (error != null) {
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(onClick = {
                // submit
                val price = precioText.toDoubleOrNull() ?: 0.0
                if (nombre.isBlank() || descripcion.isBlank() || price <= 0.0 || selectedCategoryId == null) {
                    error = "Completa todos los campos correctamente"
                    return@Button
                }
                submitting = true
                scope.launch {
                    try {
                        // Obtener el id del usuario logueado desde TokenManager
                        val userIdLong = TokenManager.getUserId()
                        val userId = userIdLong?.toInt()
                        if (userId == null) {
                            error = "Usuario no autenticado"
                            submitting = false
                            return@launch
                        }

                        val created = repo.createProduct(nombre, descripcion, price, userId, 1, selectedCategoryId!!)
                        if (created != null) {
                            // signal main to refresh
                            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                            // También intentamos notificar a la pantalla de perfil, si existe
                            runCatching {
                                val profileEntry = navController.getBackStackEntry("profile")
                                profileEntry.savedStateHandle.set("refresh", true)
                            }
                            navController.popBackStack()
                        } else {
                            error = "No se pudo crear el producto"
                        }
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        submitting = false
                    }
                }
            }, modifier = Modifier.align(Alignment.End)) {
                if (submitting) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Publicar")
            }
        }
    }
}
