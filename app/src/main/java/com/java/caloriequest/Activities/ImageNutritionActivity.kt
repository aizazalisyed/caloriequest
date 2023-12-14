package com.java.caloriequest.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.java.caloriequest.API.NutritionalInfoRequest
import com.java.caloriequest.API.RetrofitClient
import com.java.caloriequest.Adapter.NutritionAdapter
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityImageNutritionBinding
import com.java.caloriequest.databinding.ActivityMainBinding
import com.java.caloriequest.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
private val firestore = FirebaseFirestore.getInstance()

class ImageNutritionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageNutritionBinding
    private lateinit var nutritionAdapter: NutritionAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageNutritionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)
        upArrow?.setColorFilter(
            ContextCompat.getColor(this, android.R.color.white),
            android.graphics.PorterDuff.Mode.SRC_ATOP
        )
        supportActionBar?.setHomeAsUpIndicator(upArrow)

        // Retrieve imageFile and category from the intent
        val imageFile = intent.getSerializableExtra("imageFile") as File

        sendImageToAPI(imageFile)

        Log.d(" ImageNutritionActivity", "onCreate is called")

        binding.image.setImageURI(null) // Clear previous image if any
        binding.image.setImageURI(android.net.Uri.fromFile(imageFile))

        binding.saveButton.setOnClickListener {
            saveNutritionDataToFirebase(nutritionAdapter.getData())
            startActivity(Intent(this@ImageNutritionActivity, MainActivity::class.java))
            finish()
        }


    }

    private fun sendImageToAPI(imageFile: File) {
        // Use Retrofit to send the imageFile to your API for segmentation
        val imageRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

        val userService = RetrofitClient().getUserService2() // Use your Retrofit service
        val authorizationHeader = "Bearer 6c6a5bb5b421aa2ed2bccb382810d035d3f81535" // Replace with your actual API token
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
        val authorizationHeader = "Bearer 6c6a5bb5b421aa2ed2bccb382810d035d3f81535" // Replace with your actual API token

        userService?.getNutritionalInfo(nutritionalInfoRequest, authorizationHeader)?.enqueue(
            object : Callback<FoodInfo> {
                override fun onResponse(
                    call: Call<FoodInfo>,
                    response: Response<FoodInfo>
                ) {

                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val nutritionalInfoResponse = response.body()
                        if (nutritionalInfoResponse != null) {
                            // Extract data from the response
                            // Extract data from the response
                            val foodName = nutritionalInfoResponse.foodName[0]
                            val servingSize = nutritionalInfoResponse.serving_size.toInt().toString()
                            val calories = nutritionalInfoResponse.nutritional_info.calories.toInt().toString()
                            // Access nutrients' quantities directly from the totalNutrients map
                            val fat = nutritionalInfoResponse.nutritional_info.dailyIntakeReference.FAT.percent.toInt().toString()
                            val carbs = nutritionalInfoResponse.nutritional_info.dailyIntakeReference.CHOCDF.percent.toInt().toString()
                            val protein = nutritionalInfoResponse.nutritional_info.dailyIntakeReference.PROCNT.percent.toInt().toString()

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

                override fun onFailure(call: Call<FoodInfo>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Log.e("ImageNutritionActivity", "API call failed with error: ${t.message}")
                    t.printStackTrace()
                }
            }
        )
    }

    private fun saveNutritionDataToFirebase(nutritionList: List<NutritionFacts>) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            val category = intent.getStringExtra("category").toString()
            val userCollection = firestore.collection("users").document(userUid)
            val categoryCollection = userCollection.collection(category)

            // Fetch the existing total nutrition data from Firestore
            userCollection.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val existingTotalNutritionData = documentSnapshot.toObject(
                            TotalNutritionData::class.java)
                        if (existingTotalNutritionData != null) {
                            // Update total nutrition data with new nutrition facts
                            for (nutritionFact in nutritionList) {
                                existingTotalNutritionData.totalCalories += nutritionFact.calories.toDouble()
                                existingTotalNutritionData.totalProtein += nutritionFact.protein.toDouble()
                                existingTotalNutritionData.totalFats += nutritionFact.Fats.toDouble()
                                existingTotalNutritionData.totalCarbs += nutritionFact.carb.toDouble()
                            }

                            // Save the updated total nutrition data back to Firestore
                            userCollection.set(existingTotalNutritionData)
                                .addOnSuccessListener {
                                    // Data saved successfully
                                    Toast.makeText(
                                        this@ImageNutritionActivity,
                                        "Nutrition data saved successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    // Handle failure to save data
                                    Toast.makeText(
                                        this@ImageNutritionActivity,
                                        "Failed to save nutrition data",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            // Handle the case where existingTotalNutritionData is null
                            Toast.makeText(
                                this@ImageNutritionActivity,
                                "Failed to fetch existing nutrition data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Document doesn't exist, create a new total nutrition data
                        // Initialize a new TotalNutritionData object
                        val newTotalNutritionData = TotalNutritionData()

                        // Update the new total nutrition data with the new nutrition facts
                        for (nutritionFact in nutritionList) {
                            newTotalNutritionData.totalCalories += nutritionFact.calories.toDouble()
                            newTotalNutritionData.totalProtein += nutritionFact.protein.toDouble()
                            newTotalNutritionData.totalFats += nutritionFact.Fats.toDouble()
                            newTotalNutritionData.totalCarbs += nutritionFact.carb.toDouble()
                        }

                        // Save the new total nutrition data to Firestore
                        userCollection.set(newTotalNutritionData)
                            .addOnSuccessListener {
                                // Data saved successfully
                                Toast.makeText(
                                    this@ImageNutritionActivity,
                                    "Nutrition data saved successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                // Handle failure to save data
                                Toast.makeText(
                                    this@ImageNutritionActivity,
                                    "Failed to save nutrition data",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure to fetch data
                    Toast.makeText(
                        this@ImageNutritionActivity,
                        "Failed to fetch existing nutrition data",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            // Save individual nutrition data to the "categoryCollection" as before
            var dataSavedSuccessfully = false
            for (nutritionFact in nutritionList) {
                val nutritionData = NutritionData(
                    name = nutritionFact.foodName,
                    servingSize = nutritionFact.servingSize,
                    calories = nutritionFact.calories,
                    protein = nutritionFact.protein,
                    fats = nutritionFact.Fats,
                    carbs = nutritionFact.carb
                )

                categoryCollection.add(nutritionData)
                    .addOnSuccessListener { documentReference ->
                        dataSavedSuccessfully = true
                    }
                    .addOnFailureListener { e ->
                        dataSavedSuccessfully = false
                    }
            }

            // Show the toast message based on the flag
            if (dataSavedSuccessfully) {
                Toast.makeText(
                    this@ImageNutritionActivity,
                    "Nutrition data saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this@ImageNutritionActivity,
                "You need to be logged in to save nutrition data.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}