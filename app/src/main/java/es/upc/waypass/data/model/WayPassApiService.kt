package es.upc.waypass.data.model

import es.upc.waypass.data.model.AuthenticatedUserResponse
import es.upc.waypass.data.model.RouteDto
import es.upc.waypass.data.model.SignInRequest
import es.upc.waypass.data.model.SignUpRequest
import es.upc.waypass.data.model.SignUpResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface WayPassApiService {

    @POST("Authentication/sign-in")
    suspend fun signIn(
        @Body request: SignInRequest
    ): AuthenticatedUserResponse

    @POST("Authentication/sign-up")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): SignUpResponse

    @GET("Routes")
    suspend fun getRoutes(): List<RouteDto>

    @GET("Stops/company/{companyId}")
    suspend fun getStopsByCompanyId(
        @Path("companyId") companyId: Int
    ): List<StopDto>

    @GET("Routes/company/{companyId}")
    suspend fun getRoutesByCompanyId(
        @Path("companyId") companyId: Int
    ): List<RouteDto>

    @GET("Companies/user/{FKeyIdUser}")
    suspend fun getCompanyByUserId(
        @Path("FKeyIdUser") userId: Int
    ): CompanyDto

    @Multipart
    @POST("Companies")
    suspend fun createCompany(
        @Part("Name") name: RequestBody,
        @Part("FkIdUser") fkIdUser: RequestBody,
        @Part logoFile: MultipartBody.Part? = null
    ): CompanyDto

    //STOPS

    @GET("Stops/company/{FkIdCompany}")
    suspend fun getStopsByCompany(
        @Path("FkIdCompany") companyId: Int
    ): List<StopDto>

    @Multipart
    @POST("Stops")
    suspend fun createStop(
        @Part("Name") name: RequestBody,
        @Part("GoogleMapsUrl") googleMapsUrl: RequestBody,
        @Part("Phone") phone: RequestBody,
        @Part("FkIdCompany") fkIdCompany: RequestBody,
        @Part("Address") address: RequestBody,
        @Part("Reference") reference: RequestBody,
        @Part("FkIdDistrict") fkIdDistrict: RequestBody,
        @Part imageFile: MultipartBody.Part? = null
    ): StopDto

    @DELETE("Stops/{id}")
    suspend fun deleteStop(
        @Path("id") stopId: Int
    )

    //Geographic

    @GET("Geographic/districts")
    suspend fun getDistricts(): List<DistrictDto>

    @GET("Routes/company/{FkIdCompany}")
    suspend fun getRoutesByCompany(
        @Path("FkIdCompany") companyId: Int
    ): List<RouteDto>

    @POST("Routes")
    suspend fun createRoute(
        @Body request: CreateRouteRequest
    ): RouteDto

    @PUT("Routes/{id}")
    suspend fun updateRoute(
        @Path("id") routeId: Int,
        @Body request: UpdateRouteRequest
    ): RouteDto

    @DELETE("Routes/{id}")
    suspend fun deleteRoute(
        @Path("id") routeId: Int
    )

}