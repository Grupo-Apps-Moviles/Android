package es.upc.waypass.data.dto

data class CompanyDto(
    val id: Int,
    val name: String,
    val logoUrl: String,
    val fkIdUser: Int
)