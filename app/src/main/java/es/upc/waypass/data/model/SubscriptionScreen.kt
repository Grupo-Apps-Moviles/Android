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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import es.upc.waypass.MainActivity

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel = hiltViewModel(),
    onSubscriptionActive: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (MainActivity.paypalResult == "success") {
                    MainActivity.paypalResult = null
                    onSubscriptionActive()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is SubscriptionUiState.OpenPayPal) {
            val url = (uiState as SubscriptionUiState.OpenPayPal).approvalUrl
            openPayPalInCustomTab(context, url)
            viewModel.onPayPalOpened()
        }

        if (uiState is SubscriptionUiState.Active) {
            onSubscriptionActive()
        }
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

private fun openPayPalInCustomTab(context: Context, url: String) {
    CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
        .launchUrl(context, url.toUri())
}