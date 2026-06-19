package es.upc.waypass.data.dto

data class MembershipResponse(
    val id: Int,
    val companyId: Int,
    val userId: Int,
    val username: String?,         // puede venir null (ver §0)
    val memberRole: String,
    val joinedAt: String
)
