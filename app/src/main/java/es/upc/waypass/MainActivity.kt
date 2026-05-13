package es.upc.waypass

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import es.upc.waypass.presentation.navigation.AppNavigation
import es.upc.waypass.ui.theme.WayPassTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handlePayPalDeepLink(intent)

        setContent {
            WayPassTheme {
                AppNavigation()
            }
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
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            putExtra("paypal_result", "success")
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                    )
                }

                "/cancel" -> {
                    // El conductor canceló en PayPal
                }
            }
        }
    }
}