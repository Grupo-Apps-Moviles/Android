package es.upc.waypass.presentation.driver.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.upc.waypass.data.model.CreateScheduleRequest
import es.upc.waypass.data.model.RouteDto

data class ScheduleFormState(
    val day: String,
    var enabled: Boolean = false,
    var startTime: String = "",
    var endTime: String = ""
)

@Composable
fun DriverRoutesScreen(
    companyId: Int,
    paddingValues: PaddingValues,
    onViewMapClick: (RouteDto) -> Unit = {},
    viewModel: DriverRoutesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    var showForm by remember { mutableStateOf(false) }

    var price by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    val selectedStops = remember { mutableStateListOf<Int>() }

    val schedules = remember {
        mutableStateListOf(
            ScheduleFormState("Lunes"),
            ScheduleFormState("Martes"),
            ScheduleFormState("Miércoles"),
            ScheduleFormState("Jueves"),
            ScheduleFormState("Viernes"),
            ScheduleFormState("Sábado"),
            ScheduleFormState("Domingo")
        )
    }

    LaunchedEffect(companyId) {
        if (companyId != 0) {
            viewModel.loadData(companyId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B3D5C))
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Mis rutas",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Crea y administra rutas asociadas a tus paraderos.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD6EAF8)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showForm = !showForm },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF4FAFF),
                contentColor = Color(0xFF0B3D5C)
            )
        ) {
            Text(if (showForm) "Ocultar formulario" else "Crear ruta")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showForm) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF4FAFF)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Precio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = frequency,
                        onValueChange = { frequency = it },
                        label = { Text("Frecuencia en minutos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duración estimada en minutos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Selecciona paraderos",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF0B3D5C)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.stops.isEmpty()) {
                        Text(
                            text = "No hay paraderos disponibles. Primero crea paraderos.",
                            color = Color.Red
                        )
                    }

                    state.stops.forEach { stop ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedStops.contains(stop.id),
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedStops.add(stop.id)
                                    } else {
                                        selectedStops.remove(stop.id)
                                    }
                                }
                            )

                            Column {
                                Text(
                                    text = stop.name,
                                    color = Color(0xFF0B3D5C)
                                )
                                Text(
                                    text = stop.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF456979)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Horarios de atención",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF0B3D5C)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    schedules.forEachIndexed { index, schedule ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = schedule.enabled,
                                        onCheckedChange = { checked ->
                                            schedules[index] = schedule.copy(enabled = checked)
                                        }
                                    )

                                    Text(
                                        text = schedule.day,
                                        color = Color(0xFF0B3D5C),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }

                                if (schedule.enabled) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = schedule.startTime,
                                        onValueChange = { value ->
                                            schedules[index] = schedule.copy(startTime = value)
                                        },
                                        label = { Text("Hora inicio, ejemplo 08:00") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = schedule.endTime,
                                        onValueChange = { value ->
                                            schedules[index] = schedule.copy(endTime = value)
                                        },
                                        label = { Text("Hora fin, ejemplo 18:00") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val selectedSchedules = schedules
                                .filter { it.enabled }
                                .map {
                                    CreateScheduleRequest(
                                        dayOfWeek = it.day,
                                        startTime = it.startTime,
                                        endTime = it.endTime,
                                        enabled = true
                                    )
                                }

                            viewModel.createRoute(
                                companyId = companyId,
                                price = price.toDoubleOrNull() ?: 0.0,
                                frequency = frequency.toIntOrNull() ?: 0,
                                duration = duration.toIntOrNull() ?: 0,
                                stopsIds = selectedStops.toList(),
                                schedules = selectedSchedules
                            )

                            price = ""
                            frequency = ""
                            duration = ""
                            selectedStops.clear()

                            schedules.forEachIndexed { index, item ->
                                schedules[index] = item.copy(
                                    enabled = false,
                                    startTime = "",
                                    endTime = ""
                                )
                            }

                            showForm = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0B3D5C)
                        )
                    ) {
                        Text("Guardar ruta")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.isLoading) {
            CircularProgressIndicator(color = Color.White)
        }

        if (state.message.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.message,
                color = Color.Red
            )
        }

        state.routes.forEach { route ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF4FAFF)
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = "Ruta",
                        tint = Color(0xFF0B3D5C),
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Ruta #${route.id}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF0B3D5C)
                        )

                        Text(
                            text = "Precio: S/ ${route.price}",
                            color = Color(0xFF456979)
                        )

                        Text(
                            text = "Frecuencia: cada ${route.frequency} min",
                            color = Color(0xFF456979)
                        )

                        Text(
                            text = "Duración: ${route.duration} min",
                            color = Color(0xFF456979)
                        )

                        if (route.stops.isNotEmpty()) {
                            Text(
                                text = "Paraderos: ${route.stops.joinToString { it.name }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF456979)
                            )
                        }

                        if (route.schedules.isNotEmpty()) {
                            route.schedules.forEach { schedule ->
                                Text(
                                    text = "${schedule.dayOfWeek}: ${schedule.startTime} - ${schedule.endTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF456979)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onViewMapClick(route)
                        }
                    ) {
                        Text("Ver mapa")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            viewModel.deleteRoute(route.id, companyId)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar ruta",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}