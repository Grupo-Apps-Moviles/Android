package es.upc.waypass.presentation.driver.company

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import es.upc.waypass.presentation.driver.DriverViewModel

@Composable
fun CreateCompanyScreen(
    userId: Int,
    onCompanyCreated: () -> Unit,
    driverViewModel: DriverViewModel = hiltViewModel()
) {
    var companyName by remember { mutableStateOf("") }
    val state by driverViewModel.state.collectAsState()

    LaunchedEffect(state.company) {
        if (state.company != null) {
            onCompanyCreated()
        }
    }

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .padding(24.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.Companion.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.Companion.padding(24.dp),
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                Text(
                    text = "Crear empresa",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.Companion.height(8.dp))

                Text(
                    text = "Antes de gestionar rutas, registra tu empresa o servicio de transporte.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF456979)
                )

                Spacer(modifier = Modifier.Companion.height(24.dp))

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Nombre de empresa") },
                    modifier = Modifier.Companion.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Companion.Words
                    )
                )

                if (state.message.isNotBlank() && state.company == null) {
                    Spacer(modifier = Modifier.Companion.height(8.dp))
                    Text(
                        text = state.message,
                        color = Color.Companion.Red
                    )
                }

                Spacer(modifier = Modifier.Companion.height(24.dp))

                Button(
                    onClick = {
                        driverViewModel.createCompany(companyName, userId)
                    },
                    modifier = Modifier.Companion.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color.Companion.White,
                            modifier = Modifier.Companion.size(20.dp)
                        )
                    } else {
                        Text("Crear empresa")
                    }
                }
            }
        }
    }
}