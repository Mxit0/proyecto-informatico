package com.example.marketelectronico.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.marketelectronico.ui.theme.MarketElectronicoTheme
import kotlinx.coroutines.delay
private enum class AuthSuccessState { Idle, SuccessRegister, SuccessLogin }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory())
) {
    val uiState by viewModel.ui.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var authSuccessState by remember { mutableStateOf(AuthSuccessState.Idle) }

    LaunchedEffect(authSuccessState) {
        when (authSuccessState) {
            AuthSuccessState.SuccessRegister -> {
                snackbarHostState.showSnackbar("¡Registro exitoso! Bienvenido.")
                delay(2000L)

                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            }
            AuthSuccessState.SuccessLogin -> {
                delay(500L)

                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            }
            AuthSuccessState.Idle -> {}
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bienvenido a TechTrade",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(24.dp))

            TabRow(
                selectedTabIndex = if (viewModel.isRegisterTabSelected) 1 else 0,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = !viewModel.isRegisterTabSelected,
                    onClick = { viewModel.isRegisterTabSelected = false },
                    text = { Text("Ingresar") }
                )
                Tab(
                    selected = viewModel.isRegisterTabSelected,
                    onClick = { viewModel.isRegisterTabSelected = true },
                    text = { Text("Registrar") }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.isRegisterTabSelected) {
                TextField(
                    value = viewModel.nombre,
                    onValueChange = { viewModel.nombre = it },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = authTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.error != null
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            TextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = { Text("Correo Electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = authTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                isError = uiState.error != null
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = authTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                isError = uiState.error != null
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val onSuccess: (String) -> Unit = { token ->
                        // TODO: Guardar el token
                        authSuccessState = if (viewModel.isRegisterTabSelected) {
                            AuthSuccessState.SuccessRegister // <-- CORREGIDO
                        } else {
                            AuthSuccessState.SuccessLogin // <-- CORREGIDO
                        }
                    }

                    if (viewModel.isRegisterTabSelected) {
                        viewModel.register(onSuccess = onSuccess)
                    } else {
                        viewModel.login(onSuccess = onSuccess)
                    }
                },
                enabled = !uiState.loading && authSuccessState == AuthSuccessState.Idle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                when {
                    uiState.loading -> {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    }
                    authSuccessState != AuthSuccessState.Idle -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Éxito"
                        )
                    }
                    else -> {
                        Text(
                            text = if (viewModel.isRegisterTabSelected) "Registrar" else "Ingresar",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun authTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    errorContainerColor = MaterialTheme.colorScheme.surface,
    errorIndicatorColor = Color.Transparent
)

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MarketElectronicoTheme {
        LoginScreen(navController = rememberNavController())
    }
}