package com.java.caloriequest.Activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityDilyLevelBinding
import com.java.caloriequest.databinding.ActivityWeightBinding

class WeightActivity : AppCompatActivity() {

    private lateinit var binding : ActivityWeightBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("CaloriesPrefs", MODE_PRIVATE)

        binding.nextButton.setOnClickListener {
            val weightText = binding.weightEditText.text.toString()
            if (weightText.isNotEmpty()) {
                val weight = weightText.toFloat()
                saveWeight(weight)

                switchActivity(DOBActivity::class.java)

            } else {
                Toast.makeText(this, "Empty Entery", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun saveWeight(weight: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat("weight", weight)
        editor.apply()
    }
    fun switchActivity(activity:  Class<out Activity>){

        val intent = Intent(this, activity)
        startActivity(intent)

    }

}