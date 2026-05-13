package es.upc.waypass.presentation.driver.routes

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import es.upc.waypass.data.dto.RouteDto
import es.upc.waypass.ui.theme.PurpleLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val GOOGLE_ROUTES_API_KEY = "AIzaSyDun8E59XSJAYnwH9SSPGUtMPhjyPaJAKU"

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
        } ?: emptyList()

    var realRoutePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var durationText by remember { mutableStateOf("${route?.duration ?: 0} min") }
    var distanceText by remember { mutableStateOf("Calculando...") }
    var isCalculating by remember { mutableStateOf(true) }
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
                errorMessage = "Se muestra ruta referencial"
            } finally {
                isCalculating = false
            }
        } else {
            isCalculating = false
        }
    }

    val initialPosition = routePoints.firstOrNull() ?: LatLng(-12.0464, -77.0428)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 13f)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Mapa de fondo ─────────────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
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
                    width = 12f,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (routePoints.size >= 2) {
                Polyline(
                    points = routePoints,
                    width = 8f,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }

        // ── Botón volver — top left ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 16.dp)
                .align(Alignment.TopStart)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // ── Badge de estado (calculando / error) — top center ────────────────
        if (isCalculating || errorMessage.isNotBlank()) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = if (errorMessage.isNotBlank())
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isCalculating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Calculando ruta...",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else if (errorMessage.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // ── Card de info — bottom ─────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Handle visual
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Título de la ruta
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PurpleLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Route,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ruta #${route?.id ?: "-"}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (route != null && route.stops.size >= 2) {
                            Text(
                                text = "${route.stops.first().name}  →  ${route.stops.last().name}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(16.dp))

                // Stats en fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MapStat(
                        icon = Icons.Outlined.Timer,
                        label = "Duración",
                        value = durationText
                    )
                    MapStatDivider()
                    MapStat(
                        icon = Icons.Outlined.Straighten,
                        label = "Distancia",
                        value = if (isCalculating) "..." else distanceText
                    )
                    MapStatDivider()
                    MapStat(
                        icon = Icons.Outlined.Place,
                        label = "Paraderos",
                        value = "${route?.stops?.size ?: 0}"
                    )
                }

                // Paraderos como chips si hay más de 0
                if (!route?.stops.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "PARADAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    route!!.stops.forEachIndexed { index, stop ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            // Número de paradero
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == 0 || index == route.stops.size - 1)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            PurpleLight
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (index == 0 || index == route.stops.size - 1)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stop.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTES AUXILIARES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MapStat(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MapStatDivider() {
    Box(
        modifier = Modifier
            .height(36.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// LÓGICA DE RED (sin cambios)
// ─────────────────────────────────────────────────────────────────────────────

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
                put("intermediates", JSONArray().apply {
                    intermediates.forEach { put(latLngWaypoint(it)) }
                })
            }
            put("travelMode", "DRIVE")
            put("routingPreference", "TRAFFIC_AWARE")
            put("computeAlternativeRoutes", false)
            put("languageCode", "es-PE")
            put("units", "METRIC")
        }

        val url = URL("https://routes.googleapis.com/directions/v2:computeRoutes")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("X-Goog-Api-Key", GOOGLE_ROUTES_API_KEY)
            connection.setRequestProperty(
                "X-Goog-FieldMask",
                "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline"
            )
            connection.doOutput = true
            connection.outputStream.use { it.write(requestJson.toString().toByteArray(Charsets.UTF_8)) }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            val json = JSONObject(responseText)
            if (json.has("error")) throw Exception("Google Routes API error: ${json.getJSONObject("error")}")
            if (!json.has("routes")) throw Exception("La respuesta no contiene routes")

            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) throw Exception("No se encontró ruta disponible")

            val route = routes.getJSONObject(0)
            val encodedPolyline = route.getJSONObject("polyline").getString("encodedPolyline")
            val durationSeconds = route.optString("duration", "0s").replace("s", "").toIntOrNull() ?: 0
            val distanceMeters = route.optInt("distanceMeters", 0)

            GoogleRouteResult(
                points = decodePolyline(encodedPolyline),
                durationText = "${durationSeconds / 60} min",
                distanceText = String.format("%.2f km", distanceMeters / 1000.0)
            )
        } finally {
            connection.disconnect()
        }
    }
}

fun latLngWaypoint(point: LatLng): JSONObject = JSONObject().apply {
    put("location", JSONObject().apply {
        put("latLng", JSONObject().apply {
            put("latitude", point.latitude)
            put("longitude", point.longitude)
        })
    })
}

fun extractLatLngFromGoogleMapsUrl(url: String?): LatLng? {
    if (url.isNullOrBlank()) return null
    val regex = Regex("(-?\\d+\\.\\d+),\\s*(-?\\d+\\.\\d+)")
    val match = regex.find(url) ?: return null
    val lat = match.groupValues[1].toDoubleOrNull()
    val lng = match.groupValues[2].toDoubleOrNull()
    return if (lat != null && lng != null) LatLng(lat, lng) else null
}

fun decodePolyline(encoded: String): List<LatLng> {
    val polyline = mutableListOf<LatLng>()
    var index = 0; val length = encoded.length
    var lat = 0; var lng = 0

    while (index < length) {
        var result = 0; var shift = 0; var b: Int
        do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
        lat += if ((result and 1) != 0) (result shr 1).inv() else result shr 1

        result = 0; shift = 0
        do { b = encoded[index++].code - 63; result = result or ((b and 0x1f) shl shift); shift += 5 } while (b >= 0x20)
        lng += if ((result and 1) != 0) (result shr 1).inv() else result shr 1

        polyline.add(LatLng(lat / 1E5, lng / 1E5))
    }
    return polyline
}