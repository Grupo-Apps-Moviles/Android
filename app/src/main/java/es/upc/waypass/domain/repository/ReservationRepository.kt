package es.upc.waypass.domain.repository

import es.upc.waypass.data.remote.ReservationResponse
import es.upc.waypass.data.remote.WayPassApiService

class ReservationRepository(
    private val apiService: WayPassApiService
) {

    suspend fun getDriverReservations(
        driverId: Int
    ): Result<List<ReservationResponse>> {

        return try {

            val response =
                apiService
                    .getDriverReservations(
                        driverId
                    )

            if(response.isSuccessful) {

                Result.success(
                    response.body()
                        ?: emptyList()
                )

            } else {

                Result.failure(
                    Exception(
                        "Error backend"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}