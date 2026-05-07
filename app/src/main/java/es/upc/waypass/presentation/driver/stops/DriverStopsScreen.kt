package es.upc.waypass.presentation.driver.stops

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            .background(Color(0xFF0B3D5C))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text(
            text = "Mis paraderos",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Administra los puntos de abordaje de tu empresa.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD6EAF8)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                showForm = !showForm
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF4FAFF),
                contentColor = Color(0xFF0B3D5C)
            )
        ) {
            Text(
                if (showForm)
                    "Ocultar formulario"
                else
                    "Agregar paradero"
            )
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
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        label = {
                            Text("Nombre del paradero")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = mapsUrl,
                        onValueChange = {
                            mapsUrl = it
                        },
                        label = {
                            Text("Coordenadas de ubicación")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                        },
                        label = {
                            Text("Teléfono")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                        },
                        label = {
                            Text("Dirección")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reference,
                        onValueChange = {
                            reference = it
                        },
                        label = {
                            Text("Referencia")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box {

                        OutlinedButton(
                            onClick = {
                                expandedDistrict = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedDistrictName)
                        }

                        DropdownMenu(
                            expanded = expandedDistrict,
                            onDismissRequest = {
                                expandedDistrict = false
                            }
                        ) {

                            geoState.districts.forEach { district ->

                                DropdownMenuItem(
                                    text = {
                                        Text(district.name)
                                    },
                                    onClick = {
                                        selectedDistrictId = district.id
                                        selectedDistrictName = district.name
                                        expandedDistrict = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF456979)
                        )
                    ) {
                        Text("Seleccionar imagen")
                    }

                    imageUri?.let { uri ->

                        Spacer(modifier = Modifier.height(12.dp))

                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {

                            val imagePart = imageUri?.let { uri ->

                                val inputStream =
                                    context.contentResolver.openInputStream(uri)

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
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0B3D5C)
                        )
                    ) {
                        Text("Guardar paradero")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.isLoading) {

            CircularProgressIndicator(
                color = Color.White
            )
        }

        if (state.message.isNotBlank()) {

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = state.message,
                color = Color.Red
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            state.stops.forEach { stop ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF4FAFF)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    )
                ) {

                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Paradero",
                            tint = Color(0xFF0B3D5C),
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = stop.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF0B3D5C)
                            )

                            Text(
                                text = stop.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF456979)
                            )

                            Text(
                                text = "Referencia: ${stop.reference}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF456979)
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.deleteStop(
                                    stop.id,
                                    companyId
                                )
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
        }
    }
}