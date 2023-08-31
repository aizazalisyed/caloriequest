package com.java.caloriequest.Activities

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityMainBinding

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
}