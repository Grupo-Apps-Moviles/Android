package es.upc.waypass.data.model

data class StopDto(
    val id: Int,
    val name: String,
    val googleMapsUrl: String?,
    val imageUrl: String?,
    val phone: String,
    val fkIdCompany: Int,
    val address: String,
    val reference: String,
    val fkIdDistrict: Int
)