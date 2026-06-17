package es.upc.waypass.presentation.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.upc.waypass.domain.repository.ReservationRepository

class ReservationViewModelFactory(
    private val repository:
    ReservationRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel>
            create(
        modelClass: Class<T>
    ): T {

        return ReservationViewModel(
            repository
        ) as T
    }
}