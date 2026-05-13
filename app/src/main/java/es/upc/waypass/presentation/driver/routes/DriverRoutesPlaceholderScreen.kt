package es.upc.waypass.presentation.driver.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DriverRoutesPlaceholderScreen() {
    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .padding(24.dp)
    ) {
        Text(
            text = "Rutas",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Companion.White
        )

        Spacer(modifier = Modifier.Companion.height(16.dp))

        Text(
            text = "Aquí conectaremos los endpoints de rutas.",
            color = Color(0xFFD6EAF8)
        )
    }
}