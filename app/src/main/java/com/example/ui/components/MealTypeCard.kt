package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MealEntity
import com.example.data.model.MealType
import com.example.ui.theme.*

@Composable
fun MealTypeCard(
    mealType: MealType,
    meals: List<MealEntity>,
    onAddClick: () -> Unit,
    onAiScanClick: () -> Unit,
    onDeleteMeal: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCalories = meals.sumOf { it.calories }
    val totalProtein = meals.sumOf { it.protein.toDouble() }.toFloat()
    val totalCarbs = meals.sumOf { it.carbs.toDouble() }.toFloat()
    val totalFat = meals.sumOf { it.fat.toDouble() }.toFloat()

    val emoji = when (mealType) {
        MealType.BREAKFAST -> "🍳"
        MealType.LUNCH -> "🥗"
        MealType.DINNER -> "🍲"
        MealType.SNACK -> "🍎"
    }

    val iconBgColor = when (mealType) {
        MealType.BREAKFAST -> GeoBreakfastPink
        MealType.LUNCH -> GeoLunchPurple
        MealType.DINNER -> GeoDinnerPeach
        MealType.SNACK -> GeoSnackBlue
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_meal_${mealType.name.lowercase()}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, GeoBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 20.sp
                        )
                    }

                    Column {
                        Text(
                            text = mealType.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoDarkText
                            )
                        )
                        Text(
                            text = if (meals.isNotEmpty()) {
                                "碳水 ${totalCarbs.toInt()}g · 蛋白 ${totalProtein.toInt()}g · 脂肪 ${totalFat.toInt()}g"
                            } else "建议摄入营养均衡餐",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GeoMutedText
                            )
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$totalCalories",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
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

            // Food Items List
            if (meals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = GeoBorder.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))

                meals.forEach { meal ->
                    MealItemRow(
                        meal = meal,
                        onDelete = { onDeleteMeal(meal.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAiScanClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_ai_scan_${mealType.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeoPrimaryLight,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AI 智能识别",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                OutlinedButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .weight(0.85f)
                        .height(42.dp)
                        .testTag("btn_add_food_${mealType.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GeoBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = GeoDarkText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "手动记录",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = GeoDarkText
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MealItemRow(
    meal: MealEntity,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = meal.foodName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = GeoOnBackgroundLight
                    )
                )
                if (meal.aiRecognized) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GeoPrimaryContainerLight
                    ) {
                        Text(
                            text = "AI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoOnPrimaryContainerLight
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            Text(
                text = "${meal.grams}g · 碳水 ${meal.carbs}g · 蛋白 ${meal.protein}g · 脂肪 ${meal.fat}g" +
                        if (meal.notes.isNotBlank()) " · ${meal.notes}" else "",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = GeoMutedText
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${meal.calories} kcal",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GeoDarkText
                )
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除食物",
                    tint = GeoMutedText.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

