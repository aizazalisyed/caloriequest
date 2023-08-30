package com.java.caloriequest.Activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityHeightBinding

class HeightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHeightBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeightBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        sharedPreferences = getSharedPreferences("CaloriesPrefs", MODE_PRIVATE)


        val feetPicker = binding.feetPicker
        val inchesPicker = binding.inchesPicker

        // Configure the range for the number pickers
        feetPicker.minValue = 1
        feetPicker.maxValue = 10 // Adjust as needed

        inchesPicker.minValue = 0
        inchesPicker.maxValue = 11 // 0 to 11 inches

        // Set up the "Next" button click listener using data binding
        binding.nextButton.setOnClickListener {
            val selectedFeet = feetPicker.value
            val selectedInches = inchesPicker.value
            saveHeightSelection(selectedFeet, selectedInches)
            switchActivity(WeightActivity::class.java)
        }
    }

    private fun saveHeightSelection(feet: Int, inches: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("selectedFeet", feet)
        editor.putInt("selectedInches", inches)
        editor.apply()
    }

    fun switchActivity(activity:  Class<out Activity>){

        val intent = Intent(this, activity)
        startActivity(intent)

    }
}
