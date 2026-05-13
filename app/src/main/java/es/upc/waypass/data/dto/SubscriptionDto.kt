package es.upc.waypass.data.dto

data class CreateSubscriptionResponse(
    val approvalUrl: String
)

data class SubscriptionStatusResponse(
    val isActive: Boolean,
    val status: String,
    val expiresAt: String?
)

data class SubscriptionStatus(
    val isActive: Boolean,
    val status: String,
    val expiresAt: String?
)