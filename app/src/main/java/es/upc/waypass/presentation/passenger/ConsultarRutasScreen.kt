package es.upc.waypass.presentation.passenger

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun ConsultarRutasScreen(
    onBackClick: () -> Unit = {}
) {

    val lima = LatLng(-12.0464, -77.0428)
    val ves = LatLng(-12.1586, -76.9918)
    val lurin = LatLng(-12.2719, -76.8696)

    var currentLocation by remember { mutableStateOf(lima) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 12f)
    }

    // 🔄 Actualiza la cámara cuando cambia ubicación
    LaunchedEffect(currentLocation) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 12f)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {

            Marker(
                state = rememberMarkerState(position = lima),
                title = "Centro de Lima"
            )

            Marker(
                state = rememberMarkerState(position = ves),
                title = "Villa El Salvador"
            )

            Marker(
                state = rememberMarkerState(position = lurin),
                title = "Lurín"
            )
        }

        // 🔙 BOTÓN VOLVER
        Button(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text("← Volver")
        }

        // 🔄 BOTONES DE RUTAS
        Column(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(16.dp)
        ) {

            Button(
                onClick = { currentLocation = lima },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir a Lima")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { currentLocation = ves },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir a VES")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { currentLocation = lurin },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir a Lurín")
            }
        }
    }
}