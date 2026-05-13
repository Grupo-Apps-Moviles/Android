package es.upc.waypass.presentation.driver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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

    LaunchedEffect(state.needsCompany) {
        if (state.needsCompany) {
            onCreateCompanyClick()
        }
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

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}