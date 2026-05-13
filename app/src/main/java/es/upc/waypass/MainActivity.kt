package es.upc.waypass

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import es.upc.waypass.data.auth.TokenManager
import es.upc.waypass.presentation.navigation.AppNavigation
import es.upc.waypass.ui.theme.WayPassTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    companion object {
        var paypalResult: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handlePayPalDeepLink(intent)

        setContent {
            WayPassTheme {
                AppNavigation(tokenManager)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePayPalDeepLink(intent)
    }

    private fun handlePayPalDeepLink(intent: Intent?) {
        val data = intent?.data ?: return

        if (data.scheme == "waypass" && data.host == "paypal") {
            when (data.path) {
                "/success" -> {
                    paypalResult = "success"
                }

                "/cancel" -> {
                    paypalResult = "cancel"
                }
            }
        }
    }
}