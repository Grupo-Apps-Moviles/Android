package es.upc.waypass.presentation.driver.stops

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import es.upc.waypass.data.model.DistrictDto
import es.upc.waypass.data.model.ProvinceDto
import es.upc.waypass.data.model.RegionDto
import es.upc.waypass.data.model.StopDto
import es.upc.waypass.ui.theme.PurpleLight
import es.upc.waypass.ui.theme.RedContainer
import es.upc.waypass.ui.theme.RedDestructive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun DriverStopsScreen(
    companyId: Int,
    paddingValues: PaddingValues,
    viewModel: DriverStopsViewModel = viewModel(),
    geographicViewModel: GeographicViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val geoState by geographicViewModel.state.collectAsState()
    val context = LocalContext.current

    var showForm by remember { mutableStateOf(false) }
    var editingStop by remember { mutableStateOf<StopDto?>(null) }
    var editingImageUrl by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }
    var mapsUrl by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }

    var selectedRegionName by remember { mutableStateOf("Selecciona una región") }
    var selectedProvinceName by remember { mutableStateOf("Selecciona una provincia") }
    var selectedDistrictId by remember { mutableStateOf<Int?>(null) }
    var selectedDistrictName by remember { mutableStateOf("Selecciona un distrito") }

    var expandedRegion by remember { mutableStateOf(false) }
    var expandedProvince by remember { mutableStateOf(false) }
    var expandedDistrict by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showMapPicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    fun clearForm() {
        editingStop = null; editingImageUrl = ""
        name = ""; mapsUrl = ""; phone = ""; address = ""; reference = ""
        selectedRegionName = "Selecciona una región"
        selectedProvinceName = "Selecciona una provincia"
        selectedDistrictId = null; selectedDistrictName = "Selecciona un distrito"
        imageUri = null
    }

    fun loadStopToForm(stop: StopDto) {
        editingStop = stop; editingImageUrl = stop.imageUrl ?: ""
        name = stop.name; mapsUrl = stop.googleMapsUrl ?: ""
        phone = stop.phone; address = stop.address; reference = stop.reference
        selectedDistrictId = stop.fkIdDistrict
        selectedDistrictName = geoState.districts
            .firstOrNull { it.id == stop.fkIdDistrict }?.name ?: "Selecciona un distrito"
        imageUri = null
        showForm = true
    }

    LaunchedEffect(companyId) {
        if (companyId != 0) {
            viewModel.loadStops(companyId)
            geographicViewModel.loadRegions()
        }
    }

    if (showMapPicker) {
        MapPickerScreen(onLocationSelected = { coordinates ->
            mapsUrl = coordinates
            showMapPicker = false
        })
        return
    }

    // ── Pantalla principal con FAB ─────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Encabezado
            Text(
                text = "Paraderos",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gestiona los puntos de parada de tu empresa",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            StopSummaryCard(totalStops = state.stops.size)
            Spacer(modifier = Modifier.height(20.dp))

            // Loading / errores
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            if (state.message.isNotBlank()) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (geoState.message.isNotBlank()) {
                Text(text = geoState.message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Lista o empty state
            if (state.stops.isEmpty() && !state.isLoading) {
                EmptyStopsCard(onNewStopClick = { clearForm(); showForm = true })
            } else {
                state.stops.forEach { stop ->
                    StopItemCard(
                        stop = stop,
                        onEditClick = { loadStopToForm(stop) },
                        onDeleteClick = { viewModel.deleteStop(stop.id, companyId) }
                    )
                }
            }

            // Espacio para que el FAB no tape el último item
            Spacer(modifier = Modifier.height(88.dp))
        }

        // ── FAB flotante ──────────────────────────────────────────────────────
        if (state.stops.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = { clearForm(); showForm = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = "Nuevo Paradero",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }

    // ── Bottom Sheet del formulario ───────────────────────────────────────────
    if (showForm) {
        StopFormBottomSheet(
            title = if (editingStop == null) "Nuevo Paradero" else "Editar Paradero",
            saveButtonText = if (editingStop == null) "Guardar paradero" else "Actualizar paradero",
            name = name, onNameChange = { name = it },
            mapsUrl = mapsUrl, onMapsUrlChange = { mapsUrl = it },
            onSelectLocationClick = { showMapPicker = true },
            phone = phone, onPhoneChange = { phone = it },
            address = address, onAddressChange = { address = it },
            reference = reference, onReferenceChange = { reference = it },
            regions = geoState.regions,
            provinces = geoState.provinces,
            districts = geoState.districts,
            selectedRegionName = selectedRegionName,
            expandedRegion = expandedRegion,
            onOpenRegion = { expandedRegion = true },
            onDismissRegion = { expandedRegion = false },
            onSelectRegion = { region ->
                selectedRegionName = region.name
                selectedProvinceName = "Selecciona una provincia"
                selectedDistrictName = "Selecciona un distrito"
                selectedDistrictId = null
                expandedRegion = false
                geographicViewModel.loadProvincesByRegion(region.id)
            },
            selectedProvinceName = selectedProvinceName,
            expandedProvince = expandedProvince,
            onOpenProvince = { expandedProvince = true },
            onDismissProvince = { expandedProvince = false },
            onSelectProvince = { province ->
                selectedProvinceName = province.name
                selectedDistrictName = "Selecciona un distrito"
                selectedDistrictId = null
                expandedProvince = false
                geographicViewModel.loadDistrictsByProvince(province.id)
            },
            selectedDistrictName = selectedDistrictName,
            expandedDistrict = expandedDistrict,
            onOpenDistrict = { expandedDistrict = true },
            onDismissDistrict = { expandedDistrict = false },
            onSelectDistrict = { district ->
                selectedDistrictId = district.id
                selectedDistrictName = district.name
                expandedDistrict = false
            },
            imageUri = imageUri,
            onSelectImageClick = { imagePickerLauncher.launch("image/*") },
            onDismiss = { clearForm(); showForm = false },
            onSaveClick = {
                val imagePart = imageUri?.let { uri ->
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    bytes?.toRequestBody("image/*".toMediaTypeOrNull())?.let {
                        MultipartBody.Part.createFormData("ImageFile", "stop_image.jpg", it)
                    }
                }
                if (editingStop == null) {
                    viewModel.createStop(
                        name = name, mapsUrl = mapsUrl, phone = phone,
                        companyId = companyId, address = address, reference = reference,
                        districtId = selectedDistrictId ?: 0, imageFile = imagePart
                    )
                } else {
                    viewModel.updateStop(
                        stopId = editingStop!!.id, name = name, mapsUrl = mapsUrl,
                        phone = phone, companyId = companyId, address = address,
                        reference = reference, districtId = selectedDistrictId ?: 0,
                        imageUrl = editingImageUrl
                    )
                }
                clearForm(); showForm = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOTTOM SHEET DEL FORMULARIO
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopFormBottomSheet(
    title: String,
    saveButtonText: String,
    name: String, onNameChange: (String) -> Unit,
    mapsUrl: String, onMapsUrlChange: (String) -> Unit,
    onSelectLocationClick: () -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    reference: String, onReferenceChange: (String) -> Unit,
    regions: List<RegionDto>,
    provinces: List<ProvinceDto>,
    districts: List<DistrictDto>,
    selectedRegionName: String,
    expandedRegion: Boolean, onOpenRegion: () -> Unit, onDismissRegion: () -> Unit,
    onSelectRegion: (RegionDto) -> Unit,
    selectedProvinceName: String,
    expandedProvince: Boolean, onOpenProvince: () -> Unit, onDismissProvince: () -> Unit,
    onSelectProvince: (ProvinceDto) -> Unit,
    selectedDistrictName: String,
    expandedDistrict: Boolean, onOpenDistrict: () -> Unit, onDismissDistrict: () -> Unit,
    onSelectDistrict: (DistrictDto) -> Unit,
    imageUri: Uri?,
    onSelectImageClick: () -> Unit,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
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
            // Título
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Completa la información del paradero",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ── Sección 1: Imagen ─────────────────────────────────────────────
            FormSectionLabel(icon = Icons.Outlined.Image, text = "Foto del paradero")
            Spacer(modifier = Modifier.height(10.dp))

            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectImageClick() },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectImageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.AddAPhoto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Subir foto del paradero",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Sección 2: Información básica ─────────────────────────────────
            FormSectionLabel(icon = Icons.Outlined.Info, text = "Información básica")
            Spacer(modifier = Modifier.height(10.dp))

            StopTextField(
                value = name, onValueChange = onNameChange,
                label = "Nombre del paradero",
                icon = Icons.Outlined.LocationOn
            )
            Spacer(modifier = Modifier.height(12.dp))
            StopTextField(
                value = phone, onValueChange = onPhoneChange,
                label = "Teléfono de contacto",
                icon = Icons.Outlined.Phone
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Sección 3: Ubicación ──────────────────────────────────────────
            FormSectionLabel(icon = Icons.Outlined.Map, text = "Ubicación")
            Spacer(modifier = Modifier.height(10.dp))

            StopTextField(
                value = address, onValueChange = onAddressChange,
                label = "Dirección",
                icon = Icons.Outlined.Home
            )
            Spacer(modifier = Modifier.height(12.dp))
            StopTextField(
                value = reference, onValueChange = onReferenceChange,
                label = "Referencia",
                icon = Icons.Outlined.NearMe
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Coordenadas + botón mapa en la misma sección
            OutlinedTextField(
                value = mapsUrl,
                onValueChange = onMapsUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Coordenadas") },
                placeholder = { Text("-12.1586,-76.9918") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.GpsFixed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onSelectLocationClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.PinDrop,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Seleccionar en mapa", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Sección 4: Distrito ───────────────────────────────────────────
            FormSectionLabel(icon = Icons.Outlined.Place, text = "Distrito")
            Spacer(modifier = Modifier.height(10.dp))

            GeographicDropdown(
                text = selectedRegionName, expanded = expandedRegion,
                onOpen = onOpenRegion, onDismiss = onDismissRegion,
                items = regions, itemText = { it.name }, onSelect = onSelectRegion
            )
            Spacer(modifier = Modifier.height(10.dp))
            GeographicDropdown(
                text = selectedProvinceName, expanded = expandedProvince,
                onOpen = onOpenProvince, onDismiss = onDismissProvince,
                items = provinces, itemText = { it.name }, onSelect = onSelectProvince
            )
            Spacer(modifier = Modifier.height(10.dp))
            GeographicDropdown(
                text = selectedDistrictName, expanded = expandedDistrict,
                onOpen = onOpenDistrict, onDismiss = onDismissDistrict,
                items = districts, itemText = { it.name }, onSelect = onSelectDistrict
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Acciones ──────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(saveButtonText, style = MaterialTheme.typography.titleSmall)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTES DE LA LISTA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StopSummaryCard(totalStops: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "$totalStops Paraderos Activos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Registrados en tu empresa",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyStopsCard(onNewStopClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin paraderos registrados",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Crea tu primer paradero para organizar las rutas de tu empresa.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNewStopClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Paradero")
            }
        }
    }
}

@Composable
fun StopItemCard(
    stop: StopDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Texto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stop.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stop.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (stop.reference.isNotBlank()) {
                    Text(
                        text = stop.reference,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Acciones
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
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(RedContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = RedDestructive,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text("Eliminar paradero", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text(
                    "Esta acción no se puede deshacer. ¿Confirmas que deseas eliminar este paradero?",
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
// AUXILIARES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormSectionLabel(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Medium
        )
    }
    Spacer(modifier = Modifier.height(2.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
}

@Composable
private fun StopTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun <T> GeographicDropdown(
    text: String,
    expanded: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    items: List<T>,
    itemText: (T) -> String,
    onSelect: (T) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            if (items.isEmpty()) {
                DropdownMenuItem(text = { Text("Sin datos disponibles") }, onClick = {})
            } else {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemText(item)) },
                        onClick = { onSelect(item) }
                    )
                }
            }
        }
    }
}