package es.upc.waypass.data.dto

data class MyMembershipResponse(
    val companyId: Int,
    val companyName: String,
    val logoUrl: String?,
    val memberRole: String,        // "Admin" | "Driver"
    val invitationCode: String?    // null si no es admin
)
