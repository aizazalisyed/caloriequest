package com.java.caloriequest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.java.caloriequest.Activities.LoginActivity
import com.java.caloriequest.Activities.MainActivity
import com.java.caloriequest.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the UI
        initUI()
    }

    private fun initUI() {
        // Set current user's email and username in EditText fields
        binding.emailEditText.setText(user?.email)
        binding.userNameEditText.setText(user?.displayName)

        // Handle Save button click
        binding.saveButton.setOnClickListener {

            val newEmail = binding.emailEditText.text.toString()
            val newUsername = binding.userNameEditText.text.toString()
            val oldPassword = binding.passwordEditText.text.toString()
            val newPassword = binding.newPasswordEditText.text.toString()

            // Check if the new email is different from the current email
            if (newEmail != user?.email) {
                // Change email and send verification link
                user?.updateEmail(newEmail)?.addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        // Email updated successfully
                        user.sendEmailVerification().addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                // Email verification link sent successfully
                                showMessage("Email updated. Verification link sent to the new email.")
                                startActivity(Intent(this,LoginActivity::class.java))
                                finish()

                            } else {
                                showError("Error sending email verification: ${verificationTask.exception?.message}")
                            }
                        }
                    } else {
                        showError("Error updating email: ${emailTask.exception?.message}")
                    }
                }
            }

            // Check if the new username is different from the current username
            if (newUsername != user?.displayName) {
                // Update the username
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build()

                user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                    if (profileTask.isSuccessful) {
                        // Username updated successfully
                        showMessage("Username updated.")
                        startActivity(Intent(this,MainActivity::class.java))
                        finish()
                    } else {
                        showError("Error updating username: ${profileTask.exception?.message}")
                    }
                }
            }

            // Check if the old password is provided and if the new password is different
            if (!oldPassword.isEmpty() && newPassword != oldPassword) {
                // Re-authenticate the user to update the password
                val credential = user?.email?.let { email ->
                    EmailAuthProvider.getCredential(email, oldPassword)
                }

                if (credential != null) {
                    user?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            // Re-authentication successful, update the password
                            user.updatePassword(newPassword).addOnCompleteListener { passwordTask ->
                                if (passwordTask.isSuccessful) {
                                    // Password updated successfully
                                    showMessage("Password updated.")
                                    startActivity(Intent(this,LoginActivity::class.java))
                                    finish()
                                } else {
                                    showError("Error updating password: ${passwordTask.exception?.message}")
                                }
                            }
                        } else {
                            showError("Incorrect old password. Please enter the correct old password.")
                        }
                    }
                } else {
                    showError("Error creating re-authentication credential.")
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }
}
