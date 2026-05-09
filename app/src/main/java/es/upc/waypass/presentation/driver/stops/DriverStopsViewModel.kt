package es.upc.waypass.presentation.driver.stops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.upc.waypass.data.model.StopDto
import es.upc.waypass.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class StopsState(
    val isLoading: Boolean = false,
    val stops: List<StopDto> = emptyList(),
    val message: String = ""
)

class DriverStopsViewModel : ViewModel() {

    private val _state = MutableStateFlow(StopsState())
    val state: StateFlow<StopsState> = _state

    fun loadStops(companyId: Int) {
        viewModelScope.launch {
            try {
                _state.value = StopsState(isLoading = true)

                val stops = RetrofitClient.api.getStopsByCompany(companyId)

                _state.value = StopsState(
                    isLoading = false,
                    stops = stops
                )
            } catch (e: Exception) {
                _state.value = StopsState(
                    isLoading = false,
                    message = e.message ?: "Error cargando paraderos"
                )
            }
        }
    }

    fun createStop(
        name: String,
        mapsUrl: String,
        phone: String,
        companyId: Int,
        address: String,
        reference: String,
        districtId: Int,
        imageFile: MultipartBody.Part? = null
    ) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                RetrofitClient.api.createStop(
                    name = name.toRequestBody("text/plain".toMediaType()),
                    googleMapsUrl = mapsUrl.toRequestBody("text/plain".toMediaType()),
                    phone = phone.toRequestBody("text/plain".toMediaType()),
                    fkIdCompany = companyId.toString().toRequestBody("text/plain".toMediaType()),
                    address = address.toRequestBody("text/plain".toMediaType()),
                    reference = reference.toRequestBody("text/plain".toMediaType()),
                    fkIdDistrict = districtId.toString().toRequestBody("text/plain".toMediaType()),
                    imageFile = imageFile
                )

                loadStops(companyId)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = e.message ?: "Error creando paradero"
                )
            }
        }
    }

    fun deleteStop(stopId: Int, companyId: Int) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(message = "")

                RetrofitClient.api.deleteStop(stopId)

                loadStops(companyId)

            } catch (e: Exception) {
                val message = if (e is retrofit2.HttpException) {
                    when (e.code()) {
                        400 -> "El paradero no fue encontrado."
                        409 -> "No se puede eliminar este paradero porque está en uso por una ruta."
                        404 -> "No se puede eliminar este paradero porque está asociado a una ruta. Primero elimina la ruta."
                        else -> "Error al eliminar paradero: ${e.code()}"
                    }
                } else {
                    "Error al eliminar paradero"
                }

                _state.value = _state.value.copy(
                    message = message
                )
            }
        }
    }
}