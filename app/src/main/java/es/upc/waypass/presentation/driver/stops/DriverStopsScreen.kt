package es.upc.waypass.presentation.driver.stops

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import es.upc.waypass.data.model.DistrictDto
import es.upc.waypass.data.model.ProvinceDto
import es.upc.waypass.data.model.RegionDto
import es.upc.waypass.data.model.StopDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun DriverStopsScreen(
    companyId: Int,
    paddingValues: PaddingValues,
    viewModel: DriverStopsViewModel = hiltViewModel(),
    geographicViewModel: GeographicViewModel = hiltViewModel()
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
    ) { uri ->
        imageUri = uri
    }

    fun clearForm() {
        editingStop = null
        editingImageUrl = ""
        name = ""
        mapsUrl = ""
        phone = ""
        address = ""
        reference = ""

        selectedRegionName = "Selecciona una región"
        selectedProvinceName = "Selecciona una provincia"
        selectedDistrictId = null
        selectedDistrictName = "Selecciona un distrito"

        imageUri = null
    }

    fun loadStopToForm(stop: StopDto) {
        editingStop = stop
        editingImageUrl = stop.imageUrl ?: ""

        name = stop.name
        mapsUrl = stop.googleMapsUrl ?: ""
        phone = stop.phone
        address = stop.address
        reference = stop.reference

        selectedDistrictId = stop.fkIdDistrict
        selectedDistrictName = geoState.districts
            .firstOrNull { it.id == stop.fkIdDistrict }
            ?.name ?: "Selecciona un distrito"

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
        MapPickerScreen(
            onLocationSelected = { coordinates ->
                mapsUrl = coordinates
                showMapPicker = false
            }
        )
        return
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
            text = "Paraderos",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Gestiona y administra los puntos de parada de tu ruta",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(24.dp))

        StopSummaryCard(totalStops = state.stops.size)

        Spacer(modifier = Modifier.height(24.dp))

        if (showForm) {
            StopFormCard(
                title = if (editingStop == null) "Nuevo Paradero" else "Editar Paradero",
                saveButtonText = if (editingStop == null) "Guardar" else "Actualizar",

                name = name,
                onNameChange = { name = it },

                mapsUrl = mapsUrl,
                onMapsUrlChange = { mapsUrl = it },

                onSelectLocationClick = {
                    showMapPicker = true
                },

                phone = phone,
                onPhoneChange = { phone = it },

                address = address,
                onAddressChange = { address = it },

                reference = reference,
                onReferenceChange = { reference = it },

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
                onSelectImageClick = {
                    imagePickerLauncher.launch("image/*")
                },
                onCancelClick = {
                    clearForm()
                    showForm = false
                },
                onSaveClick = {
                    val imagePart = imageUri?.let { uri ->
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()

                        val requestBody = bytes?.toRequestBody(
                            contentType = "image/*".toMediaTypeOrNull()
                        )

                        requestBody?.let {
                            MultipartBody.Part.createFormData(
                                name = "ImageFile",
                                filename = "stop_image.jpg",
                                body = it
                            )
                        }
                    }

                    if (editingStop == null) {
                        viewModel.createStop(
                            name = name,
                            mapsUrl = mapsUrl,
                            phone = phone,
                            companyId = companyId,
                            address = address,
                            reference = reference,
                            districtId = selectedDistrictId ?: 0,
                            imageFile = imagePart
                        )
                    } else {
                        viewModel.updateStop(
                            stopId = editingStop!!.id,
                            name = name,
                            mapsUrl = mapsUrl,
                            phone = phone,
                            companyId = companyId,
                            address = address,
                            reference = reference,
                            districtId = selectedDistrictId ?: 0,
                            imageUrl = editingImageUrl
                        )
                    }

                    clearForm()
                    showForm = false
                }
            )

            Spacer(modifier = Modifier.height(18.dp))
        } else {
            if (state.stops.isEmpty()) {
                EmptyStopsCard(
                    onNewStopClick = {
                        clearForm()
                        showForm = true
                    }
                )
            } else {
                Button(
                    onClick = {
                        clearForm()
                        showForm = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nuevo paradero"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuevo Paradero")
                }

                Spacer(modifier = Modifier.height(18.dp))

                state.stops.forEach { stop ->
                    StopItemCard(
                        name = stop.name,
                        address = stop.address,
                        reference = stop.reference,
                        onEditClick = {
                            loadStopToForm(stop)
                        },
                        onDeleteClick = {
                            viewModel.deleteStop(stop.id, companyId)
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

        if (geoState.message.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = geoState.message,
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun StopSummaryCard(
    totalStops: Int
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
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Paraderos",
                    tint = Color(0xFF4F46E5)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Estado actual",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )

                Text(
                    text = "$totalStops Paraderos Activos",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF111827)
                )
            }
        }
    }
}

@Composable
fun EmptyStopsCard(
    onNewStopClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
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
                text = "No hay paraderos registrados",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¡Crea tu primer paradero para comenzar a organizar las rutas de tus unidades eficientemente!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onNewStopClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Paradero")
            }
        }
    }
}

