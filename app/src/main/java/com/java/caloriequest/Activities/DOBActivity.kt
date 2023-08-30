package com.java.caloriequest.Activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.java.caloriequest.databinding.ActivityDobactivityBinding
import java.util.*

class DOBActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDobactivityBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDobactivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        sharedPreferences = getSharedPreferences("CaloriesPrefs", MODE_PRIVATE)

        val datePicker = binding.datePicker
        val nextButton = binding.nextButton

        nextButton.setOnClickListener {
            val selectedDate = Calendar.getInstance()
            selectedDate.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)

            val currentDate = Calendar.getInstance()
            val age = currentDate.get(Calendar.YEAR) - selectedDate.get(Calendar.YEAR)

            saveAge(age)

            switchActivity(DailyActivityLevelActivity::class.java)
        }

    }

    fun switchActivity(activity:  Class<out Activity>){

        val intent = Intent(this, activity)
        startActivity(intent)

    }

    private fun saveAge(age: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("age", age)
        editor.apply()
    }
}