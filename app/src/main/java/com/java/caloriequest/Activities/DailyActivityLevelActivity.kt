package com.java.caloriequest.Activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityDilyLevelBinding
import com.java.caloriequest.databinding.ActivityDilyLevelBinding.inflate
import com.java.caloriequest.databinding.ActivityMainBinding

class DailyActivityLevelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDilyLevelBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDilyLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("CaloriesPrefs", MODE_PRIVATE)

        binding.sedentaryButton.setOnClickListener {
            saveActivityLevelFactor(1.2)
            switchActivity()
        }

        binding.lightlyActiveButton.setOnClickListener {
            saveActivityLevelFactor(1.375)
            switchActivity()
        }

        binding.moderatelyActiveButton.setOnClickListener {
            saveActivityLevelFactor(1.55)
            switchActivity()
        }

        binding.veryActiveActiveButton.setOnClickListener {
            saveActivityLevelFactor(1.725)
            switchActivity()
        }

        binding.suparActiveActiveButton.setOnClickListener {
            saveActivityLevelFactor(1.9)
            switchActivity()
        }

    }
    private fun saveActivityLevelFactor(factor: Double) {
        val editor = sharedPreferences.edit()
        editor.putFloat("activityLevelFactor", factor.toFloat())
        editor.apply()
    }

    fun switchActivity(){

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

    }
}