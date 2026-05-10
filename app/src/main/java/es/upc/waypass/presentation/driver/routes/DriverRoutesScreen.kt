package es.upc.waypass.presentation.driver.routes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.upc.waypass.R
import es.upc.waypass.data.model.CreateScheduleRequest
import es.upc.waypass.data.model.RouteDto
import es.upc.waypass.data.model.StopDto

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

    var showRouteForm by remember { mutableStateOf(false) }
    var showScheduleForm by remember { mutableStateOf(false) }
    var editingRoute by remember { mutableStateOf<RouteDto?>(null) }

    var price by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    var firstStopId by remember { mutableStateOf<Int?>(null) }
    var firstStopName by remember { mutableStateOf("Seleccionar paradero inicial") }
    var firstStopExpanded by remember { mutableStateOf(false) }

    var secondStopId by remember { mutableStateOf<Int?>(null) }
    var secondStopName by remember { mutableStateOf("Seleccionar siguiente paradero") }
    var secondStopExpanded by remember { mutableStateOf(false) }

    val schedules = remember {
        mutableStateListOf(
            ScheduleFormState("Lunes", true, "06:00", "22:00"),
            ScheduleFormState("Martes"),
            ScheduleFormState("Miércoles"),
            ScheduleFormState("Jueves"),
            ScheduleFormState("Viernes"),
            ScheduleFormState("Sábado"),
            ScheduleFormState("Domingo")
        )
    }

    fun clearRouteForm() {
        editingRoute = null

        price = ""
        frequency = ""
        duration = ""

        firstStopId = null
        secondStopId = null
        firstStopName = "Seleccionar paradero inicial"
        secondStopName = "Seleccionar siguiente paradero"

        schedules.forEachIndexed { index, item ->
            schedules[index] = item.copy(
                enabled = false,
                startTime = "",
                endTime = ""
            )
        }
    }

    fun loadRouteToForm(route: RouteDto) {
        editingRoute = route

        price = route.price.toString()
        frequency = route.frequency.toString()
        duration = route.duration.toString()

        firstStopId = route.stops.getOrNull(0)?.id
        secondStopId = route.stops.getOrNull(1)?.id

        firstStopName = route.stops.getOrNull(0)?.name ?: "Seleccionar paradero inicial"
        secondStopName = route.stops.getOrNull(1)?.name ?: "Seleccionar siguiente paradero"

        schedules.forEachIndexed { index, item ->
            val existingSchedule = route.schedules.firstOrNull {
                it.dayOfWeek == item.day
            }

            schedules[index] = item.copy(
                enabled = existingSchedule != null,
                startTime = existingSchedule?.startTime ?: "",
                endTime = existingSchedule?.endTime ?: ""
            )
        }

        showRouteForm = true
        showScheduleForm = false
    }

    LaunchedEffect(companyId) {
        if (companyId != 0) {
            viewModel.loadData(companyId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Gestión de Rutas",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF4F46E5)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Administra las rutas de transporte de tu empresa.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4B5563)
        )

        Spacer(modifier = Modifier.height(24.dp))

        RouteSummaryCard(totalRoutes = state.routes.size)

        Spacer(modifier = Modifier.height(28.dp))

        if (showRouteForm) {
            CreateRouteCard(
                title = if (editingRoute == null) "Crear Ruta" else "Editar Ruta",
                price = price,
                onPriceChange = { price = it },
                frequency = frequency,
                onFrequencyChange = { frequency = it },
                duration = duration,
                onDurationChange = { duration = it },
                stops = state.stops,
                firstStopName = firstStopName,
                firstStopExpanded = firstStopExpanded,
                onOpenFirstStop = { firstStopExpanded = true },
                onDismissFirstStop = { firstStopExpanded = false },
                onSelectFirstStop = { stop ->
                    firstStopId = stop.id
                    firstStopName = stop.name
                    firstStopExpanded = false
                },
                secondStopName = secondStopName,
                secondStopExpanded = secondStopExpanded,
                onOpenSecondStop = { secondStopExpanded = true },
                onDismissSecondStop = { secondStopExpanded = false },
                onSelectSecondStop = { stop ->
                    secondStopId = stop.id
                    secondStopName = stop.name
                    secondStopExpanded = false
                },
                onCancelClick = {
                    clearRouteForm()
                    showRouteForm = false
                },
                onContinueClick = {
                    showRouteForm = false
                    showScheduleForm = true
                }
            )
        } else if (showScheduleForm) {
            ScheduleRouteCard(
                schedules = schedules,
                onScheduleChange = { index, newSchedule ->
                    schedules[index] = newSchedule
                },
                onBackClick = {
                    showScheduleForm = false
                    showRouteForm = true
                },
                onSaveClick = {
                    val selectedStops = listOfNotNull(firstStopId, secondStopId).distinct()

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

                    if (editingRoute == null) {
                        viewModel.createRoute(
                            companyId = companyId,
                            price = price.toDoubleOrNull() ?: 0.0,
                            frequency = frequency.toIntOrNull() ?: 0,
                            duration = duration.toIntOrNull() ?: 0,
                            stopsIds = selectedStops,
                            schedules = selectedSchedules
                        )
                    } else {
                        viewModel.updateRoute(
                            routeId = editingRoute!!.id,
                            companyId = companyId,
                            price = price.toDoubleOrNull() ?: 0.0,
                            frequency = frequency.toIntOrNull() ?: 0,
                            duration = duration.toIntOrNull() ?: 0,
                            stopsIds = selectedStops,
                            schedules = selectedSchedules
                        )
                    }

                    clearRouteForm()
                    showScheduleForm = false
                }
            )
        } else {
            if (state.routes.isEmpty()) {
                EmptyRoutesCard(
                    onNewRouteClick = {
                        clearRouteForm()
                        showRouteForm = true
                    }
                )
            } else {
                Button(
                    onClick = {
                        clearRouteForm()
                        showRouteForm = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva ruta"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nueva Ruta")
                }

                Spacer(modifier = Modifier.height(18.dp))

                state.routes.forEach { route ->
                    RouteItemCard(
                        route = route,
                        onViewMapClick = {
                            onViewMapClick(route)
                        },
                        onEditClick = {
                            loadRouteToForm(route)
                        },
                        onDeleteClick = {
                            viewModel.deleteRoute(route.id, companyId)
                        }
                    )
                }
            }
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        if (state.message.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.message,
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(90.dp))
    }
}

@Composable
fun RouteSummaryCard(
    totalRoutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEDEBFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = "Rutas",
                    tint = Color(0xFF4F46E5)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "$totalRoutes Rutas Activas",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF111827)
                )

                Text(
                    text = "Actualmente en operación",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
fun EmptyRoutesCard(
    onNewRouteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No hay rutas registradas",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¡Crea tu primera ruta para comenzar a organizar tus servicios y horarios!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4B5563)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onNewRouteClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F46E5)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva ruta"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva ruta")
            }
        }
    }
}

