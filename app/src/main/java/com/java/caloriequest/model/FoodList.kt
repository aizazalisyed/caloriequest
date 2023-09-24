package com.java.caloriequest.model

data class FoodList(val foodItemsWithNutritionFacts : List<NutritionFacts>,
                    val category : String,
                    val timeStamp : String
)