@Composable
fun StopFormCard(
    title: String,
    saveButtonText: String,

    name: String,
    onNameChange: (String) -> Unit,

    mapsUrl: String,
    onMapsUrlChange: (String) -> Unit,
    onSelectLocationClick: () -> Unit,

    phone: String,
    onPhoneChange: (String) -> Unit,

    address: String,
    onAddressChange: (String) -> Unit,

    reference: String,
    onReferenceChange: (String) -> Unit,

    regions: List<RegionDto>,
    provinces: List<ProvinceDto>,
    districts: List<DistrictDto>,

    selectedRegionName: String,
    expandedRegion: Boolean,
    onOpenRegion: () -> Unit,
    onDismissRegion: () -> Unit,
    onSelectRegion: (RegionDto) -> Unit,

    selectedProvinceName: String,
    expandedProvince: Boolean,
    onOpenProvince: () -> Unit,
    onDismissProvince: () -> Unit,
    onSelectProvince: (ProvinceDto) -> Unit,

    selectedDistrictName: String,
    expandedDistrict: Boolean,
    onOpenDistrict: () -> Unit,
    onDismissDistrict: () -> Unit,
    onSelectDistrict: (DistrictDto) -> Unit,

    imageUri: Uri?,
    onSelectImageClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = mapsUrl,
                onValueChange = onMapsUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Coordenadas") },
                supportingText = { Text("Ejemplo: -12.1586,-76.9918") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSelectLocationClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F46E5)
                )
            ) {
                Text("Seleccionar ubicación en mapa")
            }


            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Teléfono") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Dirección") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = reference,
                onValueChange = onReferenceChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Referencia") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            GeographicDropdown(
                text = selectedRegionName,
                expanded = expandedRegion,
                onOpen = onOpenRegion,
                onDismiss = onDismissRegion,
                items = regions,
                itemText = { it.name },
                onSelect = onSelectRegion
            )

            Spacer(modifier = Modifier.height(12.dp))

            GeographicDropdown(
                text = selectedProvinceName,
                expanded = expandedProvince,
                onOpen = onOpenProvince,
                onDismiss = onDismissProvince,
                items = provinces,
                itemText = { it.name },
                onSelect = onSelectProvince
            )

            Spacer(modifier = Modifier.height(12.dp))

            GeographicDropdown(
                text = selectedDistrictName,
                expanded = expandedDistrict,
                onOpen = onOpenDistrict,
                onDismiss = onDismissDistrict,
                items = districts,
                itemText = { it.name },
                onSelect = onSelectDistrict
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSelectImageClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
            ) {
                Text("Seleccionar imagen")
            }

            if (imageUri != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }

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
                    onClick = onSaveClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text(saveButtonText)
                }
            }
        }
    }
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
            if (items.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Sin datos disponibles") },
                    onClick = {}
                )
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

@Composable
fun StopItemCard(
    name: String,
    address: String,
    reference: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
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
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEDEBFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Paradero",
                    tint = Color(0xFF4F46E5)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )

                Text(
                    text = reference,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA3AF)
                )
            }

            IconButton(onClick = onEditClick) {
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
                    text = "Esta acción no se puede deshacer. ¿Estás seguro de que deseas eliminar este paradero?",
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
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