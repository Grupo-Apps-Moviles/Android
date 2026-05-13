package es.upc.waypass.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.upc.waypass.data.auth.TokenManager
import es.upc.waypass.data.dto.RouteDto
import es.upc.waypass.presentation.subscription.SubscriptionScreen
import es.upc.waypass.presentation.home.HomeScreen
import es.upc.waypass.presentation.login.RegisterScreen
import es.upc.waypass.presentation.passenger.ConsultarRutasScreen
import es.upc.waypass.presentation.passenger.PassengerHomeScreen
import es.upc.waypass.presentation.driver.home.DriverHomeScreen
import es.upc.waypass.presentation.driver.company.CreateCompanyScreen
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
                        navController.navigate("driver") {
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
                        navController.navigate("driver") {
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

        composable("driver") {
            DriverHomeScreen(
                userId = loggedUserId ?: 0,

                onSubscribeClick = {
                    navController.navigate("subscription")
                },

                onCreateCompanyClick = {
                    navController.navigate("create_company")
                },

                onLogoutClick = {

                    tokenManager.clearSession()

                    loggedUserId = null
                    loggedRole = null

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
                    navController.navigate("driver") {
                        popUpTo("driver") { inclusive = true }
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