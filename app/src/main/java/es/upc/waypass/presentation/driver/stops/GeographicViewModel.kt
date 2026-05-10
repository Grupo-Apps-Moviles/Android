package es.upc.waypass.presentation.driver.stops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.upc.waypass.data.model.DistrictDto
import es.upc.waypass.data.model.ProvinceDto
import es.upc.waypass.data.model.RegionDto
import es.upc.waypass.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GeographicState(
    val regions: List<RegionDto> = emptyList(),
    val provinces: List<ProvinceDto> = emptyList(),
    val districts: List<DistrictDto> = emptyList(),
    val message: String = ""
)

class GeographicViewModel : ViewModel() {

    private val _state = MutableStateFlow(GeographicState())
    val state: StateFlow<GeographicState> = _state

    fun loadRegions() {
        viewModelScope.launch {
            try {
                val regions = RetrofitClient.api.getRegions()
                _state.value = _state.value.copy(
                    regions = regions,
                    provinces = emptyList(),
                    districts = emptyList(),
                    message = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "Error cargando regiones"
                )
            }
        }
    }

    fun loadProvincesByRegion(regionId: Int) {
        viewModelScope.launch {
            try {
                val provinces = RetrofitClient.api.getProvincesByRegion(regionId)
                _state.value = _state.value.copy(
                    provinces = provinces,
                    districts = emptyList(),
                    message = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "Error cargando provincias"
                )
            }
        }
    }

    fun loadDistrictsByProvince(provinceId: Int) {
        viewModelScope.launch {
            try {
                val districts = RetrofitClient.api.getDistrictsByProvince(provinceId)
                _state.value = _state.value.copy(
                    districts = districts,
                    message = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "Error cargando distritos"
                )
            }
        }
    }
}