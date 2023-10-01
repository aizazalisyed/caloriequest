package com.java.caloriequest.API


import com.java.caloriequest.model.FoodInfo
import com.java.caloriequest.model.ImageSegmentationResponse
import com.java.caloriequest.model.NutritionResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface UserService {

    @GET("/v1/nutrition")
    fun getNutritionInfo(
        @Query("query") query: String,
        @Header("X-Api-Key") apiKey: String
    ): Call<NutritionResponse>


    @Multipart
    @POST("image/segmentation/complete")
    fun segmentImage(
        @Part image: MultipartBody.Part,
        @Header("Authorization") authorization: String
    ): Call<ImageSegmentationResponse>

    @POST("recipe/nutritionalInfo")
    fun getNutritionalInfo(
        @Body request: NutritionalInfoRequest,
        @Header("Authorization") authorization: String
    ): Call<FoodInfo>

}