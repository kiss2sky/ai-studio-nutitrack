package com.example.data.model

data class UserProfile(
    val id: Int = 1,
    val name: String = "健康践行者",
    val gender: Gender = Gender.FEMALE,
    val age: Int = 26,
    val heightCm: Float = 165f,
    val currentWeightKg: Float = 58.5f,
    val targetWeightKg: Float = 52.0f,
    val activityLevel: ActivityLevel = ActivityLevel.LIGHT,
    val goal: HealthGoal = HealthGoal.WEIGHT_LOSS,
    val customCalorieTarget: Int? = null,
    val customCarbsTarget: Float? = null,
    val customProteinTarget: Float? = null,
    val customFatTarget: Float? = null,
    val waterGoalMl: Int = 2000,
    val dietaryPreferences: String = "少油少盐, 高蛋白, 控糖",
    val allergies: String = "无"
) {
    // Calculate BMR using Mifflin-St Jeor Equation
    val bmr: Int
        get() {
            val base = (10 * currentWeightKg) + (6.25 * heightCm) - (5 * age)
            return if (gender == Gender.MALE) {
                (base + 5).toInt()
            } else {
                (base - 161).toInt()
            }
        }

    // Calculate TDEE (Total Daily Energy Expenditure)
    val tdee: Int
        get() = (bmr * activityLevel.factor).toInt()

    // Calculated Target Calories based on Goal
    val calculatedCalorieTarget: Int
        get() {
            if (customCalorieTarget != null && customCalorieTarget > 500) {
                return customCalorieTarget
            }
            val target = (tdee * (1f + goal.calorieAdjustmentRatio)).toInt()
            // Safe bounds
            return target.coerceIn(1200, 3500)
        }

    // Target Macros (Carbs 4 kcal/g, Protein 4 kcal/g, Fat 9 kcal/g)
    val calculatedCarbsGrams: Float
        get() {
            if (customCarbsTarget != null && customCarbsTarget > 0) return customCarbsTarget
            return (calculatedCalorieTarget * goal.carbsRatio / 4f)
        }

    val calculatedProteinGrams: Float
        get() {
            if (customProteinTarget != null && customProteinTarget > 0) return customProteinTarget
            return (calculatedCalorieTarget * goal.proteinRatio / 4f)
        }

    val calculatedFatGrams: Float
        get() {
            if (customFatTarget != null && customFatTarget > 0) return customFatTarget
            return (calculatedCalorieTarget * goal.fatRatio / 9f)
        }

    val calculatedFiberGrams: Float
        get() = 25f + (currentWeightKg * 0.1f) // Standard 25-30g

    val bmi: Float
        get() {
            val heightM = heightCm / 100f
            return if (heightM > 0) (currentWeightKg / (heightM * heightM) * 10).toInt() / 10f else 21f
        }

    val bmiCategory: String
        get() = when {
            bmi < 18.5f -> "偏瘦"
            bmi < 24.0f -> "正常健康"
            bmi < 28.0f -> "超重"
            else -> "肥胖"
        }
}

data class DietaryAdvice(
    val id: Long = 0,
    val date: String,
    val healthScore: Int,
    val summaryTitle: String,
    val goalAssessment: String,
    val mealBreakdownAdvice: String,
    val nutrientGaps: List<String>,
    val actionableTips: List<String>,
    val suggestedNextMeal: String,
    val timestamp: Long = System.currentTimeMillis()
)
