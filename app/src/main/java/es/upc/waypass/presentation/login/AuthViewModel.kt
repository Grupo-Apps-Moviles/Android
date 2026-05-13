package es.upc.waypass.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.upc.waypass.MyApplication
import es.upc.waypass.data.auth.TokenManager
import es.upc.waypass.data.model.SignInRequest
import es.upc.waypass.data.model.SignUpRequest
import es.upc.waypass.data.model.WayPassApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val message: String = "",
    val success: Boolean = false,
    val userId: Int? = null,
    val role: Int? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: WayPassApiService,
    private val tokenManager: TokenManager
): ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _state.value = AuthState(isLoading = true)

                val response = api.signIn(
                    SignInRequest(
                        email = email.trim(),
                        password = password.trim()
                    )
                )

                tokenManager.saveToken(response.token)
                tokenManager.saveUserId(response.id)
                tokenManager.saveRole(response.role)

                _state.value = AuthState(
                    success = true,
                    message = "Inicio de sesión correcto",
                    userId = response.id,
                    role = response.role
                )
            } catch (e: Exception) {
                _state.value = AuthState(
                    success = false,
                    message = e.toString()
                )
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        userType: String
    ) {
        viewModelScope.launch {
            try {
                _state.value = AuthState(isLoading = true)

                val role = if (userType == "Conductor") 1 else 0

                api.signUp(
                    SignUpRequest(
                        username = name.trim(),
                        email = email.trim(),
                        password = password.trim(),
                        role = role
                    )
                )

                _state.value = AuthState(
                    success = true,
                    message = "Registro correcto"
                )
            } catch (e: Exception) {
                val errorMessage = if (e is retrofit2.HttpException) {
                    e.response()?.errorBody()?.string() ?: e.toString()
                } else {
                    e.toString()
                }

                _state.value = AuthState(
                    success = false,
                    message = errorMessage
                )
            }
        }
    }
}