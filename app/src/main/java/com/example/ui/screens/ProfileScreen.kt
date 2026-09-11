package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActivityLevel
import com.example.data.model.Gender
import com.example.data.model.HealthGoal
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.NutritionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: NutritionViewModel,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var name by remember(currentProfile) { mutableStateOf(currentProfile.name) }
    var gender by remember(currentProfile) { mutableStateOf(currentProfile.gender) }
    var ageText by remember(currentProfile) { mutableStateOf(currentProfile.age.toString()) }
    var heightText by remember(currentProfile) { mutableStateOf(currentProfile.heightCm.toInt().toString()) }
    var weightText by remember(currentProfile) { mutableStateOf(currentProfile.currentWeightKg.toString()) }
    var targetWeightText by remember(currentProfile) { mutableStateOf(currentProfile.targetWeightKg.toString()) }
    var selectedGoal by remember(currentProfile) { mutableStateOf(currentProfile.goal) }
    var selectedActivity by remember(currentProfile) { mutableStateOf(currentProfile.activityLevel) }
    var waterGoalText by remember(currentProfile) { mutableStateOf(currentProfile.waterGoalMl.toString()) }

    var saveSuccessMessage by remember { mutableStateOf(false) }

    // Live preview profile object
    val previewProfile = remember(gender, ageText, heightText, weightText, targetWeightText, selectedGoal, selectedActivity, waterGoalText) {
        UserProfile(
            name = name,
            gender = gender,
            age = ageText.toIntOrNull() ?: 26,
            heightCm = heightText.toFloatOrNull() ?: 165f,
            currentWeightKg = weightText.toFloatOrNull() ?: 58.5f,
            targetWeightKg = targetWeightText.toFloatOrNull() ?: 52.0f,
            activityLevel = selectedActivity,
            goal = selectedGoal,
            waterGoalMl = waterGoalText.toIntOrNull() ?: 2000
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBackgroundLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        // User Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
                border = BorderStroke(1.dp, GeoBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(GeoPrimaryLight.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = GeoPrimaryLight,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column {
                        Text(
                            text = previewProfile.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoDarkText
                            )
                        )
                        Text(
                            text = "BMI: ${previewProfile.bmi} · ${previewProfile.bmiCategory} · ${previewProfile.goal.title}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GeoPrimaryLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // Body Metrics Input
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
                border = BorderStroke(1.dp, GeoBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "身体基础生理数据",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeoDarkText
                        )
                    )

                    // Gender Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Gender.values().forEach { g ->
                            FilterChip(
                                selected = gender == g,
                                onClick = { gender = g },
                                label = { Text(g.title) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it.filter { c -> c.isDigit() } },
                            label = { Text("年龄 (岁)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it.filter { c -> c.isDigit() } },
                            label = { Text("身高 (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("当前体重 (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = targetWeightText,
                            onValueChange = { targetWeightText = it },
                            label = { Text("目标体重 (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Health Goal Selection
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
                border = BorderStroke(1.dp, GeoBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "核心健康与饮食目标",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeoDarkText
                        )
                    )

                    HealthGoal.values().forEach { goal ->
                        val isSelected = selectedGoal == goal
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) GeoPrimaryLight.copy(alpha = 0.12f) else GeoSurfaceLight,
                            border = BorderStroke(1.dp, if (isSelected) GeoPrimaryLight else GeoBorderLight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedGoal = goal }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = goal.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) GeoPrimaryLight else GeoDarkText
                                        )
                                    )
                                    Text(
                                        text = goal.desc,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = GeoMutedText
                                        )
                                    )
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedGoal = goal }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Activity Level Selection
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
                border = BorderStroke(1.dp, GeoBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "日常活动强度",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeoDarkText
                        )
                    )

                    ActivityLevel.values().forEach { level ->
                        val isSelected = selectedActivity == level
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) GeoPrimaryLight.copy(alpha = 0.12f) else GeoSurfaceLight,
                            border = BorderStroke(1.dp, if (isSelected) GeoPrimaryLight else GeoBorderLight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedActivity = level }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = level.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) GeoPrimaryLight else GeoDarkText
                                        )
                                    )
                                    Text(
                                        text = level.desc,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = GeoMutedText
                                        )
                                    )
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedActivity = level }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scientific Metabolic Calculation Result Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GeoSurfaceLight
                ),
                border = BorderStroke(1.dp, GeoBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = GeoPrimaryLight
                        )
                        Text(
                            text = "智能代谢与热量分配计算",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoDarkText
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetabolicStat("基础代谢 BMR", "${previewProfile.bmr} kcal")
                        MetabolicStat("总能耗 TDEE", "${previewProfile.tdee} kcal")
                        MetabolicStat("每日推荐摄入", "${previewProfile.calculatedCalorieTarget} kcal")
                    }

                    HorizontalDivider(color = GeoBorderLight)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetabolicStat("推荐碳水", "${previewProfile.calculatedCarbsGrams.toInt()}g")
                        MetabolicStat("推荐蛋白质", "${previewProfile.calculatedProteinGrams.toInt()}g")
                        MetabolicStat("推荐脂肪", "${previewProfile.calculatedFatGrams.toInt()}g")
                    }
                }
            }
        }

        // Gemini AI Model Configuration Card
        item {
            var apiKeyInput by remember { mutableStateOf(viewModel.getGeminiApiKey()) }
            val isConfigured by viewModel.apiKeyConfigured.collectAsStateWithLifecycle()
            var apiKeySavedFeedback by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
                border = BorderStroke(1.dp, if (isConfigured) GeoPrimaryLight.copy(alpha = 0.5f) else GeoBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GeoPrimaryLight
                            )
                            Text(
                                text = "Gemini AI 大模型配置",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GeoDarkText
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isConfigured) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = if (isConfigured) "● 真实大模型已接入" else "○ 离线智能分析中",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConfigured) Color(0xFF2E7D32) else Color(0xFFE65100)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "支持 Google Gemini 3.5 Flash 真实多模态视觉识别与营养分析。如需使用自己的 API Key，可直接在下方输入：",
                        style = MaterialTheme.typography.bodySmall.copy(color = GeoMutedText)
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            apiKeySavedFeedback = false
                        },
                        placeholder = { Text("输入 Gemini API Key (AIzaSy...)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_gemini_api_key"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                viewModel.setGeminiApiKey(apiKeyInput.trim())
                                apiKeySavedFeedback = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_save_gemini_key")
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("应用 Key")
                        }
                    }

                    if (apiKeySavedFeedback) {
                        Text(
                            text = if (isConfigured) "✓ API Key 已更新并生效！" else "Key 已清除，将使用内置引擎",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isConfigured) Color(0xFF2E7D32) else GeoMutedText,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    viewModel.updateUserProfile(previewProfile)
                    saveSuccessMessage = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_profile"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GeoPrimaryLight,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存档案与营养目标", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            if (saveSuccessMessage) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ 个人档案与热量目标已同步更新！",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = GeoProteinCyan,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MetabolicStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = GeoMutedText
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                color = GeoPrimaryLight
            )
        )
    }
}
