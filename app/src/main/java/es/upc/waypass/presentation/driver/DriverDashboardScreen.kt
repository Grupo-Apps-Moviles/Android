package es.upc.waypass.presentation.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.upc.waypass.presentation.driver.dashboard.DriverDashboardViewModel

@Composable
fun DriverDashboardScreen(
    companyId: Int,
    companyName: String,
    paddingValues: PaddingValues,
    viewModel: DriverDashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(companyId) {
        if (companyId != 0) {
            viewModel.loadDashboard(companyId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(paddingValues)
            .padding(20.dp)
    ) {
        Text(
            text = "¡Conductor, te damos la bienvenida!",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = companyName,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Resumen General",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SummaryCard(
                    title = "TARIFA PROMEDIO",
                    value = "S/ ${String.format("%.2f", state.averagePrice)}",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "TOTAL PARADEROS",
                    value = state.stopsCount.toString(),
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SummaryCard(
                    title = "TOTAL DE RUTAS",
                    value = state.routesCount.toString(),
                    icon = Icons.Default.Route,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "INTERVALO PROM.",
                    value = "${state.averageFrequency} min",
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.message.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.message,
                color = Color.Red
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(115.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF4F46E5),
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF475569)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF0F172A)
            )
        }
    }
}