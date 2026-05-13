package es.upc.waypass.presentation.subscription

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                if (MainActivity.Companion.paypalResult == "success") {
                    MainActivity.Companion.paypalResult = null
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
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Companion.Center
    ) {
        when (val state = uiState) {

            is SubscriptionUiState.Loading -> {
                Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.Companion.height(16.dp))
                    Text("Procesando...")
                }
            }

            is SubscriptionUiState.Active -> {
                Text("✅ Suscripción activa hasta ${state.expiresAt ?: "N/A"}")
            }

            is SubscriptionUiState.Error -> {
                Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                    Text(
                        text = "❌ ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Companion.Center
                    )
                    Spacer(Modifier.Companion.height(16.dp))
                    Button(onClick = { viewModel.startSubscription() }) {
                        Text("Reintentar")
                    }
                }
            }

            else -> {
                Column(
                    horizontalAlignment = Alignment.Companion.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Activa tu suscripción",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.Companion.height(8.dp))
                    Text(
                        text = "S/20/mes — Accede a todas las funciones premium de WayPass",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Companion.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.Companion.height(32.dp))
                    Button(
                        onClick = { viewModel.startSubscription() },
                        modifier = Modifier.Companion
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