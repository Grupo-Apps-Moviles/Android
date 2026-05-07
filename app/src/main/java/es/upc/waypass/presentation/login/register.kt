package es.upc.waypass.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("Pasajero") }

    val authState by authViewModel.state.collectAsState()

    LaunchedEffect(authState.success) {
        if (authState.success) {
            onRegisterClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B3D5C))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF4FAFF)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {

                Text(
                    text = "Crear cuenta",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF0B3D5C)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Tipo de usuario",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF0B3D5C)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = userType == "Pasajero",
                        onClick = { userType = "Pasajero" }
                    )
                    Text("Pasajero")

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = userType == "Conductor",
                        onClick = { userType = "Conductor" }
                    )
                    Text("Conductor")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        authViewModel.register(
                            name = name,
                            email = email,
                            password = password,
                            userType = userType
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0B3D5C)
                    )
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Registrarse")
                    }
                }

                if (authState.message.isNotBlank() && !authState.success) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = authState.message,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onBackClick
                ) {
                    Text(
                        text = "Volver al login",
                        color = Color(0xFF0B3D5C)
                    )
                }
            }
        }
    }
}