package es.upc.waypass.data.model

data class SignInRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: Int
)

data class AuthenticatedUserResponse(
    val id: Int,
    val username: String,
    val role: Int,
    val token: String
)

data class SignUpResponse(
    val message: String
)