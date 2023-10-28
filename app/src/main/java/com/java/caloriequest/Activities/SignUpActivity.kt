    package com.java.caloriequest.Activities

    import android.app.Activity
    import android.content.Intent
    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import android.view.View
    import android.widget.Toast
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.ktx.userProfileChangeRequest
    import com.java.caloriequest.databinding.ActivitySignUpBinding

    class SignUpActivity : AppCompatActivity() {

        private lateinit var binding: ActivitySignUpBinding
        private lateinit var auth: FirebaseAuth

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivitySignUpBinding.inflate(layoutInflater)
            setContentView(binding.root)

            auth = FirebaseAuth.getInstance()

            binding.signUpButton.setOnClickListener {
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()
                val userName = binding.userNameEditText.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty() && userName.isNotEmpty()) {

                    // Show progress bar while signing up
                    binding.progressBar.visibility = View.VISIBLE

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            // Hide progress bar
                            binding.progressBar.visibility = View.GONE


                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null) {
                                    // Update user's display name
                                    val profileUpdates = userProfileChangeRequest {
                                        displayName = userName
                                    }
                                    user.updateProfile(profileUpdates)

                                    // Send email verification
                                    user.sendEmailVerification()

                                    // Show email verification message
                                    showToast("Verification link sent on email")
                                    switchActivity(LoginActivity::class.java)

                                }
                            } else {
                                // Handle registration failure
                                // For example, display an error message
                            }
                        }
                }else{
                    showToast("incomplete information")
                }
            }
            binding.loginTextView.setOnClickListener {
                // Start LoginActivity
                switchActivity(LoginActivity::class.java)
                finish()
            }

        }

        fun switchActivity(activity:  Class<out Activity>){
            val intent = Intent(this, activity)
            startActivity(intent)
        }

        fun showToast(message : String){
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }