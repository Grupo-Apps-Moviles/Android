package es.upc.waypass.domain.model

data class Membership(
    val companyId: Int,
    val companyName: String,
    val logoUrl: String?,
    val isAdmin: Boolean,
    val invitationCode: String?
)

data class CompanyMember(
    val membershipId: Int,
    val userId: Int,
    val displayName: String,   // username ?: "Usuario #<userId>"
    val isAdmin: Boolean,
    val joinedAt: String
)
