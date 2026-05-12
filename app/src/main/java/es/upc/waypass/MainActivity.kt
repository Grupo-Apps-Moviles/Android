package es.upc.waypass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import es.upc.waypass.presentation.navigation.AppNavigation
import es.upc.waypass.ui.theme.WayPassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WayPassTheme {
                AppNavigation()
            }
        }
    }
}