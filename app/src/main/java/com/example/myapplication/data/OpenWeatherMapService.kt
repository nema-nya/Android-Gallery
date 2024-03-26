package com.example.myapplication.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapService {
    @GET("weather")
    fun getCurrentWeatherData(@Query("lat") lat: Double, @Query("lon") long: Double, @Query("appid") apiKey: String): Call<WeatherData>
}
