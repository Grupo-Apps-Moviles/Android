package es.upc.waypass.presentation.driver.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.upc.waypass.data.model.StopDto
import es.upc.waypass.ui.theme.PurpleLight

// ─────────────────────────────────────────────────────────────────────────────
// Llama a este composable desde DriverRoutesScreen en lugar del CreateRouteCard
// anterior. Ejemplo de uso:
//
//   if (showRouteBottomSheet) {
//       CreateRouteBottomSheet(
//           title = if (editingRoute == null) "Nueva Ruta" else "Editar Ruta",
//           ... params ...,
//           onDismiss = { showRouteBottomSheet = false }
//       )
//   }
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRouteBottomSheet(
    title: String,
    // Campos de datos
    price: String,
    onPriceChange: (String) -> Unit,
    frequency: String,
    onFrequencyChange: (String) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    // Paraderos
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
    // Acciones
    onDismiss: () -> Unit,
    onContinueClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White,
        dragHandle = {
            // Pill drag handle
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {

            // ── Título ────────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Completa los datos para configurar la ruta.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Sección: Recorrido (timeline) ─────────────────────────────────
            SectionLabel(text = "RECORRIDO")
            Spacer(modifier = Modifier.height(12.dp))

            RouteTimeline(
                stops = stops,
                firstStopName = firstStopName,
                firstStopExpanded = firstStopExpanded,
                onOpenFirstStop = onOpenFirstStop,
                onDismissFirstStop = onDismissFirstStop,
                onSelectFirstStop = onSelectFirstStop,
                secondStopName = secondStopName,
                secondStopExpanded = secondStopExpanded,
                onOpenSecondStop = onOpenSecondStop,
                onDismissSecondStop = onDismissSecondStop,
                onSelectSecondStop = onSelectSecondStop,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Sección: Datos operativos ─────────────────────────────────────
            SectionLabel(text = "DATOS OPERATIVOS")
            Spacer(modifier = Modifier.height(12.dp))

            // Duración y Precio en la misma fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = onDurationChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Duración (min)") },
                    placeholder = { Text("45") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Precio (S/)") },
                    placeholder = { Text("2.50") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = frequency,
                onValueChange = onFrequencyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Frecuencia (min entre buses)") },
                placeholder = { Text("Ej: 15") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Acciones ──────────────────────────────────────────────────────
            Button(
                onClick = onContinueClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "Continuar → Horarios",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "Cancelar",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Timeline de paraderos: círculo hueco ── línea punteada ── círculo sólido
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RouteTimeline(
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
) {
    Row(modifier = Modifier.fillMaxWidth()) {

        // ── Columna izquierda: indicadores visuales ───────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 14.dp, end = 14.dp)
        ) {
            // Círculo hueco = origen
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
            // Línea punteada entre los dos puntos
            Column(
                modifier = Modifier
                    .width(2.dp)
                    .height(52.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(6) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }
            }
            // Círculo sólido = destino
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        // ── Columna derecha: dropdowns ────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StopTimelineDropdown(
                label = "Paradero de origen",
                selectedName = firstStopName,
                expanded = firstStopExpanded,
                stops = stops,
                onOpen = onOpenFirstStop,
                onDismiss = onDismissFirstStop,
                onSelect = onSelectFirstStop
            )

            StopTimelineDropdown(
                label = "Paradero de destino",
                selectedName = secondStopName,
                expanded = secondStopExpanded,
                stops = stops,
                onOpen = onOpenSecondStop,
                onDismiss = onDismissSecondStop,
                onSelect = onSelectSecondStop
            )
        }
    }
}

@Composable
private fun StopTimelineDropdown(
    label: String,
    selectedName: String,
    expanded: Boolean,
    stops: List<StopDto>,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (StopDto) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = selectedName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss
            ) {
                if (stops.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Sin paraderos disponibles") },
                        onClick = {}
                    )
                } else {
                    stops.forEach { stop ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = stop.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = stop.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = { onSelect(stop) }
                        )
                    }
                }
            }
        }
    }
}

// ── Label de sección ──────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
    )
}