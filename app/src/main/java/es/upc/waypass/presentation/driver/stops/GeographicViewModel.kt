package es.upc.waypass.presentation.driver.stops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.upc.waypass.data.model.DistrictDto
import es.upc.waypass.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GeographicState(
    val districts: List<DistrictDto> = emptyList(),
    val message: String = ""
)

class GeographicViewModel : ViewModel() {

    private val _state = MutableStateFlow(GeographicState())
    val state: StateFlow<GeographicState> = _state

    fun loadDistricts() {
        viewModelScope.launch {
            try {
                val districts = RetrofitClient.api.getDistricts()
                _state.value = GeographicState(districts = districts)
            } catch (e: Exception) {
                _state.value = GeographicState(
                    message = e.message ?: "Error cargando distritos"
                )
            }
        }
    }
}