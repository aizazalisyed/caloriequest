package com.java.caloriequest.model

data class FoodInfo(
    val foodName: List<String>,
    val hasNutritionalInfo: Boolean,
    val ids: List<Int>,
    val imageId: Int,
    val nutritional_info: NutritionalInfo,
    val nutritionalInfoPerItem: List<NutritionalInfoPerItem>,
    val serving_size : Double
)

data class NutritionalInfo(
    val calories: Double,
    val dailyIntakeReference: DailyIntakeReference,
    val totalNutrients: TotalNutrients
)

data class DailyIntakeReference(
    val CHOCDF: NutritionInfo,
    val ENERC_KCAL: NutritionInfo,
    val FASAT: NutritionInfo,
    val FAT: NutritionInfo,
    val NA: NutritionInfo,
    val PROCNT: NutritionInfo,
    val SUGAR: NutritionInfo,
    val SUGAR_added: NutritionInfo
)

data class NutritionInfo(
    val label: String,
    val level: String,
    val percent: Double
)

data class TotalNutrients(
    val CA: NutrientInfo,
    val CHOCDF: NutrientInfo,
    val CHOLE: NutrientInfo,
    val ENERC_KCAL: NutrientInfo,
    val F18D3CN3: NutrientInfo,
    val F20D5: NutrientInfo,
    val F22D6: NutrientInfo,
    val FAMS: NutrientInfo,
    val FAPU: NutrientInfo,
    val FASAT: NutrientInfo,
    val FAT: NutrientInfo,
    val FATRN: NutrientInfo,
    val FE: NutrientInfo,
    val FIBTG: NutrientInfo,
    val FOLAC: NutrientInfo,
    val FOLDFE: NutrientInfo,
    val FOLFD: NutrientInfo,
    val K: NutrientInfo,
    val MG: NutrientInfo,
    val NA: NutrientInfo,
    val NIA: NutrientInfo,
    val P: NutrientInfo,
    val PROCNT: NutrientInfo,
    val RIBF: NutrientInfo,
    val SUGAR: NutrientInfo,
    val SUGAR_added: NutrientInfo,
    val THIA: NutrientInfo,
    val TOCPHA: NutrientInfo,
    val VITA_RAE: NutrientInfo,
    val VITB12: NutrientInfo,
    val VITB6A: NutrientInfo,
    val VITC: NutrientInfo,
    val VITD: NutrientInfo,
    val VITK1: NutrientInfo,
    val ZN: NutrientInfo
)

data class NutrientInfo(
    val label: String,
    val quantity: Double,
    val unit: String
)

data class NutritionalInfoPerItem(
    val foodItemPosition: Int,
    val hasNutritionalInfo: Boolean,
    val id: Int,
    val nutritionalInfo: NutritionalInfo,
    val servingSize: Double
)
