package com.java.caloriequest.Activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.java.caloriequest.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                // Show progress bar while logging in
                binding.progressBar.visibility = View.VISIBLE

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        // Hide progress bar
                        binding.progressBar.visibility = View.GONE

                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                if (user.isEmailVerified) {
                                    // User is verified, proceed to the next activity
                                    // For example, start MainActivity
                                    // val intent = Intent(this, MainActivity::class.java)
                                    // startActivity(intent)
                                    switchActivity(MainActivity::class.java)
                                    finish()

                                } else {
                                    showToast("Please verify your email")
                                }
                            }
                        } else {
                            // Handle login failure
                            // For example, display an error message
                            showToast("Login failed. Check your credentials.")
                        }
                    }
            } else {
                showToast("Please fill in all the fields")
            }
        }

        binding.signUpTextView.setOnClickListener {
            switchActivity(GenderActivity::class.java)
        }



    }

    fun switchActivity(activity:  Class<out Activity>){

        val intent = Intent(this, activity)
        startActivity(intent)

    }

    private fun showToast(message: String) {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }
}