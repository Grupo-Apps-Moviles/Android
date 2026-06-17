package es.upc.waypass.presentation.driver.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.upc.waypass.data.remote.ReservationResponse
import es.upc.waypass.data.remote.WayPassApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriverDashboardState(
    val isLoading: Boolean = false,
    val routesCount: Int = 0,
    val stopsCount: Int = 0,
    val averagePrice: Double = 0.0,
    val averageFrequency: Int = 0,
    val message: String = "",
    val payments: List<ReservationResponse> = emptyList(),
    val totalEarnings: Double = 0.0
)

@HiltViewModel
class DriverDashboardViewModel @Inject constructor(
    private val api: WayPassApiService
) : ViewModel() {

    private val _state = MutableStateFlow(DriverDashboardState())
    val state: StateFlow<DriverDashboardState> = _state

    fun loadDashboard(companyId: Int, driverId: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val routes = try {
                    api.getRoutesByCompany(companyId)
                } catch (e: Exception) { emptyList() }

                val stops = try {
                    api.getStopsByCompany(companyId)
                } catch (e: Exception) { emptyList() }

                val averagePrice = if (routes.isNotEmpty())
                    routes.map { it.price }.average() else 0.0

                val averageFrequency = if (routes.isNotEmpty())
                    routes.map { it.frequency }.average().toInt() else 0

                // Usa driverId (userId del conductor), NO companyId
                val paymentsResponse = api.getDriverReservations(driverId)

                val payments = if (paymentsResponse.isSuccessful)
                    paymentsResponse.body() ?: emptyList()
                else emptyList()

                val total = payments.sumOf { it.driverEarnings }

                _state.update {
                    it.copy(
                        isLoading = false,
                        routesCount = routes.size,
                        stopsCount = stops.size,
                        averagePrice = averagePrice,
                        averageFrequency = averageFrequency,
                        payments = payments,
                        totalEarnings = total
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = e.message ?: "Error cargando dashboard"
                    )
                }
            }
        }
    }
}