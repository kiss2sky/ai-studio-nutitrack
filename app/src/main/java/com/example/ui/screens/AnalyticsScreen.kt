package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MealType
import com.example.ui.theme.*
import com.example.ui.viewmodel.NutritionViewModel

@Composable
fun AnalyticsScreen(
    viewModel: NutritionViewModel,
    modifier: Modifier = Modifier
) {
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val meals by viewModel.mealsForSelectedDate.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val totalCalories = dailySummary.totalCalories
    val totalCarbs = dailySummary.totalCarbs
    val totalProtein = dailySummary.totalProtein
    val totalFat = dailySummary.totalFat

    val carbsCals = totalCarbs * 4f
    val proteinCals = totalProtein * 4f
    val fatCals = totalFat * 9f
    val macroTotalCals = (carbsCals + proteinCals + fatCals).coerceAtLeast(1f)

    val carbsPercent = ((carbsCals / macroTotalCals) * 100).toInt()
    val proteinPercent = ((proteinCals / macroTotalCals) * 100).toInt()
    val fatPercent = (100 - carbsPercent - proteinPercent).coerceAtLeast(0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBackgroundLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Header
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
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GeoPrimaryLight.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = GeoPrimaryLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "营养与热量深度分析",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoDarkText
                            )
                        )
                        Text(
                            text = "三大宏量营养素分布与能量平衡",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GeoMutedText
                            )
                        )
                    }
                }
            }
        }

        // Macro Nutrition Donut / Distribution Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
                border = BorderStroke(1.dp, GeoBorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_macro_donut")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "宏量营养素供能比",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoDarkText
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GeoPrimaryLight.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "标准供能比 4:3:3",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeoPrimaryLight
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Donut Canvas
                        Box(
                            modifier = Modifier.size(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 22.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                                val arcSize = Size(diameter, diameter)

                                if (totalCalories == 0) {
                                    drawArc(
                                        color = GeoBorderLight,
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth)
                                    )
                                } else {
                                    val carbsSweep = (carbsPercent / 100f) * 360f
                                    val proteinSweep = (proteinPercent / 100f) * 360f
                                    val fatSweep = (fatPercent / 100f) * 360f

                                    // Carbs arc (Coral)
                                    drawArc(
                                        color = GeoCarbCoral,
                                        startAngle = -90f,
                                        sweepAngle = carbsSweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth)
                                    )

                                    // Protein arc (Cyan)
                                    drawArc(
                                        color = GeoProteinCyan,
                                        startAngle = -90f + carbsSweep,
                                        sweepAngle = proteinSweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth)
                                    )

                                    // Fat arc (Amber)
                                    drawArc(
                                        color = GeoFatAmber,
                                        startAngle = -90f + carbsSweep + proteinSweep,
                                        sweepAngle = fatSweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$totalCalories",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = GeoDarkText
                                    )
                                )
                                Text(
                                    text = "kcal",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GeoMutedText
                                    )
                                )
                            }
                        }

                        // Legend Details
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            LegendRow("碳水化合物", "$carbsPercent%", "${totalCarbs.toInt()}g", GeoCarbCoral)
                            LegendRow("优质蛋白质", "$proteinPercent%", "${totalProtein.toInt()}g", GeoProteinCyan)
                            LegendRow("健康油脂", "$fatPercent%", "${totalFat.toInt()}g", GeoFatAmber)
                            LegendRow("膳食纤维", "-", "${dailySummary.totalFiber.toInt()}g", GeoFiberOlive)
                        }
                    }
                }
            }
        }

        // Meal Timing Breakdown Card
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "各餐次能量摄入占比",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeoDarkText
                        )
                    )

                    MealType.values().forEach { mealType ->
                        val mealCal = meals.filter { it.mealType == mealType.name }.sumOf { it.calories }
                        val pct = if (totalCalories > 0) (mealCal.toFloat() / totalCalories) else 0f

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mealType.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = GeoDarkText
                                    )
                                )
                                Text(
                                    text = "$mealCal kcal (${(pct * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GeoMutedText
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = GeoPrimaryLight,
                                trackColor = GeoBorderLight
                            )
                        }
                    }
                }
            }
        }

        // Energy Deficit & Goal Insights Card
        item {
            val remaining = dailySummary.targetCalories - dailySummary.totalCalories
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GeoSurfaceLight
                ),
                border = BorderStroke(1.dp, GeoBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GeoProteinCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = GeoProteinCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "健康缺口评估",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoDarkText
                            )
                        )
                        Text(
                            text = if (remaining >= 0) {
                                "当前创造了 $remaining kcal 的健康能量缺口，符合【${userProfile.goal.title}】的生理减脂/塑形节奏。"
                            } else {
                                "今日热量稍超出预算 ${-remaining} kcal，可通过增加快走运动或调整下一餐粗粮比例予以平衡。"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = GeoMutedText
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(title: String, percent: String, amount: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = GeoDarkText),
            modifier = Modifier.width(75.dp)
        )
        Text(
            text = percent,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = color),
            modifier = Modifier.width(36.dp)
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodySmall.copy(color = GeoMutedText)
        )
    }
}
