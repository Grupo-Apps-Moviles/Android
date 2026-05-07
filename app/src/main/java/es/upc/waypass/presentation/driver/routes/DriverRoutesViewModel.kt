package es.upc.waypass.presentation.driver.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.upc.waypass.data.model.CreateRouteRequest
import es.upc.waypass.data.model.CreateScheduleRequest
import es.upc.waypass.data.model.RouteDto
import es.upc.waypass.data.model.StopDto
import es.upc.waypass.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RoutesState(
    val isLoading: Boolean = false,
    val routes: List<RouteDto> = emptyList(),
    val stops: List<StopDto> = emptyList(),
    val message: String = ""
)

class DriverRoutesViewModel : ViewModel() {

    private val _state = MutableStateFlow(RoutesState())
    val state: StateFlow<RoutesState> = _state

    fun loadData(companyId: Int) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, message = "")

                val stops = try {
                    RetrofitClient.api.getStopsByCompany(companyId)
                } catch (e: Exception) {
                    emptyList()
                }

                val routes = try {
                    RetrofitClient.api.getRoutesByCompany(companyId)
                } catch (e: Exception) {
                    emptyList()
                }

                _state.value = RoutesState(
                    isLoading = false,
                    routes = routes,
                    stops = stops,
                    message = if (stops.isEmpty()) {
                        "Primero registra paraderos para crear rutas"
                    } else {
                        ""
                    }
                )
            } catch (e: Exception) {
                _state.value = RoutesState(
                    isLoading = false,
                    message = e.message ?: "Error cargando datos"
                )
            }
        }
    }

    fun createRoute(
        companyId: Int,
        price: Double,
        frequency: Int,
        duration: Int,
        stopsIds: List<Int>,
        schedules: List<CreateScheduleRequest>
    ) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, message = "")

                val request = CreateRouteRequest(
                    frequency = frequency,
                    price = price,
                    duration = duration,
                    stopsIds = stopsIds,
                    schedules = schedules
                )

                RetrofitClient.api.createRoute(request)

                loadData(companyId)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = e.message ?: "Error creando ruta"
                )
            }
        }
    }

    fun deleteRoute(routeId: Int, companyId: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.deleteRoute(routeId)
                loadData(companyId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    message = e.message ?: "Error eliminando ruta"
                )
            }
        }
    }
}