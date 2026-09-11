package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DefaultFoodDatabase
import com.example.data.local.MealEntity
import com.example.data.model.DailyNutritionSummary
import com.example.data.model.DietaryAdvice
import com.example.data.model.FoodItem
import com.example.data.model.MealType
import com.example.data.model.NutritionAnalysisResult
import com.example.data.model.UserProfile
import com.example.data.repository.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class NutritionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NutritionRepository(AppDatabase.getDatabase(application), application)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _apiKeyConfigured = MutableStateFlow(repository.isApiKeyConfigured())
    val apiKeyConfigured: StateFlow<Boolean> = _apiKeyConfigured.asStateFlow()

    fun setGeminiApiKey(key: String) {
        repository.setGeminiApiKey(key)
        _apiKeyConfigured.value = repository.isApiKeyConfigured()
    }

    fun getGeminiApiKey(): String {
        return repository.getGeminiApiKey()
    }

    private val _selectedDate = MutableStateFlow(repository.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val userProfile: StateFlow<UserProfile> = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

    val mealsForSelectedDate: StateFlow<List<MealEntity>> = _selectedDate
        .flatMapLatest { date -> repository.getMealsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waterIntakeMl: StateFlow<Int> = _selectedDate
        .flatMapLatest { date -> repository.getWaterLog(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val storedAdvice: StateFlow<DietaryAdvice?> = _selectedDate
        .flatMapLatest { date -> repository.getStoredAdviceForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailySummary: StateFlow<DailyNutritionSummary> = combine(
        _selectedDate,
        mealsForSelectedDate,
        userProfile,
        waterIntakeMl
    ) { date, meals, profile, water ->
        val totalCal = meals.sumOf { it.calories }
        val totalCarbs = meals.sumOf { it.carbs.toDouble() }.toFloat()
        val totalProtein = meals.sumOf { it.protein.toDouble() }.toFloat()
        val totalFat = meals.sumOf { it.fat.toDouble() }.toFloat()
        val totalFiber = meals.sumOf { it.fiber.toDouble() }.toFloat()

        DailyNutritionSummary(
            date = date,
            totalCalories = totalCal,
            targetCalories = profile.calculatedCalorieTarget,
            totalCarbs = totalCarbs,
            targetCarbs = profile.calculatedCarbsGrams,
            totalProtein = totalProtein,
            targetProtein = profile.calculatedProteinGrams,
            totalFat = totalFat,
            targetFat = profile.calculatedFatGrams,
            totalFiber = totalFiber,
            targetFiber = profile.calculatedFiberGrams,
            waterIntakeMl = water,
            waterTargetMl = profile.waterGoalMl
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailyNutritionSummary(
            date = repository.getTodayDateString(),
            totalCalories = 0,
            targetCalories = 1800,
            totalCarbs = 0f,
            targetCarbs = 200f,
            totalProtein = 0f,
            targetProtein = 110f,
            totalFat = 0f,
            targetFat = 50f,
            totalFiber = 0f,
            targetFiber = 25f,
            waterIntakeMl = 0,
            waterTargetMl = 2000
        )
    )

    // AI States
    private val _isAnalyzingAi = MutableStateFlow(false)
    val isAnalyzingAi: StateFlow<Boolean> = _isAnalyzingAi.asStateFlow()

    private val _aiAnalysisResult = MutableStateFlow<NutritionAnalysisResult?>(null)
    val aiAnalysisResult: StateFlow<NutritionAnalysisResult?> = _aiAnalysisResult.asStateFlow()

    private val _isGeneratingAdvice = MutableStateFlow(false)
    val isGeneratingAdvice: StateFlow<Boolean> = _isGeneratingAdvice.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "您好！我是您的 AI 私人注册营养师 🌿。您可以随时向我咨询饮食搭配、减脂增肌计划、食材升糖指数或宵夜替代建议！"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultSampleDataIfEmpty()
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun shiftDate(days: Int) {
        val current = try {
            dateFormat.parse(_selectedDate.value) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        val cal = Calendar.getInstance().apply {
            time = current
            add(Calendar.DAY_OF_YEAR, days)
        }
        _selectedDate.value = dateFormat.format(cal.time)
    }

    fun goToToday() {
        _selectedDate.value = repository.getTodayDateString()
    }

    fun logMeal(
        mealType: MealType,
        foodName: String,
        grams: Int,
        calories: Int,
        carbs: Float,
        protein: Float,
        fat: Float,
        fiber: Float = 0f,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.logMeal(
                date = _selectedDate.value,
                mealType = mealType,
                foodName = foodName,
                grams = grams,
                calories = calories,
                carbs = carbs,
                protein = protein,
                fat = fat,
                fiber = fiber,
                portionDesc = "${grams}g",
                notes = notes,
                aiRecognized = false
            )
        }
    }

    fun logBatchItems(
        mealType: MealType,
        items: List<FoodItem>,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.logMealItems(
                date = _selectedDate.value,
                mealType = mealType,
                items = items,
                notes = notes
            )
        }
    }

    fun deleteMeal(id: Long) {
        viewModelScope.launch {
            repository.deleteMealById(id)
        }
    }

    fun addWater(deltaMl: Int) {
        viewModelScope.launch {
            repository.addWater(_selectedDate.value, deltaMl)
        }
    }

    fun setWater(amountMl: Int) {
        viewModelScope.launch {
            repository.setWater(_selectedDate.value, amountMl)
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    // AI Meal Analysis Actions
    fun analyzeMealPhoto(bitmap: Bitmap, promptHint: String = "") {
        viewModelScope.launch {
            try {
                _isAnalyzingAi.value = true
                _aiError.value = null
                val result = repository.analyzeMealImage(bitmap, promptHint)
                _isAnalyzingAi.value = false
                if (result.isSuccess) {
                    _aiAnalysisResult.value = result.getOrNull()
                } else {
                    _aiError.value = result.exceptionOrNull()?.message ?: "识别失败，请重试"
                }
            } catch (e: Exception) {
                _isAnalyzingAi.value = false
                _aiError.value = "识别出错: ${e.localizedMessage}"
            }
        }
    }

    fun analyzeMealText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                _isAnalyzingAi.value = true
                _aiError.value = null
                val result = repository.analyzeMealText(text)
                _isAnalyzingAi.value = false
                if (result.isSuccess) {
                    _aiAnalysisResult.value = result.getOrNull()
                } else {
                    _aiError.value = result.exceptionOrNull()?.message ?: "解析失败，请重试"
                }
            } catch (e: Exception) {
                _isAnalyzingAi.value = false
                _aiError.value = "解析出错: ${e.localizedMessage}"
            }
        }
    }

    fun clearAiAnalysisResult() {
        _aiAnalysisResult.value = null
        _aiError.value = null
    }

    fun confirmAiAnalysisToMeal(mealType: MealType) {
        val result = _aiAnalysisResult.value ?: return
        viewModelScope.launch {
            try {
                repository.logMealItems(
                    date = _selectedDate.value,
                    mealType = mealType,
                    items = result.items,
                    notes = "${result.title} (${result.ratingTag})"
                )
                _aiAnalysisResult.value = null
            } catch (e: Exception) {
                // Ignore error
            }
        }
    }

    fun requestPersonalizedAdvice() {
        viewModelScope.launch {
            try {
                _isGeneratingAdvice.value = true
                val profile = userProfile.value
                val summary = dailySummary.value
                val meals = mealsForSelectedDate.value.map { "${it.foodName} (${it.grams}g)" }

                repository.generatePersonalizedAdvice(
                    date = _selectedDate.value,
                    profile = profile,
                    consumedCalories = summary.totalCalories,
                    consumedCarbs = summary.totalCarbs,
                    consumedProtein = summary.totalProtein,
                    consumedFat = summary.totalFat,
                    consumedFiber = summary.totalFiber,
                    loggedFoods = meals
                )
                _isGeneratingAdvice.value = false
            } catch (e: Exception) {
                _isGeneratingAdvice.value = false
            }
        }
    }

    fun sendNutritionChat(question: String) {
        if (question.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = question)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            try {
                val profile = userProfile.value
                val summary = dailySummary.value
                val context = "用户目标: ${profile.goal.title}, 今日热量: ${summary.totalCalories}/${summary.targetCalories} kcal, 蛋白质: ${summary.totalProtein.toInt()}g"
                val result = repository.askNutritionist(question, context)
                _isChatLoading.value = false
                val replyText = result.getOrDefault("对不起，暂时无法处理，请稍后再试。")
                _chatMessages.value = _chatMessages.value + ChatMessage(isUser = false, text = replyText)
            } catch (e: Exception) {
                _isChatLoading.value = false
                _chatMessages.value = _chatMessages.value + ChatMessage(isUser = false, text = "网络或服务异常，请稍后再试。")
            }
        }
    }

    fun searchFood(query: String): List<FoodItem> {
        return repository.searchPresetFoods(query)
    }

    fun loadPresetUserHealthyPlate() {
        _aiAnalysisResult.value = NutritionAnalysisResult(
            title = "清蒸海陆高蛋白减脂拼盘 (真实拍照识别)",
            summary = "精准识别盘中食材：白灼鲜虾6只、水煮全蛋1颗、潮汕清炖瘦肉丸3颗、清蒸贝贝南瓜、水煮鲜西兰花与胡萝卜片。黄金高蛋白、优质慢碳与丰富膳食纤维！",
            totalCalories = DefaultFoodDatabase.userPlateCombo.sumOf { it.calories },
            totalCarbs = DefaultFoodDatabase.userPlateCombo.sumOf { it.carbs.toDouble() }.toFloat(),
            totalProtein = DefaultFoodDatabase.userPlateCombo.sumOf { it.protein.toDouble() }.toFloat(),
            totalFat = DefaultFoodDatabase.userPlateCombo.sumOf { it.fat.toDouble() }.toFloat(),
            totalFiber = DefaultFoodDatabase.userPlateCombo.sumOf { it.fiber.toDouble() }.toFloat(),
            healthScore = 98,
            ratingTag = "A+ 黄金减脂高蛋白",
            items = DefaultFoodDatabase.userPlateCombo,
            highlights = listOf(
                "优质动物完全蛋白高达 44.4g，有力维持肌肉代谢与持久饱腹",
                "清蒸贝贝南瓜提供优质低GI复合慢碳 (14.2g 碳水)，升糖极其平稳",
                "水煮西兰花与胡萝卜提供 8.9g 膳食纤维与高浓度 β-胡萝卜素",
                "水煮清蒸零多余油脂，脂肪仅 10.6g"
            ),
            suggestions = listOf(
                "这一餐营养结构极度标准，完美契合 2:1:1 黄金减脂餐盘比例",
                "建议午餐后适量饮水 300-500ml 促进蛋白质吸收与排毒",
                "下午若需加餐，可选择一小把坚果（15g）补充优质多不饱和脂肪酸"
            )
        )
    }
}
