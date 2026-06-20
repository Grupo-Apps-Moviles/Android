package es.upc.waypass.presentation.membership

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.upc.waypass.domain.model.CompanyMember
import es.upc.waypass.domain.model.Membership
import es.upc.waypass.domain.usecase.GetCompanyMembersUseCase
import es.upc.waypass.domain.usecase.GetMyMembershipUseCase
import es.upc.waypass.domain.usecase.JoinCompanyUseCase
import es.upc.waypass.domain.usecase.LeaveCompanyUseCase
import es.upc.waypass.domain.usecase.RegenerateInvitationCodeUseCase
import es.upc.waypass.domain.usecase.RemoveMemberUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MembershipUiState(
    val isLoading: Boolean = false,
    val membership: Membership? = null,    // null tras cargar = no pertenece
    val loaded: Boolean = false,           // ya se intentó cargar /me
    val members: List<CompanyMember> = emptyList(),
    val membersLoading: Boolean = false,
    val actionMessage: String? = null,     // feedback de join/leave/remove
    val actionSuccess: Boolean = false,
    val regeneratedCode: String? = null,   // nuevo código tras regenerar
    val error: String? = null
)

private const val CONNECTION_ERROR = "Error de conexión. Intenta de nuevo."

@HiltViewModel
class MembershipViewModel @Inject constructor(
    private val getMyMembership: GetMyMembershipUseCase,
    private val joinCompany: JoinCompanyUseCase,
    private val leaveCompany: LeaveCompanyUseCase,
    private val getCompanyMembers: GetCompanyMembersUseCase,
    private val removeMember: RemoveMemberUseCase,
    private val regenerateCode: RegenerateInvitationCodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MembershipUiState())
    val uiState: StateFlow<MembershipUiState> = _uiState.asStateFlow()

    fun loadMyMembership() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getMyMembership()
                .onSuccess { membership ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            membership = membership,
                            loaded = true
                        )
                    }
                }
                .onFailure { error ->
                    // Nunca crashea: ante error de red dejamos loaded = true igualmente
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loaded = true,
                            error = error.message ?: CONNECTION_ERROR
                        )
                    }
                }
        }
    }

    fun join(code: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    actionSuccess = false,
                    actionMessage = null,
                    error = null
                )
            }

            joinCompany(code.trim())
                .onSuccess { membership ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            membership = membership,
                            loaded = true,
                            actionSuccess = true,
                            actionMessage = "Te uniste a ${membership.companyName}"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            actionSuccess = false,
                            error = error.message ?: CONNECTION_ERROR
                        )
                    }
                }
        }
    }

    fun leave() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    actionSuccess = false,
                    actionMessage = null,
                    error = null
                )
            }

            leaveCompany()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            membership = null,
                            actionSuccess = true,
                            actionMessage = "Saliste de la empresa"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            actionSuccess = false,
                            error = error.message ?: CONNECTION_ERROR
                        )
                    }
                }
        }
    }

    fun loadMembers(companyId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(membersLoading = true, error = null) }

            getCompanyMembers(companyId)
                .onSuccess { members ->
                    _uiState.update {
                        it.copy(membersLoading = false, members = members)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            membersLoading = false,
                            error = error.message ?: CONNECTION_ERROR
                        )
                    }
                }
        }
    }

    fun remove(membershipId: Int, companyId: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(actionMessage = null, error = null)
            }

            removeMember(membershipId)
                .onSuccess {
                    _uiState.update { it.copy(actionMessage = "Miembro expulsado") }
                    loadMembers(companyId)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: CONNECTION_ERROR)
                    }
                }
        }
    }

    fun regenerate(companyId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            regenerateCode(companyId)
                .onSuccess { newCode ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            membership = it.membership?.copy(invitationCode = newCode),
                            regeneratedCode = newCode,
                            actionMessage = "Código regenerado"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: CONNECTION_ERROR
                        )
                    }
                }
        }
    }

    /** Limpia los flags de feedback tras consumirlos en la UI. */
    fun consumeAction() {
        _uiState.update {
            it.copy(
                actionSuccess = false,
                actionMessage = null,
                regeneratedCode = null,
                error = null
            )
        }
    }
}
