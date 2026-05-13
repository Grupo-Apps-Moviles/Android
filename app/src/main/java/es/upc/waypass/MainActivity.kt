package es.upc.waypass

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import es.upc.waypass.presentation.navigation.AppNavigation

@AndroidEntryPoint
class MainActivity : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppNavigation()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handlePayPalDeepLink(intent)
    }

    private fun handlePayPalDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "waypass" && data.host == "paypal") {
            when (data.path) {
                "/success" -> {
                    // PayPal aprobó — el webhook ya actualizó el backend.
                    // Solo navegamos a subscription para que el ViewModel
                    // llame a checkSubscriptionStatus() y confirme con el backend.
                    // navController no está accesible aquí directamente,
                    // así que usamos un Intent:
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            putExtra("paypal_result", "success")
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                    )
                }
                "/cancel" -> {
                    // El conductor canceló en PayPal, no hacemos nada especial
                }
            }
        }
    }
}