package es.upc.waypass.data.model

data class RegionDto(
    val id: Int,
    val name: String
)

data class ProvinceDto(
    val id: Int,
    val name: String,
    val fkIdRegion: Int
)

data class DistrictDto(
    val id: Int,
    val name: String,
    val fkIdProvince: Int
)