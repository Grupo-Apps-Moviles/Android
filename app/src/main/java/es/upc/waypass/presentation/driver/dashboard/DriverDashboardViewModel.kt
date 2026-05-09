package es.upc.waypass.presentation.driver.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.upc.waypass.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DriverDashboardState(
    val isLoading: Boolean = false,
    val routesCount: Int = 0,
    val stopsCount: Int = 0,
    val averagePrice: Double = 0.0,
    val averageFrequency: Int = 0,
    val message: String = ""
)

class DriverDashboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(DriverDashboardState())
    val state: StateFlow<DriverDashboardState> = _state

    fun loadDashboard(companyId: Int) {
        viewModelScope.launch {
            try {
                _state.value = DriverDashboardState(isLoading = true)

                val routes = try {
                    RetrofitClient.api.getRoutesByCompany(companyId)
                } catch (e: Exception) {
                    emptyList()
                }

                val stops = try {
                    RetrofitClient.api.getStopsByCompany(companyId)
                } catch (e: Exception) {
                    emptyList()
                }

                val averagePrice = if (routes.isNotEmpty()) {
                    routes.map { it.price }.average()
                } else {
                    0.0
                }

                val averageFrequency = if (routes.isNotEmpty()) {
                    routes.map { it.frequency }.average().toInt()
                } else {
                    0
                }

                _state.value = DriverDashboardState(
                    isLoading = false,
                    routesCount = routes.size,
                    stopsCount = stops.size,
                    averagePrice = averagePrice,
                    averageFrequency = averageFrequency
                )

            } catch (e: Exception) {
                _state.value = DriverDashboardState(
                    isLoading = false,
                    message = e.message ?: "Error cargando resumen"
                )
            }
        }
    }
}