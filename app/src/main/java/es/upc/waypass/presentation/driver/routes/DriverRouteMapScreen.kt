package es.upc.waypass.presentation.driver.routes

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import es.upc.waypass.data.model.RouteDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val GOOGLE_ROUTES_API_KEY = "AIzaSyAuTncWDz3h-Qkr32CZmk6gJZqqi7ev56A"

@Composable
fun DriverRouteMapScreen(
    route: RouteDto?,
    onBackClick: () -> Unit = {}
) {
    val routePoints = route?.stops
        ?.mapNotNull { stop ->
            val coordinates = stop.googleMapsUrl?.takeIf { it.isNotBlank() }
                ?: stop.imageUrl?.takeIf { it.isNotBlank() }

            extractLatLngFromGoogleMapsUrl(coordinates)
        }
        ?: emptyList()

    var realRoutePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var durationText by remember { mutableStateOf("${route?.duration ?: 0} min") }
    var distanceText by remember { mutableStateOf("Calculando...") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(route?.id) {
        if (routePoints.size >= 2) {
            try {
                val result = fetchGoogleRoute(routePoints)
                realRoutePoints = result.points
                durationText = result.durationText
                distanceText = result.distanceText
                errorMessage = ""
            } catch (e: Exception) {
                Log.e("WAYPASS_MAP", "Routes API error", e)
                realRoutePoints = routePoints
                distanceText = "No disponible"
                errorMessage = "No se pudo calcular la ruta real. Se muestra línea referencial."
            }
        }
    }

    val initialPosition = routePoints.firstOrNull() ?: LatLng(-12.0464, -77.0428)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 13f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            routePoints.forEachIndexed { index, point ->
                val stop = route?.stops?.getOrNull(index)

                Marker(
                    state = rememberMarkerState(position = point),
                    title = stop?.name ?: "Paradero ${index + 1}",
                    snippet = stop?.address ?: ""
                )
            }

            if (realRoutePoints.size >= 2) {
                Polyline(
                    points = realRoutePoints,
                    width = 10f,
                    color = Color(0xFF4F46E5)
                )
            } else if (routePoints.size >= 2) {
                Polyline(
                    points = routePoints,
                    width = 8f,
                    color = Color(0xFF6B7280)
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
            Button(onClick = onBackClick) {
                Text("← Volver")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Ruta #${route?.id ?: "-"}")
                    Text("Paraderos: ${route?.stops?.size ?: 0}")
                    Text("Duración aproximada: $durationText")
                    Text("Distancia aproximada: $distanceText")

                    if (errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

data class GoogleRouteResult(
    val points: List<LatLng>,
    val durationText: String,
    val distanceText: String
)

suspend fun fetchGoogleRoute(points: List<LatLng>): GoogleRouteResult {
    return withContext(Dispatchers.IO) {
        val origin = points.first()
        val destination = points.last()
        val intermediates = points.drop(1).dropLast(1)

        val requestJson = JSONObject().apply {
            put("origin", latLngWaypoint(origin))
            put("destination", latLngWaypoint(destination))

            if (intermediates.isNotEmpty()) {
                put(
                    "intermediates",
                    JSONArray().apply {
                        intermediates.forEach { point ->
                            put(latLngWaypoint(point))
                        }
                    }
                )
            }

            put("travelMode", "DRIVE")
            put("routingPreference", "TRAFFIC_AWARE")
            put("computeAlternativeRoutes", false)
            put("languageCode", "es-PE")
            put("units", "METRIC")
        }

        val url = URL("https://routes.googleapis.com/directions/v2:computeRoutes")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("X-Goog-Api-Key", GOOGLE_ROUTES_API_KEY)
        connection.setRequestProperty(
            "X-Goog-FieldMask",
            "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline"
        )
        connection.doOutput = true

        connection.outputStream.use { output ->
            output.write(requestJson.toString().toByteArray())
        }

        val responseCode = connection.responseCode
        val responseText = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }

        if (responseCode !in 200..299) {
            throw Exception("HTTP $responseCode: $responseText")
        }

        val json = JSONObject(responseText)
        val routes = json.getJSONArray("routes")

        if (routes.length() == 0) {
            throw Exception("No se encontró ruta")
        }

        val route = routes.getJSONObject(0)
        val encodedPolyline = route
            .getJSONObject("polyline")
            .getString("encodedPolyline")

        val decodedPoints = decodePolyline(encodedPolyline)

        val durationSeconds = route
            .optString("duration", "0s")
            .replace("s", "")
            .toIntOrNull() ?: 0

        val distanceMeters = route.optInt("distanceMeters", 0)

        GoogleRouteResult(
            points = decodedPoints,
            durationText = "${durationSeconds / 60} min",
            distanceText = String.format("%.2f km", distanceMeters / 1000.0)
        )
    }
}

fun latLngWaypoint(point: LatLng): JSONObject {
    return JSONObject().apply {
        put(
            "location",
            JSONObject().apply {
                put(
                    "latLng",
                    JSONObject().apply {
                        put("latitude", point.latitude)
                        put("longitude", point.longitude)
                    }
                )
            }
        )
    }
}

fun extractLatLngFromGoogleMapsUrl(url: String?): LatLng? {
    if (url.isNullOrBlank()) return null

    val regex = Regex("(-?\\d+\\.\\d+),\\s*(-?\\d+\\.\\d+)")
    val match = regex.find(url) ?: return null

    val lat = match.groupValues[1].toDoubleOrNull()
    val lng = match.groupValues[2].toDoubleOrNull()

    return if (lat != null && lng != null) {
        LatLng(lat, lng)
    } else {
        null
    }
}

fun decodePolyline(encoded: String): List<LatLng> {
    val polyline = mutableListOf<LatLng>()
    var index = 0
    val length = encoded.length
    var lat = 0
    var lng = 0

    while (index < length) {
        var result = 0
        var shift = 0
        var b: Int

        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)

        val deltaLat = if ((result and 1) != 0) {
            (result shr 1).inv()
        } else {
            result shr 1
        }

        lat += deltaLat

        result = 0
        shift = 0

        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)

        val deltaLng = if ((result and 1) != 0) {
            (result shr 1).inv()
        } else {
            result shr 1
        }

        lng += deltaLng

        polyline.add(
            LatLng(
                lat / 1E5,
                lng / 1E5
            )
        )
    }

    return polyline
}