package es.upc.waypass.presentation.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import es.upc.waypass.data.model.RouteDto

@Composable
fun DriverHomeScreen(
    userId: Int,
    onSubscribeClick: () -> Unit,
    onCreateCompanyClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onViewMapClick: (RouteDto) -> Unit,
    driverViewModel: DriverViewModel = hiltViewModel()
) {
    val state by driverViewModel.state.collectAsState()

    LaunchedEffect(userId) {
        if (userId != 0) {
            driverViewModel.checkCompany(userId)
        }
    }

    if (state.needsCompany) {
        onCreateCompanyClick()
    }

    if (state.company != null) {
        DriverNavigationScreen(
            userId = userId,
            companyId = state.company!!.id,
            companyName = state.company!!.name,
            onSubscribeClick = onSubscribeClick,
            onLogoutClick = onLogoutClick,
            onViewMapClick = onViewMapClick
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B3D5C))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Panel del conductor",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4FAFF))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = state.company?.name ?: "Cargando empresa...",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF0B3D5C)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ID de usuario: $userId",
                    color = Color(0xFF456979)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        DriverOptionCard("Mi empresa", "Gestiona los datos de tu empresa", Icons.Default.Business)
        DriverOptionCard("Mis paraderos", "Administra tus puntos de abordaje", Icons.Default.LocationOn)
        DriverOptionCard("Mis rutas", "Consulta y crea rutas disponibles", Icons.Default.Route)
        DriverOptionCard("Perfil", "Ver información del conductor", Icons.Default.Person)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB00020)
            )
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun DriverOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4FAFF)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF456979)
                )
            }
        }
    }
}