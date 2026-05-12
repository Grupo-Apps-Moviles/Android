package es.upc.waypass.presentation.driver.routes

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.upc.waypass.data.model.CreateScheduleRequest
import es.upc.waypass.data.model.RouteDto
import es.upc.waypass.data.model.StopDto
import es.upc.waypass.ui.theme.PurpleLight
import es.upc.waypass.ui.theme.RedContainer
import es.upc.waypass.ui.theme.RedDestructive

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

    var showRouteBottomSheet by remember { mutableStateOf(false) }
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
        price = ""; frequency = ""; duration = ""
        firstStopId = null; secondStopId = null
        firstStopName = "Seleccionar paradero inicial"
        secondStopName = "Seleccionar siguiente paradero"
        schedules.forEachIndexed { index, item ->
            schedules[index] = item.copy(enabled = false, startTime = "", endTime = "")
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
            val existing = route.schedules.firstOrNull { it.dayOfWeek == item.day }
            schedules[index] = item.copy(
                enabled = existing != null,
                startTime = existing?.startTime ?: "",
                endTime = existing?.endTime ?: ""
            )
        }
        showRouteBottomSheet = true
        showScheduleForm = false
    }

    LaunchedEffect(companyId) {
        if (companyId != 0) viewModel.loadData(companyId)
    }

    // ── Pantalla principal (siempre visible) ──────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Encabezado
        Text(
            text = "Gestión de Rutas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Administra las rutas de transporte de tu empresa.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Card resumen
        RouteSummaryCard(totalRoutes = state.routes.size)
        Spacer(modifier = Modifier.height(24.dp))

        // Loading / error
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        if (state.message.isNotBlank()) {
            Text(text = state.message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Contenido dinámico DENTRO del Column ─────────────────────────────
        when {
            showScheduleForm -> {
                ScheduleRouteCard(
                    schedules = schedules,
                    onScheduleChange = { index, newSchedule -> schedules[index] = newSchedule },
                    onBackClick = {
                        showScheduleForm = false
                        showRouteBottomSheet = true
                    },
                    onSaveClick = {
                        val selectedStops = listOfNotNull(firstStopId, secondStopId).distinct()
                        val selectedSchedules = schedules.filter { it.enabled }.map {
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
            }

            state.routes.isEmpty() && !state.isLoading -> {
                EmptyRoutesCard(
                    onNewRouteClick = {
                        clearRouteForm()
                        showRouteBottomSheet = true
                    }
                )
            }

            else -> {
                // Botón Nueva Ruta
                Button(
                    onClick = { clearRouteForm(); showRouteBottomSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nueva Ruta")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de rutas
                state.routes.forEach { route ->
                    RouteItemCard(
                        route = route,
                        onViewMapClick = { onViewMapClick(route) },
                        onEditClick = { loadRouteToForm(route) },
                        onDeleteClick = { viewModel.deleteRoute(route.id, companyId) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }

    // ── Bottom Sheet (fuera del Column, correcto) ─────────────────────────────
    if (showRouteBottomSheet) {
        CreateRouteBottomSheet(
            title = if (editingRoute == null) "Nueva Ruta" else "Editar Ruta",
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
            onDismiss = {
                clearRouteForm()
                showRouteBottomSheet = false
            },
            onContinueClick = {
                showRouteBottomSheet = false
                showScheduleForm = true
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RouteSummaryCard(totalRoutes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "$totalRoutes Rutas Activas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Actualmente en operación",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyRoutesCard(onNewRouteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No hay rutas registradas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Crea tu primera ruta para comenzar a organizar tus servicios y horarios.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNewRouteClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nueva ruta")
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
    var showSchedules by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PurpleLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ruta #${route.id}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    when {
                        route.stops.size >= 2 -> Text(
                            text = "${route.stops.first().name}  →  ${route.stops.last().name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                        route.stops.size == 1 -> Text(
                            text = route.stops.first().name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        else -> Text(
                            text = "Sin paraderos asignados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminar",
                        tint = RedDestructive,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Stats 3 columnas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RouteStat(icon = Icons.Outlined.Timer, label = "Duración", value = "${route.duration} min")
                StatDivider()
                RouteStat(icon = Icons.Outlined.Schedule, label = "Frecuencia", value = "c/${route.frequency} min")
                StatDivider()
                RouteStat(icon = Icons.Outlined.AttachMoney, label = "Tarifa", value = "S/ ${String.format("%.2f", route.price)}")
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onViewMapClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = PurpleLight,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver mapa", style = MaterialTheme.typography.labelLarge)
                }

                if (route.schedules.isNotEmpty()) {
                    TextButton(
                        onClick = { showSchedules = !showSchedules },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (showSchedules) "Ocultar horarios" else "Ver horarios",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (showSchedules) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Horarios expandibles
            AnimatedVisibility(visible = showSchedules) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HORARIOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    route.schedules.forEach { schedule ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = schedule.dayOfWeek,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${schedule.startTime} – ${schedule.endTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(RedContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Warning, contentDescription = null, tint = RedDestructive, modifier = Modifier.size(28.dp))
                }
            },
            title = {
                Text("Eliminar ruta", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(
                    "Esta acción no se puede deshacer. ¿Confirmas que deseas eliminar esta ruta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showDeleteDialog = false; onDeleteClick() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Confirmar eliminación") }
                    OutlinedButton(
                        onClick = { showDeleteDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancelar") }
                }
            },
            dismissButton = null,
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FORMULARIO DE HORARIOS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ScheduleRouteCard(
    schedules: List<ScheduleFormState>,
    onScheduleChange: (Int, ScheduleFormState) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Horario de atención",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Configura los días y horarios en los que esta ruta estará operativa.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                schedules.forEachIndexed { index, schedule ->
                    ScheduleDayCard(schedule = schedule, onChange = { onScheduleChange(index, it) })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Guardar y salir") }
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Regresar") }
                }
            }
        }
    }
}

@Composable
fun ScheduleDayCard(schedule: ScheduleFormState, onChange: (ScheduleFormState) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.enabled) Color.White else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(if (schedule.enabled) 1.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = schedule.day,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (schedule.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = schedule.enabled,
                    onCheckedChange = {
                        onChange(schedule.copy(
                            enabled = it,
                            startTime = if (it && schedule.startTime.isBlank()) "06:00" else schedule.startTime,
                            endTime = if (it && schedule.endTime.isBlank()) "22:00" else schedule.endTime
                        ))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            if (schedule.enabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = schedule.startTime,
                        onValueChange = { onChange(schedule.copy(startTime = it)) },
                        label = { Text("Inicio") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = schedule.endTime,
                        onValueChange = { onChange(schedule.copy(endTime = it)) },
                        label = { Text("Fin") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
}

// Auxiliares
@Composable
private fun RouteStat(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(36.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

// StopDropdownButton — se mantiene para compatibilidad con CreateRouteCard si la usas
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
        OutlinedButton(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
            Text(text = text, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            stops.forEach { stop ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(stop.name)
                            Text(stop.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    onClick = { onSelectStop(stop) }
                )
            }
        }
    }
}