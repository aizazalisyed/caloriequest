package com.java.caloriequest.Activities

import NutritionDataAdapter
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.java.caloriequest.API.NutritionalInfoRequest
import com.java.caloriequest.API.RetrofitClient
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityMainBinding
import com.java.caloriequest.model.ImageSegmentationResponse
import com.java.caloriequest.model.NutritionItem
import com.java.caloriequest.model.TotalNutritionData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private var maintenanceCalories by Delegates.notNull<Int>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val CAMERA_REQUEST_CODE = 123
    private val CAMERA_PERMISSION_CODE = 101
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var nutritionAdapterBreakFast: NutritionDataAdapter
    private lateinit var nutritionAdapterLunch: NutritionDataAdapter
    private lateinit var nutritionAdapterDinner: NutritionDataAdapter
    var maxCalories : Int? = null
    lateinit var category : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        binding.sidenavigationicon.setOnClickListener {
            showAddExerciseDialog()
        }

        // Display the user's name and email in the navigation header
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            binding.userName.text = "Hi, ${user.displayName}"
        }

        sharedPreferences = getSharedPreferences("CaloriesPrefs", MODE_PRIVATE)
        val age = sharedPreferences.getInt("age", 0)
        val heightFeet = sharedPreferences.getInt("selectedFeet", 0)
        val heightInches = sharedPreferences.getInt("selectedInches", 0)
        val weight = sharedPreferences.getFloat("weight", 0f)
        val activityLevelFactor = sharedPreferences.getFloat("activityLevelFactor", 1.55f) // Default to moderate activity level

         maintenanceCalories = calculateMaintenanceCalories(age, heightFeet, heightInches, weight, activityLevelFactor)

        binding.calTextView.text = "/ $maintenanceCalories cal"

        binding.addBreakFastButton.setOnClickListener {
            showOptionDialog("BreakFast")
        }

        binding.addLunchButton.setOnClickListener {
            showOptionDialog("Lunch")
        }
        binding.addDinnerButton.setOnClickListener {
            showOptionDialog("Dinner")
        }

        binding.addWaterButton.setOnClickListener {
            showWaterIntakeDialog()
        }

        // Initialize RecyclerView and its adapter
        nutritionAdapterBreakFast = NutritionDataAdapter(emptyList())
        binding.breakFastRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.breakFastRecyclerView.adapter = nutritionAdapterBreakFast

        nutritionAdapterLunch = NutritionDataAdapter(emptyList())
        binding.lunchRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.lunchRecyclerView.adapter = nutritionAdapterLunch

        nutritionAdapterDinner = NutritionDataAdapter(emptyList())
        binding.dinnerRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.dinnerRecyclerView.adapter = nutritionAdapterDinner



        // Retrieve "BreakFast" data from Firestore
        val userUid = auth.currentUser!!.uid
        val userCollection = firestore.collection("users").document(userUid)
        val categoryCollectionBreakFast = userCollection.collection("BreakFast")
        val categoryCollectionLunch = userCollection.collection("Lunch")
        val categoryCollectionDinner = userCollection.collection("Dinner")

        categoryCollectionBreakFast.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                return@addSnapshotListener
            }

            if (snapshot != null) {

                binding.breakFastRecyclerView.visibility = View.VISIBLE
                val nutritionItems = mutableListOf<NutritionItem>()

                for (document in snapshot.documents) {
                    val nutritionData = document.toObject(NutritionItem::class.java)
                    nutritionData?.let { nutritionItems.add(it) }
                }

                // Calculate the sum of calories
                val totalCalories = nutritionItems.sumBy { it.calories.toInt() }

                // Set the total calories in breakfastCalTextView
                binding.breakfastCalTextView.text = "$totalCalories cal"

                // Update RecyclerView with retrieved data
                nutritionAdapterBreakFast.updateData(nutritionItems)
            }
        }

        categoryCollectionLunch.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                return@addSnapshotListener
            }

            if (snapshot != null) {

                binding.lunchRecyclerView.visibility = View.VISIBLE
                val nutritionItems = mutableListOf<NutritionItem>()

                for (document in snapshot.documents) {
                    val nutritionData = document.toObject(NutritionItem::class.java)
                    nutritionData?.let { nutritionItems.add(it) }
                }

                // Calculate the sum of calories
                val totalCalories = nutritionItems.sumBy { it.calories.toInt() }

                // Set the total calories in breakfastCalTextView
                binding.lunchCalTextView.text = "$totalCalories cal"

                // Update RecyclerView with retrieved data
                nutritionAdapterLunch.updateData(nutritionItems)
            }
        }

        categoryCollectionDinner.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Handle error
                return@addSnapshotListener
            }

            if (snapshot != null) {

                binding.dinnerRecyclerView.visibility = View.VISIBLE
                val nutritionItems = mutableListOf<NutritionItem>()

                for (document in snapshot.documents) {
                    val nutritionData = document.toObject(NutritionItem::class.java)
                    nutritionData?.let { nutritionItems.add(it) }
                }

                // Calculate the sum of calories
                val totalCalories = nutritionItems.sumBy { it.calories.toInt() }

                // Set the total calories in breakfastCalTextView
                binding.dinnerCalTextView.text = "$totalCalories cal"

                // Update RecyclerView with retrieved data
                nutritionAdapterDinner.updateData(nutritionItems)
            }
        }

        // Check if totalCalories is greater than maintenanceCalories

        retrieveTotalNutritionData()
    }

    private fun showAddExerciseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.add_exercise_dialog, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        val caloriesEditText = dialogView.findViewById<EditText>(R.id.caloriesEditText)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val caloriesText = caloriesEditText.text.toString()
            if (caloriesText.isNotEmpty()) {
                try {
                    val enteredCalories = caloriesText.toInt()
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val userUid = currentUser.uid
                        val userCollection = firestore.collection("users").document(userUid)

                        userCollection.get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val totalNutritionData = documentSnapshot.toObject(TotalNutritionData::class.java)
                                    if (totalNutritionData != null) {
                                        val currentCalories = totalNutritionData.totalCalories
                                        val updatedCalories = currentCalories - enteredCalories

                                        Log.d("upatedCalories", updatedCalories.toString())

                                        // Update the totalCalories in Firebase
                                        userCollection.update("totalCalories", updatedCalories)
                                            .addOnSuccessListener {
                                                Toast.makeText(this@MainActivity, "Calories updated successfully", Toast.LENGTH_SHORT).show()

                                                // Update the UI with the new totalCalories
                                                binding.cycleLengthCircularProgress.setProgress(updatedCalories, maxCalories!!.toDouble())
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this@MainActivity, "Failed to update calories", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            }
                    } else {
                        Toast.makeText(this, "You need to be logged in to update calories.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid input. Please enter a valid number.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter the calories value.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
    }

    private fun retrieveTotalNutritionData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            val userCollection = firestore.collection("users").document(userUid)

            // Add a snapshot listener to listen for changes in the document
            userCollection.addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    // Handle the error
                    Log.e("FirestoreListener", "Listen failed: $error")
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val totalNutritionData = documentSnapshot.toObject(TotalNutritionData::class.java)
                    if (totalNutritionData != null) {
                        // Update your UI with the new data
                        val totalCalories = totalNutritionData.totalCalories
                        val totalProtein = totalNutritionData.totalProtein
                        val totalFats = totalNutritionData.totalFats
                        val totalCarbs = totalNutritionData.totalCarbs
                        val cupsOfWater = totalNutritionData.waterIntake

                        binding.cycleLengthCircularProgress.setProgress(totalCalories, maxCalories!!.toDouble())
                        binding.protienTextView.text = totalProtein.toString() + " protein"
                        binding.fatsTextView.text = totalFats.toString() + " fats"
                        binding.carbsTextView.text = totalCarbs.toString() + " carbs"
                        binding.cupsTextView.text = cupsOfWater.toString() + " cups"
                        binding.waterCalTextView.text = cupsOfWater.toString() + " cups"

                        if (totalCalories > maintenanceCalories) {
                            // Create a warning message dialogue
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Warning")
                            builder.setMessage("You have exceeded your maintenance calories. Consider doing some workout to burn the excess calories.")

                            builder.setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }

                            val warningDialog = builder.create()
                            warningDialog.show()
                        }
                    }
                } else {
                    // Handle the case where the document doesn't exist
                }
            }
        } else {
            // User is not logged in, handle this case based on your requirements
        }
    }


    private fun showOptionDialog(category: String) {

        val dialogView = LayoutInflater.from(this).inflate(R.layout.option_selection_dialog, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        val cameraOption = dialogView.findViewById<TextView>(R.id.camera_option)
        val searchOption = dialogView.findViewById<TextView>(R.id.search_option)

        cameraOption.setOnClickListener {
            openCamera() // Open the camera when the "Camera" option is clicked
            dialog.dismiss()

            this.category = category
        }

        searchOption.setOnClickListener {
            val intent = Intent(this, QueryNutritionActivity::class.java)
            intent.putExtra("category", category) // Pass the category string
            startActivity(intent)
            finish()
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
            val waterIntakeText = waterIntakeEditText.text.toString()

            if (waterIntakeText.isEmpty()) {
                Toast.makeText(this, "Kindly Enter Number Of Glasses", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val waterIntake = waterIntakeText.toLong()
                    // Get the current user's UID
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val userUid = currentUser.uid
                        val userCollection = firestore.collection("users").document(userUid)

                        // Check if the document exists
                        userCollection.get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val documentSnapshot = task.result
                                    if (documentSnapshot != null && documentSnapshot.exists()) {
                                        // Document exists, update the water intake value
                                        val currentWaterIntake = documentSnapshot.getLong("waterIntake") ?: 0
                                        val newWaterIntake = currentWaterIntake + waterIntake

                                        // Update the document with the new water intake value
                                        userCollection.update("waterIntake", newWaterIntake)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Water intake updated successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                dialog.dismiss()

                                                // Update the TextViews with the new water intake value
                                                binding.cupsTextView.text = newWaterIntake.toString() + " cups"
                                                binding.waterCalTextView.text = newWaterIntake.toString() + " cups"
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Failed to update water intake",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    } else {
                                        // Document doesn't exist, create a new document
                                        val userData = hashMapOf(
                                            "waterIntake" to waterIntake
                                        )
                                        userCollection.set(userData)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Water intake saved successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                dialog.dismiss()

                                                // Update the TextViews with the new water intake value
                                                binding.cupsTextView.text = waterIntake.toString() + " cups"
                                                binding.waterCalTextView.text = waterIntake.toString() + " cups"
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Failed to save water intake",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "You need to be logged in to save water intake data.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid input. Please enter a valid number.", Toast.LENGTH_SHORT).show()
                }
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
        maxCalories = maintenanceCalories
        return maintenanceCalories
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            // Explain to the user why you need the camera permission (optional).
            AlertDialog.Builder(this)
                .setTitle("Camera Permission Needed")
                .setMessage("This app needs camera access to capture images.")
                .setPositiveButton("OK") { _, _ ->
                    requestPermissions(
                        arrayOf(android.Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_CODE
                    )
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        } else {
            // No explanation needed, request the permission.
            requestPermissions(
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera.
                openCamera()
            } else {
                // Permission denied, show a message or handle accordingly.
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        if (hasCameraPermission()) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Request camera permission.
            requestCameraPermission()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap // The captured image
            val imageFile = saveBitmapToFile(photo) // Convert Bitmap to File
            // TODO: Send the imageFile to the API for segmentation
            // Start ImageNutritionActivity and pass imageFile and category
            val intent = Intent(this, ImageNutritionActivity::class.java)
            intent.putExtra("imageFile", imageFile)
            intent.putExtra("category", category)
            startActivity(intent)
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val filesDir = applicationContext.filesDir
        val imageFile = File(filesDir, "captured_image.jpg")

        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        return imageFile
    }

}