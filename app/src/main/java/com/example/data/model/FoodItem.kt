package com.example.data.model

data class FoodItem(
    val id: String = "",
    val name: String,
    val grams: Int = 100,
    val calories: Int,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val category: String = "主食",
    val portionUnit: String = "g",
    val defaultServingGrams: Int = 100,
    val giLevel: String = "低GI", // 低GI, 中GI, 高GI
    val tip: String = ""
) {
    fun calculateForGrams(targetGrams: Int): FoodItem {
        val factor = targetGrams.toFloat() / grams.coerceAtLeast(1)
        return copy(
            grams = targetGrams,
            calories = (calories * factor).toInt(),
            carbs = (carbs * factor * 10).toInt() / 10f,
            protein = (protein * factor * 10).toInt() / 10f,
            fat = (fat * factor * 10).toInt() / 10f,
            fiber = (fiber * factor * 10).toInt() / 10f
        )
    }
}

data class NutritionAnalysisResult(
    val title: String,
    val summary: String,
    val totalCalories: Int,
    val totalCarbs: Float,
    val totalProtein: Float,
    val totalFat: Float,
    val totalFiber: Float,
    val healthScore: Int, // 0 - 100
    val ratingTag: String, // e.g. "A+ 黄金减脂高蛋白", "低GI营养餐"
    val items: List<FoodItem>,
    val highlights: List<String>,
    val suggestions: List<String>,
    val rawAiText: String = ""
)

data class DailyNutritionSummary(
    val date: String,
    val totalCalories: Int,
    val targetCalories: Int,
    val totalCarbs: Float,
    val targetCarbs: Float,
    val totalProtein: Float,
    val targetProtein: Float,
    val totalFat: Float,
    val targetFat: Float,
    val totalFiber: Float,
    val targetFiber: Float,
    val waterIntakeMl: Int,
    val waterTargetMl: Int,
    val burnedCalories: Int = 0
)
