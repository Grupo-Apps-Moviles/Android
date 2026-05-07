package es.upc.waypass.presentation.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DriverDashboardScreen(
    companyName: String,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B3D5C))
            .padding(paddingValues)
            .padding(24.dp)
    ) {
        Text(
            text = "Inicio",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        DashboardCard(
            title = companyName,
            description = "Empresa registrada",
            icon = Icons.Default.Business
        )

        DashboardCard(
            title = "Paraderos",
            description = "Gestiona tus puntos de abordaje",
            icon = Icons.Default.LocationOn
        )

        DashboardCard(
            title = "Rutas",
            description = "Administra rutas, horarios y tarifas",
            icon = Icons.Default.Route
        )
    }
}

@Composable
fun DashboardCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4FAFF))
    ) {
        Row(
            modifier = Modifier.padding(18.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF0B3D5C),
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0B3D5C)
                )
                Text(
                    text = description,
                    color = Color(0xFF456979)
                )
            }
        }
    }
}