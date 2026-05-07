package es.upc.waypass.presentation.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.upc.waypass.data.model.CompanyDto
import es.upc.waypass.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

data class DriverState(
    val isLoading: Boolean = false,
    val company: CompanyDto? = null,
    val needsCompany: Boolean = false,
    val message: String = ""
)

class DriverViewModel : ViewModel() {

    private val _state = MutableStateFlow(DriverState())
    val state: StateFlow<DriverState> = _state

    fun checkCompany(userId: Int) {
        viewModelScope.launch {
            try {
                _state.value = DriverState(isLoading = true)

                val company = RetrofitClient.api.getCompanyByUserId(userId)

                _state.value = DriverState(
                    company = company,
                    needsCompany = false
                )
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 404) {
                    _state.value = DriverState(
                        needsCompany = true,
                        message = "Debes crear tu empresa"
                    )
                } else {
                    _state.value = DriverState(
                        message = e.message ?: "Error al verificar empresa"
                    )
                }
            }
        }
    }

    fun createCompany(name: String, userId: Int) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val nameBody = name.trim().toRequestBody("text/plain".toMediaType())
                val userIdBody = userId.toString().toRequestBody("text/plain".toMediaType())

                val company = RetrofitClient.api.createCompany(
                    name = nameBody,
                    fkIdUser = userIdBody
                )

                _state.value = DriverState(
                    company = company,
                    needsCompany = false,
                    message = "Empresa creada correctamente"
                )
            } catch (e: Exception) {
                _state.value = DriverState(
                    needsCompany = true,
                    message = e.message ?: "Error al crear empresa"
                )
            }
        }
    }
}