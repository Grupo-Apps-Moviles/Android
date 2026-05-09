package es.upc.waypass.presentation.driver.routes

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import es.upc.waypass.data.model.RouteDto
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment

@Composable
fun DriverRouteMapScreen(
    route: RouteDto?,
    onBackClick: () -> Unit = {}
) {

    Log.d("WAYPASS_MAP", "ROUTE: $route")

    val routePoints = route?.stops
        ?.mapNotNull { stop ->

            Log.d(
                "WAYPASS_MAP",
                "STOP URL -> ${stop.googleMapsUrl}"
            )

            val position =
                extractLatLngFromGoogleMapsUrl(stop.googleMapsUrl)

            Log.d(
                "WAYPASS_MAP",
                "PARSED POSITION -> $position"
            )

            position
        }
        ?: emptyList()

    Log.d(
        "WAYPASS_MAP",
        "FINAL ROUTE POINTS -> $routePoints"
    )

    val initialPosition =
        routePoints.firstOrNull()
            ?: LatLng(-12.0464, -77.0428)

    val cameraPositionState = rememberCameraPositionState {
        position =
            CameraPosition.fromLatLngZoom(initialPosition, 13f)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {

            route?.stops?.forEach { stop ->

                val position =
                    extractLatLngFromGoogleMapsUrl(stop.googleMapsUrl)

                if (position != null) {

                    Log.d(
                        "WAYPASS_MAP",
                        "DRAWING MARKER -> ${stop.name} : $position"
                    )

                    Marker(
                        state = rememberMarkerState(position = position),
                        title = stop.name,
                        snippet = stop.address
                    )
                } else {

                    Log.d(
                        "WAYPASS_MAP",
                        "MARKER FAILED -> ${stop.name}"
                    )
                }
            }

            if (routePoints.size >= 2) {

                Log.d(
                    "WAYPASS_MAP",
                    "DRAWING POLYLINE -> $routePoints"
                )

                Polyline(
                    points = routePoints,
                    width = 8f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("← Volver")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = MaterialTheme.shapes.large
            ) {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {

                    Text("Ruta #${route?.id ?: "-"}")

                    Text(
                        "Paraderos: ${route?.stops?.size ?: 0}"
                    )

                    Text(
                        "Duración estimada: ${route?.duration ?: 0} min"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Puntos detectados: ${routePoints.size}"
                    )
                }
            }
        }
    }
}

fun extractLatLngFromGoogleMapsUrl(
    url: String?
): LatLng? {

    Log.d(
        "WAYPASS_MAP",
        "EXTRACT FUNCTION URL -> $url"
    )

    if (url.isNullOrBlank()) {

        Log.d(
            "WAYPASS_MAP",
            "URL IS NULL"
        )

        return null
    }

    val parts = url.split(",")

    Log.d(
        "WAYPASS_MAP",
        "SPLIT PARTS -> ${parts.toList()}"
    )

    if (parts.size != 2) {

        Log.d(
            "WAYPASS_MAP",
            "INVALID PARTS SIZE"
        )

        return null
    }

    val lat =
        parts[0].trim().toDoubleOrNull()

    val lng =
        parts[1].trim().toDoubleOrNull()

    Log.d(
        "WAYPASS_MAP",
        "LAT -> $lat | LNG -> $lng"
    )

    return if (lat != null && lng != null) {

        LatLng(lat, lng)

    } else {

        Log.d(
            "WAYPASS_MAP",
            "FAILED TO PARSE LAT LNG"
        )

        null
    }
}