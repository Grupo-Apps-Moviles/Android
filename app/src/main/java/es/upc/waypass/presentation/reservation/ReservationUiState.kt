package es.upc.waypass.presentation.reservation

import es.upc.waypass.data.remote.ReservationResponse

data class ReservationUiState(

    val isLoading: Boolean = false,

    val reservations:
    List<ReservationResponse> =
        emptyList(),

    val totalEarnings: Double = 0.0,

    val error: String? = null
)