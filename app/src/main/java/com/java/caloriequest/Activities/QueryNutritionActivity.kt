package com.java.caloriequest.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.java.caloriequest.API.RetrofitClient
import com.java.caloriequest.Adapter.NutritionAdapter
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityQueryNutritionBinding
import com.java.caloriequest.model.NutritionData
import com.java.caloriequest.model.NutritionFacts
import com.java.caloriequest.model.NutritionResponse
import com.java.caloriequest.model.TotalNutritionData
import retrofit2.Call
import retrofit2.Response

class QueryNutritionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQueryNutritionBinding
    private lateinit var nutritionAdapter: NutritionAdapter
    private var nutritionList: MutableList<NutritionFacts> = mutableListOf()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    // Create a TotalNutritionData object to store the totals
    private var totalNutritionData = TotalNutritionData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQueryNutritionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)
        upArrow?.setColorFilter(
            ContextCompat.getColor(this, android.R.color.white),
            android.graphics.PorterDuff.Mode.SRC_ATOP
        )
        supportActionBar?.setHomeAsUpIndicator(upArrow)

        nutritionAdapter = NutritionAdapter(nutritionList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = nutritionAdapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotEmpty()) {
                    val category = intent.getStringExtra("category").toString()
                    binding.hintImage.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    apiCall(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        binding.searchView.setOnClickListener(View.OnClickListener {
            val query = binding.searchView.query.toString()
            binding.hintImage.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            apiCall(query)
        })

        val saveButton = binding.saveButton
        saveButton.visibility = View.GONE

        saveButton.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userUid = currentUser.uid
                val category = intent.getStringExtra("category").toString()
                val userCollection = firestore.collection("users").document(userUid)
                val categoryCollection = userCollection.collection(category)

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

                    // Update total nutrition data
                    totalNutritionData.totalCalories += nutritionFact.calories.toDouble()
                    totalNutritionData.totalProtein += nutritionFact.protein.toDouble()
                    totalNutritionData.totalFats += nutritionFact.Fats.toDouble()
                    totalNutritionData.totalCarbs += nutritionFact.carb.toDouble()
                }

                // Save total nutrition data to Firestore
                userCollection.set(totalNutritionData)
                    .addOnSuccessListener {
                        // Data saved successfully
                        dataSavedSuccessfully = true
                    }
                    .addOnFailureListener { e ->
                        dataSavedSuccessfully = false
                    }

                // Show the toast message based on the flag
                if (dataSavedSuccessfully) {
                    Toast.makeText(
                        this@QueryNutritionActivity,
                        "Nutrition data saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@QueryNutritionActivity,
                    "You need to be logged in to save nutrition data.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun apiCall(query: String) {
        RetrofitClient().getUserService()?.getNutritionInfo(query, "OY5GqRA9rgiY2XY432YRVg==VlbGgAMGMcAncyzS")
            ?.enqueue(object : retrofit2.Callback<NutritionResponse> {
                override fun onResponse(
                    call: Call<NutritionResponse>,
                    response: Response<NutritionResponse>
                ) {
                    if (response.isSuccessful) {
                        val nutritionResponse = response.body()
                        if (nutritionResponse != null) {
                            val nutritionList = nutritionResponse.items.map { item ->
                                NutritionFacts(
                                    item.name,
                                    item.serving_size_g.toInt().toString(),
                                    item.calories.toInt().toString(),
                                    item.protein_g.toInt().toString(),
                                    item.fat_total_g.toInt().toString(),
                                    item.carbohydrates_total_g.toInt().toString()
                                )
                            }

                            nutritionAdapter.updateData(nutritionList)

                            binding.progressBar.visibility = View.GONE
                            binding.linearLayout.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.saveButton.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFailure(call: Call<NutritionResponse>, t: Throwable) {
                    Toast.makeText(this@QueryNutritionActivity, "failed to resolve query", Toast.LENGTH_SHORT).show()
                }
            })
    }
}