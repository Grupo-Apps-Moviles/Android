package es.upc.waypass.presentation.driver.stops

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapPickerScreen(
    onLocationSelected: (String) -> Unit
) {
    var markerPosition by remember {
        mutableStateOf(LatLng(-12.0464, -77.0428)) // Lima centro
    }

    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            markerPosition,
            13f
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            markerPosition = latLng
            onLocationSelected("${latLng.latitude},${latLng.longitude}")
        }
    ) {
        Marker(
            state = MarkerState(position = markerPosition),
            title = "Ubicación seleccionada"
        )
    }
}