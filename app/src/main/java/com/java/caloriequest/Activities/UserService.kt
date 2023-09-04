package com.java.caloriequest.Activities


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UserService {

    @GET("/v1/nutrition")
    fun getNutritionInfo(
        @Query("query") query: String,
        @Header("X-Api-Key") apiKey: String
    ): Call<NutritionResponse>

}