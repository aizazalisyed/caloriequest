package com.java.caloriequest.model



data class ImageSegmentationResponse(
    val foodFamily: List<FoodFamily>,
    val foodType: FoodType,
    val imageId: Int,
    val modelVersions: ModelVersions,
    val occasion: String,
    val occasionInfo: OccasionInfo,
    val processedImageSize: ProcessedImageSize,
    val segmentationResults: List<SegmentationResult>
)



data class FoodFamily(
    val id: Int,
    val name: String,
    val prob: Double
)

data class FoodType(
    val id: Int,
    val name: String
)

data class OccasionInfo(
    val id: Int?,
    val translation: String
)

data class ProcessedImageSize(
    val height: Int,
    val width: Int
)

data class FoodItemPosition(
    val x: Int,
    val y: Int
)

data class ContainedBbox(
    val h: Int,
    val w: Int,
    val x: Int,
    val y: Int
)

data class RecognitionResult(
    val foodFamily: List<FoodFamily>,
    val foodType: FoodType,
    val id: Int,
    val name: String,
    val prob: Double,
    val subclasses: List<RecognitionResult>
)

data class SegmentationResult(
    val center: FoodItemPosition,
    val containedBbox: ContainedBbox,
    val foodItemPosition: Int,
    val polygon: List<Int>,
    val recognitionResults: List<RecognitionResult>
)

data class ModelVersions(
    val drinks: String,
    val foodType: String,
    val foodgroups: String,
    val foodrec: String,
    val ingredients: String,
    val ontology: String,
    val segmentation: String
)
