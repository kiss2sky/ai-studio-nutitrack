package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.local.AppDatabase
import com.example.data.local.DefaultFoodDatabase
import com.example.data.local.DietaryAdviceEntity
import com.example.data.local.MealEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.WaterLogEntity
import com.example.data.model.DietaryAdvice
import com.example.data.model.FoodItem
import com.example.data.model.MealType
import com.example.data.model.NutritionAnalysisResult
import com.example.data.model.UserProfile
import com.example.data.remote.GeminiNutritionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import android.content.Context
import java.util.Date
import java.util.Locale

class NutritionRepository(
    private val database: AppDatabase,
    private val context: Context? = null,
    private val geminiService: GeminiNutritionService = GeminiNutritionService(context)
) {
    private val mealDao = database.mealDao()
    private val userProfileDao = database.userProfileDao()
    private val adviceDao = database.dietaryAdviceDao()
    private val waterLogDao = database.waterLogDao()

    fun setGeminiApiKey(key: String) = geminiService.setCustomApiKey(key)
    fun getGeminiApiKey(): String = geminiService.getStoredApiKey()
    fun isApiKeyConfigured(): Boolean = geminiService.isApiKeyConfigured()

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Meals Flow
    fun getMealsByDate(date: String): Flow<List<MealEntity>> {
        return mealDao.getMealsByDate(date)
    }

    fun getMealsBetweenDates(startDate: String, endDate: String): Flow<List<MealEntity>> {
        return mealDao.getMealsBetweenDates(startDate, endDate)
    }

    suspend fun logMeal(
        date: String,
        mealType: MealType,
        foodName: String,
        grams: Int,
        calories: Int,
        carbs: Float,
        protein: Float,
        fat: Float,
        fiber: Float = 0f,
        portionDesc: String = "",
        imageUri: String? = null,
        notes: String = "",
        aiRecognized: Boolean = false
    ): Long {
        val entity = MealEntity(
            date = date,
            mealType = mealType.name,
            foodName = foodName,
            grams = grams,
            calories = calories,
            carbs = carbs,
            protein = protein,
            fat = fat,
            fiber = fiber,
            portionDesc = portionDesc,
            imageUri = imageUri,
            notes = notes,
            aiRecognized = aiRecognized
        )
        return mealDao.insertMeal(entity)
    }

    suspend fun logMealItems(
        date: String,
        mealType: MealType,
        items: List<FoodItem>,
        imageUri: String? = null,
        notes: String = ""
    ) {
        val entities = items.map { item ->
            MealEntity(
                date = date,
                mealType = mealType.name,
                foodName = item.name,
                grams = item.grams,
                calories = item.calories,
                carbs = item.carbs,
                protein = item.protein,
                fat = item.fat,
                fiber = item.fiber,
                portionDesc = "${item.grams}g",
                imageUri = imageUri,
                notes = notes,
                aiRecognized = true
            )
        }
        mealDao.insertMeals(entities)
    }

    suspend fun deleteMealById(id: Long) {
        mealDao.deleteMealById(id)
    }

    // User Profile
    fun getUserProfile(): Flow<UserProfile> {
        return userProfileDao.getUserProfile().map { entity ->
            entity?.toDomain() ?: UserProfile()
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.insertOrUpdateProfile(UserProfileEntity.fromDomain(profile))
    }

    // Water Log
    fun getWaterLog(date: String): Flow<Int> {
        return waterLogDao.getWaterLog(date).map { it?.intakeMl ?: 0 }
    }

    suspend fun addWater(date: String, deltaMl: Int) {
        val current = waterLogDao.getWaterLog(date).firstOrNull()?.intakeMl ?: 0
        val newIntake = (current + deltaMl).coerceAtLeast(0)
        waterLogDao.saveWaterLog(WaterLogEntity(date = date, intakeMl = newIntake))
    }

    suspend fun setWater(date: String, intakeMl: Int) {
        waterLogDao.saveWaterLog(WaterLogEntity(date = date, intakeMl = intakeMl.coerceAtLeast(0)))
    }

    // AI Analysis & Dietary Advice
    suspend fun analyzeMealImage(bitmap: Bitmap, promptHint: String = ""): Result<NutritionAnalysisResult> {
        return geminiService.analyzeMealImage(bitmap, promptHint)
    }

    suspend fun analyzeMealText(foodText: String): Result<NutritionAnalysisResult> {
        return geminiService.analyzeMealText(foodText)
    }

    suspend fun generatePersonalizedAdvice(
        date: String,
        profile: UserProfile,
        consumedCalories: Int,
        consumedCarbs: Float,
        consumedProtein: Float,
        consumedFat: Float,
        consumedFiber: Float,
        loggedFoods: List<String>
    ): Result<DietaryAdvice> {
        val result = geminiService.generatePersonalizedAdvice(
            profile, consumedCalories, consumedCarbs, consumedProtein, consumedFat, consumedFiber, loggedFoods
        )
        if (result.isSuccess) {
            val advice = result.getOrNull()
            if (advice != null) {
                val adviceWithDate = advice.copy(date = date)
                adviceDao.insertAdvice(
                    DietaryAdviceEntity(
                        date = date,
                        healthScore = advice.healthScore,
                        summaryTitle = advice.summaryTitle,
                        goalAssessment = advice.goalAssessment,
                        mealBreakdownAdvice = advice.mealBreakdownAdvice,
                        nutrientGapsJson = advice.nutrientGaps.joinToString("||"),
                        actionableTipsJson = advice.actionableTips.joinToString("||"),
                        suggestedNextMeal = advice.suggestedNextMeal
                    )
                )
                return Result.success(adviceWithDate)
            }
        }
        return result
    }

    fun getStoredAdviceForDate(date: String): Flow<DietaryAdvice?> {
        return adviceDao.getAdviceForDate(date).map { entity ->
            entity?.let {
                DietaryAdvice(
                    id = it.id,
                    date = it.date,
                    healthScore = it.healthScore,
                    summaryTitle = it.summaryTitle,
                    goalAssessment = it.goalAssessment,
                    mealBreakdownAdvice = it.mealBreakdownAdvice,
                    nutrientGaps = if (it.nutrientGapsJson.isNotBlank()) it.nutrientGapsJson.split("||") else emptyList(),
                    actionableTips = if (it.actionableTipsJson.isNotBlank()) it.actionableTipsJson.split("||") else emptyList(),
                    suggestedNextMeal = it.suggestedNextMeal,
                    timestamp = it.timestamp
                )
            }
        }
    }

    suspend fun askNutritionist(question: String, profileContext: String): Result<String> {
        return geminiService.askNutritionist(question, profileContext)
    }

    // Food Database Search
    fun searchPresetFoods(query: String): List<FoodItem> {
        if (query.isBlank()) return DefaultFoodDatabase.presetFoods
        val q = query.lowercase().trim()
        return DefaultFoodDatabase.presetFoods.filter {
            it.name.lowercase().contains(q) || it.category.lowercase().contains(q)
        }
    }

    suspend fun initializeDefaultSampleDataIfEmpty() {
        val today = getTodayDateString()
        val meals = mealDao.getMealsByDate(today).firstOrNull()
        if (meals.isNullOrEmpty()) {
            // Populate sample breakfast and lunch (including the healthy meal dish from user's image)
            val sampleBreakfast = listOf(
                MealEntity(
                    date = today,
                    mealType = MealType.BREAKFAST.name,
                    foodName = "全谷物燕麦粥 (纯牛奶煮)",
                    grams = 200,
                    calories = 195,
                    carbs = 28.5f,
                    protein = 8.2f,
                    fat = 4.5f,
                    fiber = 4.2f,
                    portionDesc = "1碗",
                    notes = "加入新鲜蓝莓",
                    aiRecognized = false
                ),
                MealEntity(
                    date = today,
                    mealType = MealType.BREAKFAST.name,
                    foodName = "水煮鸡蛋",
                    grams = 55,
                    calories = 78,
                    carbs = 0.8f,
                    protein = 6.8f,
                    fat = 5.2f,
                    fiber = 0f,
                    portionDesc = "1颗",
                    notes = "全蛋",
                    aiRecognized = false
                )
            )

            // User healthy plate for lunch
            val sampleLunch = DefaultFoodDatabase.userPlateCombo.map { item ->
                MealEntity(
                    date = today,
                    mealType = MealType.LUNCH.name,
                    foodName = item.name,
                    grams = item.grams,
                    calories = item.calories,
                    carbs = item.carbs,
                    protein = item.protein,
                    fat = item.fat,
                    fiber = item.fiber,
                    portionDesc = "${item.grams}g",
                    notes = "高蛋白轻食减脂拼盘",
                    aiRecognized = true
                )
            }

            mealDao.insertMeals(sampleBreakfast + sampleLunch)
            waterLogDao.saveWaterLog(WaterLogEntity(date = today, intakeMl = 1250))
        }
    }
}
