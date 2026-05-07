package es.upc.waypass.data.model

data class CompanyDto(
    val id: Int,
    val name: String,
    val logoUrl: String,
    val fkIdUser: Int
)