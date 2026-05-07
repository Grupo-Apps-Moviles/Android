package es.upc.waypass.presentation.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DriverRoutesPlaceholderScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B3D5C))
            .padding(24.dp)
    ) {
        Text(
            text = "Rutas",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aquí conectaremos los endpoints de rutas.",
            color = Color(0xFFD6EAF8)
        )
    }
}