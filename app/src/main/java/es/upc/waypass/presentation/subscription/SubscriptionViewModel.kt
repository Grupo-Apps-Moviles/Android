package es.upc.waypass.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.upc.waypass.domain.usecase.CreateSubscriptionUseCase
import es.upc.waypass.domain.usecase.GetSubscriptionStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SubscriptionUiState {
    data object Idle : SubscriptionUiState()
    data object Loading : SubscriptionUiState()

    data class OpenPayPal(val approvalUrl: String) : SubscriptionUiState()

    data class Active(val expiresAt: String?) : SubscriptionUiState()

    data object Inactive : SubscriptionUiState()

    data class Error(val message: String) : SubscriptionUiState()
}

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val createSubscriptionUseCase: CreateSubscriptionUseCase,
    private val getSubscriptionStatusUseCase: GetSubscriptionStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Idle)
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    fun startSubscription() {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Loading

            createSubscriptionUseCase()
                .onSuccess { approvalUrl ->
                    _uiState.value = SubscriptionUiState.OpenPayPal(approvalUrl)
                }
                .onFailure { error ->
                    _uiState.value = SubscriptionUiState.Error(
                        error.message ?: "Error al iniciar suscripción"
                    )
                }
        }
    }

    fun checkSubscriptionStatus() {
        viewModelScope.launch {
            _uiState.value = SubscriptionUiState.Loading

            getSubscriptionStatusUseCase()
                .onSuccess { status ->
                    _uiState.value = if (status.isActive) {
                        SubscriptionUiState.Active(status.expiresAt)
                    } else {
                        SubscriptionUiState.Inactive
                    }
                }
                .onFailure { error ->
                    _uiState.value = SubscriptionUiState.Error(
                        error.message ?: "Error al verificar suscripción"
                    )
                }
        }
    }

    fun onPayPalSuccess() {
        checkSubscriptionStatus()
    }

    fun onPayPalCancelled() {
        _uiState.value = SubscriptionUiState.Idle
    }
}