@Composable
fun CreateRouteCard(
    title: String,
    price: String,
    onPriceChange: (String) -> Unit,
    frequency: String,
    onFrequencyChange: (String) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    stops: List<StopDto>,
    firstStopName: String,
    firstStopExpanded: Boolean,
    onOpenFirstStop: () -> Unit,
    onDismissFirstStop: () -> Unit,
    onSelectFirstStop: (StopDto) -> Unit,
    secondStopName: String,
    secondStopExpanded: Boolean,
    onOpenSecondStop: () -> Unit,
    onDismissSecondStop: () -> Unit,
    onSelectSecondStop: (StopDto) -> Unit,
    onCancelClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF111827),
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onCancelClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }

            Divider()

            Image(
                painter = painterResource(id = R.drawable.logo_waypass),
                contentDescription = "Ruta",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Primer paradero",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4B5563)
                )

                StopDropdownButton(
                    text = firstStopName,
                    expanded = firstStopExpanded,
                    stops = stops,
                    onOpen = onOpenFirstStop,
                    onDismiss = onDismissFirstStop,
                    onSelectStop = onSelectFirstStop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Segundo paradero",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4B5563)
                )

                StopDropdownButton(
                    text = secondStopName,
                    expanded = secondStopExpanded,
                    stops = stops,
                    onOpen = onOpenSecondStop,
                    onDismiss = onDismissSecondStop,
                    onSelectStop = onSelectSecondStop
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = onDurationChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Duración") },
                        placeholder = { Text("Ej: 45") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Duración"
                            )
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = onPriceChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Precio") },
                        placeholder = { Text("Ej: 2.50") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Precio"
                            )
                        },
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = frequency,
                    onValueChange = onFrequencyChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Frecuencia") },
                    placeholder = { Text("Intervalo entre buses") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Frecuencia"
                        )
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = onContinueClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5)
                        )
                    ) {
                        Text("Continuar")
                    }
                }
            }
        }
    }
}

