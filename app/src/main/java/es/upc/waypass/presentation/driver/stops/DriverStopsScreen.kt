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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
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

    var name by remember { mutableStateOf("") }
    var mapsUrl by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }

    var selectedDistrictId by remember { mutableStateOf<Int?>(null) }
    var selectedDistrictName by remember { mutableStateOf("Selecciona un distrito") }
    var expandedDistrict by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    LaunchedEffect(companyId) {
        if (companyId != 0) {
            viewModel.loadStops(companyId)
            geographicViewModel.loadDistricts()
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
                name = name,
                onNameChange = { name = it },
                mapsUrl = mapsUrl,
                onMapsUrlChange = { mapsUrl = it },
                phone = phone,
                onPhoneChange = { phone = it },
                address = address,
                onAddressChange = { address = it },
                reference = reference,
                onReferenceChange = { reference = it },
                selectedDistrictName = selectedDistrictName,
                expandedDistrict = expandedDistrict,
                onOpenDistrict = { expandedDistrict = true },
                onDismissDistrict = { expandedDistrict = false },
                districts = geoState.districts,
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

                    name = ""
                    mapsUrl = ""
                    phone = ""
                    address = ""
                    reference = ""
                    selectedDistrictId = null
                    selectedDistrictName = "Selecciona un distrito"
                    imageUri = null
                    showForm = false
                }
            )

            Spacer(modifier = Modifier.height(18.dp))
        } else {
            if (state.stops.isEmpty()) {
                EmptyStopsCard(
                    onNewStopClick = {
                        showForm = true
                    }
                )
            } else {
                Button(
                    onClick = { showForm = true },
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F46E5)
                )
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
    name: String,
    onNameChange: (String) -> Unit,
    mapsUrl: String,
    onMapsUrlChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    reference: String,
    onReferenceChange: (String) -> Unit,
    selectedDistrictName: String,
    expandedDistrict: Boolean,
    onOpenDistrict: () -> Unit,
    onDismissDistrict: () -> Unit,
    districts: List<es.upc.waypass.data.model.DistrictDto>,
    onSelectDistrict: (es.upc.waypass.data.model.DistrictDto) -> Unit,
    imageUri: Uri?,
    onSelectImageClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Nuevo Paradero",
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

            Box {
                OutlinedButton(
                    onClick = onOpenDistrict,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedDistrictName)
                }

                DropdownMenu(
                    expanded = expandedDistrict,
                    onDismissRequest = onDismissDistrict
                ) {
                    districts.forEach { district ->
                        DropdownMenuItem(
                            text = { Text(district.name) },
                            onClick = {
                                onSelectDistrict(district)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSelectImageClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                )
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    )
                ) {
                    Text("Guardar")
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
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
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

            IconButton(
                onClick = onDeleteClick
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