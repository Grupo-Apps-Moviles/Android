package es.upc.waypass.presentation.driver.home

import androidx.compose.runtime.Composable
import es.upc.waypass.data.dto.RouteDto
import es.upc.waypass.presentation.driver.home.DriverNavigationScreen

@Composable
fun DriverHomeScreen(
    userId: Int,
    companyId: Int,
    companyName: String,
    memberRole: String,
    invitationCode: String?,
    onSubscribeClick: () -> Unit,
    onManageMembersClick: () -> Unit,
    onLeaveCompanyClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onViewMapClick: (RouteDto) -> Unit
) {
    // El companyId proviene de la membresía (resuelto por el gate), no de getCompanyByUserId.
    DriverNavigationScreen(
        userId = userId,
        companyId = companyId,
        companyName = companyName,
        memberRole = memberRole,
        invitationCode = invitationCode,
        onSubscribeClick = onSubscribeClick,
        onManageMembersClick = onManageMembersClick,
        onLeaveCompanyClick = onLeaveCompanyClick,
        onLogoutClick = onLogoutClick,
        onViewMapClick = onViewMapClick
    )
}
