package com.java.caloriequest.model

data class NutritionalInfoResponse(
    val foodName: List<String>,
    val hasNutritionalInfo: Boolean,
    val ids: List<Int>,
    val imageId: Int,
    val nutritional_info: NutritionalInfo,
    val nutritional_info_per_item: List<NutritionalInfoPerItem>,
    val serving_size: Double
)

data class NutritionalInfo(
    val calories: Double,
    val dailyIntakeReference: Map<String, NutritionalValue>,
    val totalNutrients: Map<String, NutritionalValue>
)

data class NutritionalValue(
    val label: String,
    val level: String,
    val percent: Double
)

data class NutritionalInfoPerItem(
    val food_item_position: Int,
    val hasNutritionalInfo: Boolean,
    val id: Int,
    val nutritional_info: NutritionalInfo,
    val serving_size: Double
)
