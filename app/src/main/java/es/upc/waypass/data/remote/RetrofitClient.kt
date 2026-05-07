package es.upc.waypass.data.remote

import es.upc.waypass.data.model.WayPassApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:5191/api/"

    val api: WayPassApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WayPassApiService::class.java)
    }
}