package com.example.mapquest.Network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationIqApiService {
    @GET("v1/autocomplete")
    suspend fun searchAddress(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): List<LocationIqResponse>
}

data class LocationIqResponse(
    val display_name: String,
    val lat: String,
    val lon: String,
    val address: Map<String, String>?
)

object RetrofitClient {
    private const val BASE_URL = "https://api.locationiq.com/"

    val apiService: LocationIqApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocationIqApiService::class.java)
    }
}