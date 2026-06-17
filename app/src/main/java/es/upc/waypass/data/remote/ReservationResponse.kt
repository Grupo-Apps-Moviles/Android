package es.upc.waypass.data.remote

data class ReservationResponse(

    val id: Int,

    val userId: Int,

    val driverId: Int,

    val status: String,

    val amount: Double,

    val driverEarnings: Double,

    val platformFee: Double,

    val paypalTransactionId: String,

    val createdAt: String
)