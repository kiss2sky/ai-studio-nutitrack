package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FoodItem
import com.example.data.model.MealType
import com.example.ui.components.*
import com.example.ui.dialogs.AddFoodManualDialog
import com.example.ui.dialogs.AiMealScanDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.NutritionViewModel

@Composable
fun HomeScreen(
    viewModel: NutritionViewModel,
    onNavigateToAdvice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val meals by viewModel.mealsForSelectedDate.collectAsStateWithLifecycle()
    val waterIntake by viewModel.waterIntakeMl.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val isAnalyzingAi by viewModel.isAnalyzingAi.collectAsStateWithLifecycle()
    val aiAnalysisResult by viewModel.aiAnalysisResult.collectAsStateWithLifecycle()

    var showAiScanDialog by remember { mutableStateOf(false) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var activeMealTypeForManual by remember { mutableStateOf(MealType.LUNCH) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAiScanDialog = true },
                icon = { Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null) },
                text = { Text("AI 拍照/智能识别", fontWeight = FontWeight.Bold) },
                containerColor = GeoPrimaryLight,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("fab_ai_scan")
            )
        },
        containerColor = GeoBackgroundLight,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
        ) {
            // Geometric Balance Top Greeting Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedDate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = GeoMutedText,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "你好, ${userProfile.name}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoDarkText
                            )
                        )
                    }

                    // Avatar Circle
                    val initials = if (userProfile.name.isNotBlank()) {
                        userProfile.name.take(2).uppercase()
                    } else "ME"

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GeoSecondaryContainerLight)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoOnSecondaryContainerLight
                            )
                        )
                    }
                }
            }

            // Date Selector Bar
            item {
                DateSelectorBar(
                    selectedDate = selectedDate,
                    onPreviousDay = { viewModel.shiftDate(-1) },
                    onNextDay = { viewModel.shiftDate(1) },
                    onTodayClick = { viewModel.goToToday() }
                )
            }

            // Geometric Hero Calorie & Macro Card
            item {
                CalorieHeroCard(summary = dailySummary)
            }

            // Geometric AI Personalized Advice Quick Card
            item {
                Card(
                    onClick = onNavigateToAdvice,
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, GeoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_quick_ai_advice")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(GeoTertiaryContainerLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "💡",
                                fontSize = 22.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "个性化饮食建议",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GeoDarkText
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "AI 营养师已根据您的热量及宏量配比生成调控策略",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GeoOnSurfaceVariantLight,
                                    lineHeight = 16.sp
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GeoPrimaryContainerLight
                        ) {
                            Text(
                                text = "查看",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GeoOnPrimaryContainerLight
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Section Header: 今日饮食
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日饮食",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeoDarkText
                        )
                    )
                    Text(
                        text = "4 餐配置",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = GeoMutedText
                        )
                    )
                }
            }

            // Meal Types: Breakfast, Lunch, Dinner, Snack
            MealType.values().forEach { mealType ->
                item {
                    val mealsForType = meals.filter { it.mealType == mealType.name }
                    MealTypeCard(
                        mealType = mealType,
                        meals = mealsForType,
                        onAddClick = {
                            activeMealTypeForManual = mealType
                            showManualAddDialog = true
                        },
                        onAiScanClick = {
                            activeMealTypeForManual = mealType
                            showAiScanDialog = true
                        },
                        onDeleteMeal = { id ->
                            viewModel.deleteMeal(id)
                        }
                    )
                }
            }

            // Hydration Tracker Card
            item {
                WaterTrackerCard(
                    currentMl = waterIntake,
                    targetMl = userProfile.waterGoalMl,
                    onAddWater = { delta -> viewModel.addWater(delta) },
                    onResetWater = { viewModel.setWater(0) }
                )
            }
        }
    }

    // AI Meal Scan Dialog
    if (showAiScanDialog) {
        AiMealScanDialog(
            initialMealType = activeMealTypeForManual,
            isAnalyzing = isAnalyzingAi,
            analysisResult = aiAnalysisResult,
            onDismiss = {
                showAiScanDialog = false
                viewModel.clearAiAnalysisResult()
            },
            onAnalyzePhoto = { bitmap, hint ->
                viewModel.analyzeMealPhoto(bitmap, hint)
            },
            onAnalyzeText = { text ->
                viewModel.analyzeMealText(text)
            },
            onLoadSamplePlate = {
                viewModel.loadPresetUserHealthyPlate()
            },
            onConfirmRecord = { mealType ->
                viewModel.confirmAiAnalysisToMeal(mealType)
            }
        )
    }

    // Manual Food Add Dialog
    if (showManualAddDialog) {
        AddFoodManualDialog(
            mealType = activeMealTypeForManual,
            onDismiss = { showManualAddDialog = false },
            onConfirmAdd = { food, notes ->
                viewModel.logMeal(
                    mealType = activeMealTypeForManual,
                    foodName = food.name,
                    grams = food.grams,
                    calories = food.calories,
                    carbs = food.carbs,
                    protein = food.protein,
                    fat = food.fat,
                    fiber = food.fiber,
                    notes = notes
                )
            }
        )
    }
}