@Composable
fun StopDropdownButton(
    text: String,
    expanded: Boolean,
    stops: List<StopDto>,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    onSelectStop: (StopDto) -> Unit
) {
    Box {
        OutlinedButton(
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = Color(0xFF111827)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss
        ) {
            stops.forEach { stop ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(stop.name)
                            Text(
                                stop.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                        }
                    },
                    onClick = {
                        onSelectStop(stop)
                    }
                )
            }
        }
    }
}

@Composable
fun ScheduleRouteCard(
    schedules: List<ScheduleFormState>,
    onScheduleChange: (Int, ScheduleFormState) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Horario de atención",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF111827),
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }

            Divider()

            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Configura los días y horarios en los que esta ruta estará operativa.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(18.dp))

                schedules.forEachIndexed { index, schedule ->
                    ScheduleDayCard(
                        schedule = schedule,
                        onChange = { newSchedule ->
                            onScheduleChange(index, newSchedule)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Regresar")
                    }

                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF111827)
                        )
                    ) {
                        Text("Guardar y salir")
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleDayCard(
    schedule: ScheduleFormState,
    onChange: (ScheduleFormState) -> Unit
) {
    val activeColor = if (schedule.enabled) Color(0xFF4F46E5) else Color(0xFFE5E7EB)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.enabled) Color.White else Color(0xFFF3F4F6)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = schedule.day,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (schedule.enabled) Color(0xFF111827) else Color(0xFF9CA3AF),
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = schedule.enabled,
                    onCheckedChange = {
                        onChange(
                            schedule.copy(
                                enabled = it,
                                startTime = if (it && schedule.startTime.isBlank()) "06:00" else schedule.startTime,
                                endTime = if (it && schedule.endTime.isBlank()) "22:00" else schedule.endTime
                            )
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = activeColor
                    )
                )
            }

            if (schedule.enabled) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = schedule.startTime,
                        onValueChange = {
                            onChange(schedule.copy(startTime = it))
                        },
                        label = { Text("Inicio") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = schedule.endTime,
                        onValueChange = {
                            onChange(schedule.copy(endTime = it))
                        },
                        label = { Text("Fin") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
fun RouteItemCard(
    route: RouteDto,
    onViewMapClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Route,
                contentDescription = "Ruta",
                tint = Color(0xFF4F46E5),
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Ruta #${route.id}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF111827)
                )

                Text(
                    text = "Precio: S/ ${route.price}",
                    color = Color(0xFF4B5563)
                )

                Text(
                    text = "Frecuencia: cada ${route.frequency} min",
                    color = Color(0xFF4B5563)
                )

                Text(
                    text = "Duración: ${route.duration} min",
                    color = Color(0xFF4B5563)
                )

                if (route.stops.isNotEmpty()) {
                    Text(
                        text = "Paraderos: ${route.stops.joinToString { it.name }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }

                route.schedules.forEach { schedule ->
                    Text(
                        text = "${schedule.dayOfWeek}: ${schedule.startTime} - ${schedule.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onViewMapClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6D5DF6)
                    )
                ) {
                    Text("Ver mapa")
                }

                IconButton(
                    onClick = onEditClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFF4F46E5)
                    )
                }

                IconButton(
                    onClick = {
                        showDeleteDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Red
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE4E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "¿Confirmar acción?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Text(
                    text = "Esta acción no se puede deshacer. ¿Estás seguro de que deseas continuar?",
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }
}