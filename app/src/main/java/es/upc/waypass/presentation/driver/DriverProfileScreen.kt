package es.upc.waypass.presentation.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DriverProfileScreen(
    userId: Int,
    companyName: String,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B3D5C))
            .padding(24.dp)
    ) {
        Text(
            text = "Perfil",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4FAFF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("ID usuario: $userId")
                Text("Empresa: $companyName")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
        ) {
            Text("Cerrar sesión")
        }
    }
}