package es.upc.waypass.presentation.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.upc.waypass.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservationViewModel(
    private val repository:
    ReservationRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ReservationUiState()
        )

    val uiState:
            StateFlow<ReservationUiState> =
        _uiState.asStateFlow()

    fun loadDriverReservations(
        driverId: Int
    ) {

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = true
                )

            val result =
                repository
                    .getDriverReservations(
                        driverId
                    )

            result.onSuccess { reservations ->

                val total =
                    reservations.sumOf {
                        it.driverEarnings
                    }

                _uiState.value =
                    ReservationUiState(
                        reservations =
                            reservations,

                        totalEarnings =
                            total
                    )
            }

            result.onFailure {

                _uiState.value =
                    ReservationUiState(
                        error =
                            it.message
                    )
            }
        }
    }
}