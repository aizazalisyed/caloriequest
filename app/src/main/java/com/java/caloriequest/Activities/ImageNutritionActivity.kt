package com.java.caloriequest.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.java.caloriequest.API.NutritionalInfoRequest
import com.java.caloriequest.API.RetrofitClient
import com.java.caloriequest.Adapter.NutritionAdapter
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityImageNutritionBinding
import com.java.caloriequest.databinding.ActivityMainBinding
import com.java.caloriequest.model.ImageSegmentationResponse
import com.java.caloriequest.model.NutritionFacts
import com.java.caloriequest.model.NutritionalInfoResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ImageNutritionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageNutritionBinding
    private lateinit var nutritionAdapter: NutritionAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageNutritionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Retrieve imageFile and category from the intent
        val imageFile = intent.getSerializableExtra("imageFile") as File
        val category = intent.getStringExtra("category")

        sendImageToAPI(imageFile)

        Log.d(" ImageNutritionActivity", "onCreate is called")

        binding.image.setImageURI(null) // Clear previous image if any
        binding.image.setImageURI(android.net.Uri.fromFile(imageFile))

    }

    private fun sendImageToAPI(imageFile: File) {
        // Use Retrofit to send the imageFile to your API for segmentation
        val imageRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

        val userService = RetrofitClient().getUserService2() // Use your Retrofit service
        val authorizationHeader = "Bearer 64c9f582ccdaf40793154e7df768d5ae6b0eac0b" // Replace with your actual API token
        userService?.segmentImage(imagePart, authorizationHeader)?.enqueue(
            object : Callback<ImageSegmentationResponse> {
                override fun onResponse(
                    call: Call<ImageSegmentationResponse>,
                    response: Response<ImageSegmentationResponse>
                ) {
                    if (response.isSuccessful) {
                        val imageSegmentationResponse = response.body()
                        if (imageSegmentationResponse != null) {
                            val imageId = imageSegmentationResponse.imageId
                            // TODO: Get nutritional information based on the imageId
                            getNutritionalInfo(imageId)
                        } else {
                            // Handle the case where imageSegmentationResponse is null
                        }
                    } else {
                        // Handle API error
                    }
                }

                override fun onFailure(call: Call<ImageSegmentationResponse>, t: Throwable) {
                    // Handle API call failure
                }
            }
        )
    }

    private fun getNutritionalInfo(imageId: Int) {
        Log.d("ImageNutritionActivity", "getNutritionalInfo called with imageId: $imageId")
        val userService = RetrofitClient().getUserService2() // Use your Retrofit service
        val nutritionalInfoRequest = NutritionalInfoRequest(imageId)
        val authorizationHeader = "Bearer 64c9f582ccdaf40793154e7df768d5ae6b0eac0b" // Replace with your actual API token

        userService?.getNutritionalInfo(nutritionalInfoRequest, authorizationHeader)?.enqueue(
            object : Callback<NutritionalInfoResponse> {
                override fun onResponse(
                    call: Call<NutritionalInfoResponse>,
                    response: Response<NutritionalInfoResponse>
                ) {
                    if (response.isSuccessful) {
                        val nutritionalInfoResponse = response.body()
                        if (nutritionalInfoResponse != null) {
                            // Extract data from the response
                            // Extract data from the response
                            val foodName = nutritionalInfoResponse.foodName[0]
                            val servingSize = nutritionalInfoResponse.serving_size.toString()
                            val calories = nutritionalInfoResponse.nutritional_info.calories.toString()

                            // Access nutrients' quantities directly from the totalNutrients map
                            val fat = nutritionalInfoResponse.nutritional_info.totalNutrients["FAT"]?.percent.toString()
                            val carbs = nutritionalInfoResponse.nutritional_info.totalNutrients["CHOCDF"]?.percent.toString()
                            val protein = nutritionalInfoResponse.nutritional_info.totalNutrients["PROCNT"]?.percent.toString()

                            // Create a NutritionFacts object
                            val nutritionFact = NutritionFacts(
                                foodName,
                                servingSize,
                                calories,
                                fat,
                                carbs,
                                protein
                            )

                            nutritionAdapter = NutritionAdapter((listOf<NutritionFacts>( nutritionFact) as MutableList<NutritionFacts>))
                            binding.recyclerView.layoutManager = LinearLayoutManager(this@ImageNutritionActivity)
                            binding.recyclerView.adapter = nutritionAdapter

                        } else {
                            Log.d("ImageNutritionActivity", "nutritionalInfoResponse is null")
                        }
                    } else {
                        Log.d("ImageNutritionActivity", "API error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<NutritionalInfoResponse>, t: Throwable) {
                    Log.e("ImageNutritionActivity", "API call failed with error: ${t.message}")
                    t.printStackTrace()
                }
            }
        )
    }


}