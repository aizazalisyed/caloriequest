package com.java.caloriequest.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.java.caloriequest.API.RetrofitClient
import com.java.caloriequest.Adapter.NutritionAdapter
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityQueryNutritionBinding
import com.java.caloriequest.model.NutritionFacts
import com.java.caloriequest.model.NutritionResponse
import retrofit2.Call
import retrofit2.Response

class QueryNutritionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQueryNutritionBinding
    private lateinit var nutritionAdapter: NutritionAdapter
    private var nutritionList: MutableList<NutritionFacts> = mutableListOf() // Change to MutableList


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQueryNutritionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)

        // Enable the back button in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the color of the back arrow (up button) to white
        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24) // Replace with your back arrow icon resource
        upArrow?.setColorFilter(ContextCompat.getColor(this, android.R.color.white), android.graphics.PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)



        // Initialize the RecyclerView and its adapter
        nutritionAdapter = NutritionAdapter(nutritionList) // Pass the MutableList here
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = nutritionAdapter

        // Set an OnQueryTextListener for the SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle the search query here
                if (query != null && query.isNotEmpty()) {

                    // Retrieve the category string from the Intent
                    val category = intent.getStringExtra("category").toString()
                    // Hide hint image and show progress bar
                    binding.hintImage.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    apiCall(query,category)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // You can handle text changes here if needed
                return true
            }
        })

        // Set an OnClickListener for the SearchView to handle clicks on the search icon
        binding.searchView.setOnClickListener(View.OnClickListener {
            // Search icon was clicked, perform the API call here
            val query = binding.searchView.query.toString()
            val category = intent.getStringExtra("category").toString()
            // Hide hint image and show progress bar
            binding.hintImage.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            apiCall(query, category)
        })

    }

    // Handle the back button click event
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun apiCall(query: String, category: String) {
        RetrofitClient().getUserService()?.getNutritionInfo(query, "OY5GqRA9rgiY2XY432YRVg==VlbGgAMGMcAncyzS")
            ?.enqueue(object : retrofit2.Callback<NutritionResponse> {
                override fun onResponse(
                    call: Call<NutritionResponse>,
                    response: Response<NutritionResponse>
                ) {
                    if (response.isSuccessful) {
                        val nutritionResponse = response.body()
                        if (nutritionResponse != null) {
                            // Map the Item objects to NutritionFacts
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

                            // Update the adapter with the API response data
                            nutritionAdapter.updateData(nutritionList)

                            // Hide progress bar, show linear layout and RecyclerView
                            binding.progressBar.visibility = View.GONE
                            binding.linearLayout.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.VISIBLE

                        }
                    }
                }

                override fun onFailure(call: Call<NutritionResponse>, t: Throwable) {
                    // Handle API call failure

                    Toast.makeText(this@QueryNutritionActivity, "failed to resolve query", Toast.LENGTH_SHORT).show()
                }
            })
    }


}