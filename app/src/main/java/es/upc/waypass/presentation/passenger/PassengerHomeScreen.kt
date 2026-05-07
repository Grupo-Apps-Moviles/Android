package es.upc.waypass.presentation.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PassengerHomeScreen(
    onConsultarRutasClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B3D5C))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Panel del pasajero",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Elige una funcionalidad disponible en WayPass",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD6EAF8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        PassengerOptionCard(
            title = "Consultar rutas",
            description = "Ver rutas disponibles para llegar a tu destino.",
            icon = Icons.Default.DirectionsBus,
            onClick = onConsultarRutasClick
        )

        PassengerOptionCard(
            title = "Ubicar paraderos",
            description = "Encontrar puntos de abordaje cercanos y seguros.",
            icon = Icons.Default.LocationOn
        )

        PassengerOptionCard(
            title = "Ver tarifas",
            description = "Consultar precios referenciales antes de viajar.",
            icon = Icons.Default.Payments
        )

        PassengerOptionCard(
            title = "Disponibilidad",
            description = "Revisar colectivos disponibles y horarios aproximados.",
            icon = Icons.Default.Schedule
        )
    }
}

@Composable
fun PassengerOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4FAFF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF0B3D5C),
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF0B3D5C),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF456979),
                textAlign = TextAlign.Center
            )
        }
    }
}