package es.upc.waypass.data.model

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Pantalla de suscripción para conductores.
 *
 * Flujo:
 * 1. Usuario toca "Suscribirse"
 * 2. ViewModel llama al backend → recibe approvalUrl
 * 3. Compose detecta estado OpenPayPal → abre Chrome Custom Tabs
 * 4. Usuario aprueba en PayPal → regresa vía deep link waypass://paypal/success
 * 5. MainActivity detecta el deep link → llama checkSubscriptionStatus()
 */
@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel = hiltViewModel(),
    onSubscriptionActive: () -> Unit = {} // Navega a la pantalla principal
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Efecto: cuando el backend devuelve la URL, abrimos PayPal
    LaunchedEffect(uiState) {
        if (uiState is SubscriptionUiState.OpenPayPal) {
            val url = (uiState as SubscriptionUiState.OpenPayPal).approvalUrl
            openPayPalInCustomTab(context, url)
            viewModel.onPayPalOpened() // Reset para evitar re-open
        }

        if (uiState is SubscriptionUiState.Active) {
            onSubscriptionActive()
        }
    }

    // Verificar estado al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.checkSubscriptionStatus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {

            is SubscriptionUiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Procesando...")
                }
            }

            is SubscriptionUiState.Active -> {
                // No debería verse — onSubscriptionActive() navega fuera
                Text("✅ Suscripción activa hasta ${state.expiresAt ?: "N/A"}")
            }

            is SubscriptionUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "❌ ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.startSubscription() }) {
                        Text("Reintentar")
                    }
                }
            }

            // Idle o Inactive → mostrar botón de suscripción
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Activa tu suscripción",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "S/20/mes — Accede a todas las funciones premium de WayPass",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.startSubscription() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Suscribirse con PayPal")
                    }
                }
            }
        }
    }
}

/**
 * Abre la URL de aprobación de PayPal en Chrome Custom Tabs.
 * El usuario aprueba sin salir de la app y vuelve vía deep link.
 */
private fun openPayPalInCustomTab(context: Context, url: String) {
    CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
        .launchUrl(context, url.toUri())
}
