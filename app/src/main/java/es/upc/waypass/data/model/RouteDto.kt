package es.upc.waypass.data.model

data class RouteDto(
    val id: Int,
    val price: Double,
    val frequency: Int,
    val duration: Int,
    val stops: List<RouteStopDto>,
    val schedules: List<RouteScheduleDto>
)

data class RouteStopDto(
    val id: Int,
    val name: String,
    val googleMapsUrl: String?,
    val imageUrl: String?,
    val address: String,
    val fkIdCompany: Int,
    val fkIdDistrict: Int
)

data class RouteScheduleDto(
    val startTime: String,
    val endTime: String,
    val dayOfWeek: String,
    val enabled: Boolean
)

data class CreateRouteRequest(
    val frequency: Int,
    val price: Double,
    val duration: Int,
    val stopsIds: List<Int>,
    val schedules: List<CreateScheduleRequest>
)

data class CreateScheduleRequest(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val enabled: Boolean
)

data class UpdateRouteRequest(
    val price: Double,
    val duration: Int,
    val frequency: Int,
    val stopsIds: List<Int>,
    val schedules: List<CreateScheduleRequest>
)