package com.java.caloriequest.Activities

import NutritionDataAdapter
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.java.caloriequest.API.NutritionalInfoRequest
import com.java.caloriequest.API.RetrofitClient
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityMainBinding
import com.java.caloriequest.model.ImageSegmentationResponse
import com.java.caloriequest.model.NutritionItem
import com.java.caloriequest.model.NutritionalInfoResponse
import com.java.caloriequest.model.TotalNutritionData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

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

        sharedPreferences = getSharedPreferences("CaloriesPrefs", MODE_PRIVATE)
        val age = sharedPreferences.getInt("age", 0)
        val heightFeet = sharedPreferences.getInt("selectedFeet", 0)
        val heightInches = sharedPreferences.getInt("selectedInches", 0)
        val weight = sharedPreferences.getFloat("weight", 0f)
        val activityLevelFactor = sharedPreferences.getFloat("activityLevelFactor", 1.55f) // Default to moderate activity level

        val maintenanceCalories = calculateMaintenanceCalories(age, heightFeet, heightInches, weight, activityLevelFactor)

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

        retrieveTotalNutritionData()
    }

    private fun retrieveTotalNutritionData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            val userCollection = firestore.collection("users").document(userUid)

            // Fetch the existing total nutrition data from Firestore
            userCollection.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val totalNutritionData = documentSnapshot.toObject(TotalNutritionData::class.java)
                        if (totalNutritionData != null) {
                            // Here, you can use the totalNutritionData as needed
                            // For example, you can access its properties like this:
                            val totalCalories = totalNutritionData.totalCalories
                            val totalProtein = totalNutritionData.totalProtein
                            val totalFats = totalNutritionData.totalFats
                            val totalCarbs = totalNutritionData.totalCarbs

                            binding.cycleLengthCircularProgress.setProgress(totalCalories, maxCalories!!.toDouble())
                            binding.protienTextView.text = totalProtein.toString()
                            binding.fatsTextView.text = totalFats.toString()
                            binding.carbsTextView.text = totalCarbs.toString()

                            // Update your UI or perform any other actions with the data
                        } else {
                            // Handle the case where totalNutritionData is null
                        }
                    } else {
                        // Document doesn't exist, handle this case based on your requirements
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure to fetch data
                    // You can add a Toast message or other error handling here
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

           if (waterIntakeEditText.text.isEmpty()){
               Toast.makeText(this, "Kindly Enter Number Of Glasses", Toast.LENGTH_SHORT).show()
           }
           else{
               dialog.dismiss()
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