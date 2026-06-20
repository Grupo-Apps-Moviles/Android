package es.upc.waypass.domain.repository

import es.upc.waypass.domain.model.CompanyMember
import es.upc.waypass.domain.model.Membership

interface MembershipRepository {
    suspend fun getMyMembership(): Result<Membership?>          // null = no pertenece a empresa
    suspend fun joinCompany(code: String): Result<Membership>
    suspend fun leaveCompany(): Result<Unit>
    suspend fun getCompanyMembers(companyId: Int): Result<List<CompanyMember>>
    suspend fun removeMember(membershipId: Int): Result<Unit>
    suspend fun regenerateInvitationCode(companyId: Int): Result<String>  // nuevo código
}
