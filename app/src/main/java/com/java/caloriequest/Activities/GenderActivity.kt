package com.java.caloriequest.Activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityGenderBinding
import com.java.caloriequest.databinding.ActivityLoginBinding

class GenderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenderBinding
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("CaloriesPrefs", MODE_PRIVATE)

        binding.maleButton.setOnClickListener {
            onGenderButtonClick(true)
            switchActivity(HeightActivity::class.java)
        }
        binding.femaleButton.setOnClickListener {
            onGenderButtonClick(false)
            switchActivity(HeightActivity::class.java)
        }

    }

    fun onGenderButtonClick(isMale: Boolean) {
        val gender = if (isMale) "Male" else "Female"
        saveGenderSelection(gender)
    }

    private fun saveGenderSelection(gender: String) {
        val editor = sharedPreferences.edit()
        editor.putString("gender", gender)
        editor.apply()
    }
    fun switchActivity(activity:  Class<out Activity>){

        val intent = Intent(this, activity)
        startActivity(intent)

    }

}