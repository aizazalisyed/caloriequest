package com.java.caloriequest.Activities

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.java.caloriequest.API.NutritionalInfoRequest
import com.java.caloriequest.API.RetrofitClient
import com.java.caloriequest.R
import com.java.caloriequest.databinding.ActivityMainBinding
import com.java.caloriequest.model.ImageSegmentationResponse
import com.java.caloriequest.model.NutritionalInfoResponse
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
            sendImageToAPI(imageFile)
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

    private fun sendImageToAPI(imageFile: File) {
        // Use Retrofit to send the imageFile to your API for segmentation
        val imageRequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

        val userService = RetrofitClient().getUserService2() // Use your Retrofit service
        val authorizationHeader = "Bearer e97206646cbd2ff40afdc1c7d1c356a9448424fe" // Replace with your actual API token
        userService?.segmentImage(imagePart, authorizationHeader)?.enqueue(
            object : Callback<ImageSegmentationResponse> {
                override fun onResponse(
                    call: Call<ImageSegmentationResponse>,
                    response: Response<ImageSegmentationResponse>
                ) {
                    if (response.isSuccessful) {
                        val imageSegmentationResponse = response.body()
                        if (imageSegmentationResponse != null) {
                            val imageId = imageSegmentationResponse.imageId
                            // TODO: Get nutritional information based on the imageId
                            getNutritionalInfo(imageId)
                        } else {
                            // Handle the case where imageSegmentationResponse is null
                        }
                    } else {
                        // Handle API error
                    }
                }

                override fun onFailure(call: Call<ImageSegmentationResponse>, t: Throwable) {
                    // Handle API call failure
                }
            }
        )
    }

    private fun getNutritionalInfo(imageId: Int) {
        val userService = RetrofitClient().getUserService2() // Use your Retrofit service
        val nutritionalInfoRequest = NutritionalInfoRequest(imageId)

        val authorizationHeader = "Bearer e97206646cbd2ff40afdc1c7d1c356a9448424fe" // Replace with your actual API token

        userService?.getNutritionalInfo(nutritionalInfoRequest, authorizationHeader)?.enqueue(
            object : Callback<NutritionalInfoResponse> {
                override fun onResponse(
                    call: Call<NutritionalInfoResponse>,
                    response: Response<NutritionalInfoResponse>
                ) {
                    if (response.isSuccessful) {
                        val nutritionalInfoResponse = response.body()
                        if (nutritionalInfoResponse != null) {
                            // Handle the nutritional information response
                            println(nutritionalInfoResponse)
                        } else {
                            // Handle the case where nutritionalInfoResponse is null
                        }
                    } else {
                        // Handle API error
                    }
                }

                override fun onFailure(call: Call<NutritionalInfoResponse>, t: Throwable) {
                    // Handle API call failure
                }
            }
        )
    }

}