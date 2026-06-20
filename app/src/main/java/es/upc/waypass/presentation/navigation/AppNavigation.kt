package es.upc.waypass.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.upc.waypass.data.auth.TokenManager
import es.upc.waypass.data.dto.RouteDto
import es.upc.waypass.presentation.subscription.SubscriptionScreen
import es.upc.waypass.presentation.home.HomeScreen
import es.upc.waypass.presentation.login.RegisterScreen
import es.upc.waypass.presentation.membership.MembershipViewModel
import es.upc.waypass.presentation.passenger.ConsultarRutasScreen
import es.upc.waypass.presentation.passenger.PassengerHomeScreen
import es.upc.waypass.presentation.driver.home.DriverHomeScreen
import es.upc.waypass.presentation.driver.company.CompanyMembersScreen
import es.upc.waypass.presentation.driver.company.CompanyOnboardingScreen
import es.upc.waypass.presentation.driver.company.CreateCompanyScreen
import es.upc.waypass.presentation.driver.company.JoinCompanyScreen
import es.upc.waypass.presentation.driver.routes.DriverRouteMapScreen
import es.upc.waypass.presentation.splash.SplashScreen

@Composable
fun AppNavigation(
    tokenManager: TokenManager
) {
    val navController = rememberNavController()

    var loggedUserId by remember { mutableStateOf<Int?>(null) }
    var loggedRole by remember { mutableStateOf<Int?>(null) }
    var selectedRoute by remember { mutableStateOf<RouteDto?>(null) }

    // Contexto de membresía de compañía (segmento conductor)
    var companyId by remember { mutableStateOf<Int?>(null) }
    var companyName by remember { mutableStateOf<String?>(null) }
    var memberRole by remember { mutableStateOf<String?>(null) }      // "Admin"|"Driver"
    var invitationCode by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {

            SplashScreen()

            LaunchedEffect(Unit) {

                val token = tokenManager.getToken()
                val role = tokenManager.getRole()
                val userId = tokenManager.getUserId()

                loggedRole = role
                loggedUserId = userId

                when {

                    token == null -> {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }

                    role == 1 -> {
                        navController.navigate("driver_gate") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }

                    else -> {
                        navController.navigate("passenger") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            }
        }

        composable("home") {
            HomeScreen(
                padding = PaddingValues(),
                onLoginSuccess = { userId, role ->
                    loggedUserId = userId
                    loggedRole = role

                    if (role == 1) {
                        navController.navigate("driver_gate") {
                            popUpTo("home") { inclusive = true }
                        }
                    } else {
                        navController.navigate("passenger") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onRegisterClick = {
                    navController.navigate("home")
                }
            )
        }

        composable("passenger") {
            PassengerHomeScreen(
                onConsultarRutasClick = {
                    navController.navigate("consultar_rutas")
                }
            )
        }

        composable("consultar_rutas") {
            ConsultarRutasScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Gate del conductor: consulta /memberships/me y rutea
        composable("driver_gate") {
            val vm: MembershipViewModel = hiltViewModel()
            val state by vm.uiState.collectAsState()

            LaunchedEffect(Unit) { vm.loadMyMembership() }

            SplashScreen()   // spinner mientras carga

            LaunchedEffect(state.loaded) {
                if (state.loaded) {
                    val m = state.membership
                    if (m != null) {
                        companyId = m.companyId
                        companyName = m.companyName
                        memberRole = if (m.isAdmin) "Admin" else "Driver"
                        invitationCode = m.invitationCode
                        navController.navigate("driver") {
                            popUpTo("driver_gate") { inclusive = true }
                        }
                    } else {
                        navController.navigate("company_onboarding") {
                            popUpTo("driver_gate") { inclusive = true }
                        }
                    }
                }
            }
        }

        composable("company_onboarding") {
            CompanyOnboardingScreen(
                onCreateCompanyClick = { navController.navigate("create_company") },
                onJoinCompanyClick = { navController.navigate("join_company") },
                onLogoutClick = {
                    tokenManager.clearSession()
                    loggedUserId = null
                    loggedRole = null
                    companyId = null
                    companyName = null
                    memberRole = null
                    invitationCode = null
                    navController.navigate("home") { popUpTo(0) }
                }
            )
        }

        composable("join_company") {
            JoinCompanyScreen(
                onJoined = {
                    navController.navigate("driver_gate") {
                        popUpTo("company_onboarding") { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("driver") {
            DriverHomeScreen(
                userId = loggedUserId ?: 0,
                companyId = companyId ?: 0,
                companyName = companyName ?: "",
                memberRole = memberRole ?: "Driver",
                invitationCode = invitationCode,

                onSubscribeClick = {
                    navController.navigate("subscription")
                },

                onManageMembersClick = {
                    navController.navigate("company_members")
                },

                onLeaveCompanyClick = {
                    navController.navigate("driver_gate") {
                        popUpTo("driver") { inclusive = true }
                    }
                },

                onLogoutClick = {

                    tokenManager.clearSession()

                    loggedUserId = null
                    loggedRole = null
                    companyId = null
                    companyName = null
                    memberRole = null
                    invitationCode = null

                    navController.navigate("home") {
                        popUpTo(0)
                    }
                },

                onViewMapClick = { route ->
                    selectedRoute = route
                    navController.navigate("driver_route_map")
                }
            )
        }

        composable("company_members") {
            CompanyMembersScreen(
                companyId = companyId ?: 0,
                invitationCode = invitationCode,
                onBackClick = { navController.popBackStack() },
                onCodeRegenerated = { newCode -> invitationCode = newCode }
            )
        }

        composable("subscription") {
            SubscriptionScreen(
                onSubscriptionActive = {
                    navController.navigate("driver") {
                        popUpTo("subscription") { inclusive = true }
                    }
                }
            )
        }

        composable("create_company") {
            CreateCompanyScreen(
                userId = loggedUserId ?: 0,
                onCompanyCreated = {
                    navController.navigate("driver_gate") {
                        popUpTo("company_onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("driver_route_map") {
            DriverRouteMapScreen(
                route = selectedRoute,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
