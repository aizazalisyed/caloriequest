package com.java.caloriequest.Activities

import android.app.AlertDialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        sharedPreferences = getSharedPreferences("CaloriesPrefs", MODE_PRIVATE)
        val age = sharedPreferences.getInt("age", 0)
        val heightFeet = sharedPreferences.getInt("selectedFeet", 0)
        val heightInches = sharedPreferences.getInt("selectedInches", 0)
        val weight = sharedPreferences.getFloat("weight", 0f)
        val activityLevelFactor = sharedPreferences.getFloat("activityLevelFactor", 1.55f) // Default to moderate activity level

        val maintenanceCalories = calculateMaintenanceCalories(age, heightFeet, heightInches, weight, activityLevelFactor)

        binding.calTextView.text = "/ $maintenanceCalories cal"

        binding.addBreakFastButton.setOnClickListener {
            showOptionDialog()
        }

        binding.addLunchButton.setOnClickListener {
            showOptionDialog()
        }
        binding.addDinnerButton.setOnClickListener {
            showOptionDialog()
        }

        binding.addWaterButton.setOnClickListener {
            showWaterIntakeDialog()
        }

    }


    private fun showOptionDialog() {

        val dialogView = LayoutInflater.from(this).inflate(R.layout.option_selection_dialog, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        val cameraOption = dialogView.findViewById<TextView>(R.id.camera_option)
        val searchOption = dialogView.findViewById<TextView>(R.id.search_option)

        cameraOption.setOnClickListener {
            dialog.dismiss()
        }

        searchOption.setOnClickListener {

            apiCall()
            dialog.dismiss()
        }
    }


    private fun showWaterIntakeDialog() {

        val dialogView = LayoutInflater.from(this).inflate(R.layout.water_intake_dialogue, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        val waterIntakeEditText = dialogView.findViewById<EditText>(R.id.waterIntakeEditText)
        val confirm_button = dialogView.findViewById<Button>(R.id.confirm_button)



       confirm_button.setOnClickListener {

           if (waterIntakeEditText.text.isEmpty()){
               Toast.makeText(this, "Kindly Enter Number Of Glasses", Toast.LENGTH_SHORT).show()
           }
           else{
               dialog.dismiss()
           }

        }
    }


    private fun calculateMaintenanceCalories(age: Int, heightFeet: Int, heightInches: Int, weight: Float, activityLevelFactor: Float): Int {
        val totalInches = heightFeet * 12 + heightInches
        val bmr: Double

        if (age < 18) {
            bmr = 88.362 + (13.397 * weight) + (4.799 * totalInches) - (5.677 * age)
        } else {
            bmr = 88.362 + (13.397 * weight) + (4.799 * totalInches) - (5.677 * age)
        }

        val maintenanceCalories = (bmr * activityLevelFactor).toInt()
        return maintenanceCalories
    }

    fun apiCall(){
        RetrofitClient().getUserService()?.getNutritionInfo("10oz onion and a tomato","OY5GqRA9rgiY2XY432YRVg==VlbGgAMGMcAncyzS")
            ?.enqueue(
                object  : retrofit2.Callback<NutritionResponse>{
                    override fun onResponse(
                        call: Call<NutritionResponse>,
                        response: Response<NutritionResponse>,
                    ) {
                        if(response.isSuccessful){

                        }
                    }

                    override fun onFailure(call: Call<NutritionResponse>, t: Throwable) {

                    }
                })
    }

}