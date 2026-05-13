package es.upc.waypass.presentation.driver

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import es.upc.waypass.presentation.driver.routes.DriverRoutesScreen
import es.upc.waypass.presentation.driver.stops.DriverStopsScreen

data class DriverBottomItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun DriverNavigationScreen(
    userId: Int,
    companyId: Int,
    companyName: String,
    onSubscribeClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onViewMapClick: (es.upc.waypass.data.model.RouteDto) -> Unit
) {
    var selectedIndex by remember { mutableStateOf(0) }

    val items = listOf(
        DriverBottomItem("Inicio", Icons.Default.Home),
        DriverBottomItem("Paraderos", Icons.Default.LocationOn),
        DriverBottomItem("Rutas", Icons.Default.Route),
        DriverBottomItem("Perfil", Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedIndex) {
            0 -> DriverDashboardScreen(
                companyId = companyId,
                companyName = companyName,
                paddingValues = paddingValues
            )

            1 -> DriverStopsScreen(
                companyId = companyId,
                paddingValues = paddingValues
            )

            2 -> DriverRoutesScreen(
                companyId = companyId,
                paddingValues = paddingValues,
                onViewMapClick = onViewMapClick
            )

            3 -> DriverProfileScreen(
                userId = userId,
                companyName = companyName,
                onSubscribeClick = onSubscribeClick,
                onLogoutClick = onLogoutClick
            )
        }
    }
}