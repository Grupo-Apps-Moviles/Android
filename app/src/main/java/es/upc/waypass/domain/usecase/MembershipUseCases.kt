package es.upc.waypass.domain.usecase

import es.upc.waypass.domain.model.CompanyMember
import es.upc.waypass.domain.model.Membership
import es.upc.waypass.domain.repository.MembershipRepository
import javax.inject.Inject

class GetMyMembershipUseCase @Inject constructor(
    private val repository: MembershipRepository
) {
    suspend operator fun invoke(): Result<Membership?> =
        repository.getMyMembership()
}

class JoinCompanyUseCase @Inject constructor(
    private val repository: MembershipRepository
) {
    suspend operator fun invoke(code: String): Result<Membership> =
        repository.joinCompany(code)
}

class LeaveCompanyUseCase @Inject constructor(
    private val repository: MembershipRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        repository.leaveCompany()
}

class GetCompanyMembersUseCase @Inject constructor(
    private val repository: MembershipRepository
) {
    suspend operator fun invoke(companyId: Int): Result<List<CompanyMember>> =
        repository.getCompanyMembers(companyId)
}

class RemoveMemberUseCase @Inject constructor(
    private val repository: MembershipRepository
) {
    suspend operator fun invoke(membershipId: Int): Result<Unit> =
        repository.removeMember(membershipId)
}

class RegenerateInvitationCodeUseCase @Inject constructor(
    private val repository: MembershipRepository
) {
    suspend operator fun invoke(companyId: Int): Result<String> =
        repository.regenerateInvitationCode(companyId)
}
