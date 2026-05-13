package es.upc.waypass.data.remote

import es.upc.waypass.data.dto.AuthenticatedUserResponse
import es.upc.waypass.data.dto.CompanyDto
import es.upc.waypass.data.dto.CreateRouteRequest
import es.upc.waypass.data.dto.CreateSubscriptionResponse
import es.upc.waypass.data.dto.DistrictDto
import es.upc.waypass.data.dto.ProvinceDto
import es.upc.waypass.data.dto.RegionDto
import es.upc.waypass.data.dto.RouteDto
import es.upc.waypass.data.dto.SignInRequest
import es.upc.waypass.data.dto.SignUpRequest
import es.upc.waypass.data.dto.SignUpResponse
import es.upc.waypass.data.dto.StopDto
import es.upc.waypass.data.dto.SubscriptionStatusResponse
import es.upc.waypass.data.dto.UpdateRouteRequest
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

    @POST("v1/subscriptions")
    suspend fun createSubscription(): CreateSubscriptionResponse

    @GET("v1/subscriptions/me")
    suspend fun getSubscriptionStatus(): SubscriptionStatusResponse

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

    @PUT("stops/{id}")
    suspend fun updateStop(
        @Path("id") id: Int,
        @Body stop: StopDto
    ): StopDto

    @GET("geographic/regions")
    suspend fun getRegions(): List<RegionDto>

    @GET("geographic/provinces/region/{regionId}")
    suspend fun getProvincesByRegion(
        @Path("regionId") regionId: Int
    ): List<ProvinceDto>

    @GET("geographic/districts/province/{provinceId}")
    suspend fun getDistrictsByProvince(
        @Path("provinceId") provinceId: Int
    ): List<DistrictDto>
}