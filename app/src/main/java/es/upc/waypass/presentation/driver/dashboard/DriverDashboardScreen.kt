package es.upc.waypass.presentation.driver.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DriverDashboardScreen(
    companyId: Int,
    companyName: String,
    paddingValues: PaddingValues,
    viewModel: DriverDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(companyId) {
        if (companyId != 0) viewModel.loadDashboard(companyId)
    }

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {

        // ── Header: saludo + avatar ───────────────────────────────────────────
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Column(modifier = Modifier.Companion.weight(1f)) {
                Text(
                    text = "¡Bienvenido!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.Companion.height(2.dp))
                Text(
                    text = companyName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Companion.SemiBold
                )
            }

            // Avatar con inicial de la empresa
            Box(
                modifier = Modifier.Companion
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text(
                    text = companyName.firstOrNull()?.uppercaseChar()?.toString() ?: "W",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Companion.Bold
                )
            }
        }

        Spacer(modifier = Modifier.Companion.height(24.dp))

        // ── Sección: Resumen ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Text(
                text = "Resumen General",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.Companion.weight(1f)
            )
//            if (!state.isLoading) {
//                Text(
//                    text = "Actualizado",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
        }

        Spacer(modifier = Modifier.Companion.height(14.dp))

        // ── Cards de stats ────────────────────────────────────────────────────
        if (state.isLoading) {
            // Skeleton loading
            repeat(2) {
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    repeat(2) {
                        SkeletonCard(modifier = Modifier.Companion.weight(1f))
                    }
                }
                if (it == 0) Spacer(modifier = Modifier.Companion.height(14.dp))
            }
        } else {
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SummaryCard(
                    title = "Tarifa Promedio",
                    value = "S/ ${String.format("%.2f", state.averagePrice)}",
                    icon = Icons.Outlined.AttachMoney,
                    //accentColor = Color(0xFF0EA5E9), color azul
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.Companion.weight(1f)
                )
                SummaryCard(
                    title = "Paraderos",
                    value = state.stopsCount.toString(),
                    icon = Icons.Outlined.LocationOn,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.Companion.weight(1f)
                )
            }

            Spacer(modifier = Modifier.Companion.height(14.dp))

            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SummaryCard(
                    title = "Rutas Activas",
                    value = state.routesCount.toString(),
                    icon = Icons.Outlined.Route,
                    //accentColor = Color(0xFF10B981), //color verde
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.Companion.weight(1f)
                )
                SummaryCard(
                    title = "Intervalo Prom.",
                    value = "${state.averageFrequency} min",
                    icon = Icons.Outlined.Schedule,
                    //accentColor = Color(0xFFF59E0B), //color naranja
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.Companion.weight(1f)
                )
            }
        }

        // ── Error ─────────────────────────────────────────────────────────────
        if (state.message.isNotBlank()) {
            Spacer(modifier = Modifier.Companion.height(12.dp))
            Row(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.Companion.size(16.dp)
                )
                Spacer(modifier = Modifier.Companion.width(8.dp))
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.Companion.height(24.dp))

//        // ── Banner de empresa ─────────────────────────────────────────────────
//        CompanyBannerCard(companyName = companyName)
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // ── Accesos rápidos ───────────────────────────────────────────────────
//        Text(
//            text = "Accesos Rápidos",
//            style = MaterialTheme.typography.titleMedium,
//            color = MaterialTheme.colorScheme.onSurface
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))

//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            QuickAccessCard(
//                icon = Icons.Outlined.Route,
//                label = "Rutas",
//                modifier = Modifier.weight(1f)
//            )
//            QuickAccessCard(
//                icon = Icons.Outlined.LocationOn,
//                label = "Paraderos",
//                modifier = Modifier.weight(1f)
//            )
//            QuickAccessCard(
//                icon = Icons.Outlined.Person,
//                label = "Perfil",
//                modifier = Modifier.weight(1f)
//            )
//        }

        Spacer(modifier = Modifier.Companion.height(80.dp))
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier.Companion
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Companion.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.Companion.fillMaxSize()) {
            // Barra de acento izquierda
            Box(
                modifier = Modifier.Companion
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Column(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Ícono en chip redondeado
                Box(
                    modifier = Modifier.Companion
                        .size(34.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.12f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.Companion.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(modifier = Modifier.Companion.height(2.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Companion.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SkeletonCard(modifier: Modifier = Modifier.Companion) {
    Card(
        modifier = modifier.height(120.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {}
}