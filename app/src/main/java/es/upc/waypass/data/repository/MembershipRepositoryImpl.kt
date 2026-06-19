package es.upc.waypass.data.repository

import es.upc.waypass.data.dto.JoinCompanyRequest
import es.upc.waypass.data.dto.MyMembershipResponse
import es.upc.waypass.data.remote.WayPassApiService
import es.upc.waypass.domain.model.CompanyMember
import es.upc.waypass.domain.model.Membership
import es.upc.waypass.domain.repository.MembershipRepository
import javax.inject.Inject

class MembershipRepositoryImpl @Inject constructor(
    private val api: WayPassApiService
) : MembershipRepository {

    override suspend fun getMyMembership(): Result<Membership?> =
        runCatching {
            val response = api.getMyMembership()
            when {
                response.code() == 404 -> null   // conductor sin empresa (caso normal)
                response.isSuccessful -> {
                    val body = response.body()
                        ?: error("Respuesta vacía de membresía")
                    body.toMembership()
                }
                else -> error("Error al cargar la membresía: ${response.code()}")
            }
        }

    override suspend fun joinCompany(code: String): Result<Membership> =
        runCatching {
            val response = api.joinCompany(JoinCompanyRequest(invitationCode = code))
            when {
                response.code() == 404 -> error("Código de invitación inválido")
                response.code() == 409 -> error("Ya perteneces a una empresa")
                response.isSuccessful -> {
                    val body = response.body()
                        ?: error("Respuesta vacía al unirse a la empresa")
                    body.toMembership()
                }
                else -> error("Error al unirse a la empresa: ${response.code()}")
            }
        }

    override suspend fun leaveCompany(): Result<Unit> =
        runCatching {
            val response = api.leaveCompany()
            when {
                response.code() == 409 ->
                    error("Un administrador no puede abandonar la empresa")
                response.isSuccessful -> Unit
                else -> error("Error al salir de la empresa: ${response.code()}")
            }
        }

    override suspend fun getCompanyMembers(companyId: Int): Result<List<CompanyMember>> =
        runCatching {
            api.getCompanyMembers(companyId).map { item ->
                CompanyMember(
                    membershipId = item.id,
                    userId = item.userId,
                    displayName = item.username ?: "Usuario #${item.userId}",
                    isAdmin = item.memberRole == "Admin",
                    joinedAt = item.joinedAt
                )
            }
        }

    override suspend fun removeMember(membershipId: Int): Result<Unit> =
        runCatching {
            val response = api.removeMember(membershipId)
            when {
                response.code() == 403 -> error("No tienes permisos")
                response.isSuccessful -> Unit
                else -> error("Error al expulsar al miembro: ${response.code()}")
            }
        }

    override suspend fun regenerateInvitationCode(companyId: Int): Result<String> =
        runCatching {
            api.regenerateInvitationCode(companyId).invitationCode ?: ""
        }

    private fun MyMembershipResponse.toMembership() = Membership(
        companyId = companyId,
        companyName = companyName,
        logoUrl = logoUrl,
        isAdmin = memberRole == "Admin",
        invitationCode = invitationCode
    )
}
