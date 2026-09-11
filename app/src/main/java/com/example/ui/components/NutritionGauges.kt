package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyNutritionSummary
import com.example.ui.theme.*

@Composable
fun DateSelectorBar(
    selectedDate: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GeoBorder),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier.testTag("btn_prev_day")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "前一天",
                    tint = GeoDarkText
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onTodayClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = selectedDate,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = GeoDarkText
                    )
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GeoPrimaryContainerLight
                ) {
                    Text(
                        text = "今日",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeoOnPrimaryContainerLight
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = onNextDay,
                modifier = Modifier.testTag("btn_next_day")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "后一天",
                    tint = GeoDarkText
                )
            }
        }
    }
}

@Composable
fun CalorieHeroCard(
    summary: DailyNutritionSummary,
    modifier: Modifier = Modifier
) {
    val remaining = (summary.targetCalories - summary.totalCalories)
    val progress = if (summary.targetCalories > 0) {
        (summary.totalCalories.toFloat() / summary.targetCalories).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "calorie_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_calorie_hero"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = GeoHeroCardBg
        ),
        border = BorderStroke(1.dp, GeoBorder.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Geometric Calorie Ring
            Box(
                modifier = Modifier.size(184.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                    val arcSize = Size(diameter, diameter)

                    // Track arc (Full background circle)
                    drawArc(
                        color = GeoEnergyTrack,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active progress arc
                    val sweep = animatedProgress * 360f
                    if (sweep > 0f) {
                        drawArc(
                            color = GeoEnergyArc,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "%,d".format(summary.totalCalories),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeoDarkText,
                            fontSize = 36.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (remaining >= 0) "剩余 $remaining kcal" else "超出 ${-remaining} kcal",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (remaining >= 0) GeoOnSurfaceVariantLight else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Macro Nutrients Breakdown Grid (Geometric Capsules)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GeometricMacroPill(
                    label = "蛋白质",
                    current = summary.totalProtein,
                    target = summary.targetProtein,
                    modifier = Modifier.weight(1f)
                )
                GeometricMacroPill(
                    label = "碳水",
                    current = summary.totalCarbs,
                    target = summary.targetCarbs,
                    modifier = Modifier.weight(1f)
                )
                GeometricMacroPill(
                    label = "脂肪",
                    current = summary.totalFat,
                    target = summary.targetFat,
                    modifier = Modifier.weight(1f)
                )
                GeometricMacroPill(
                    label = "纤维",
                    current = summary.totalFiber,
                    target = summary.targetFiber,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GeometricMacroPill(
    label: String,
    current: Float,
    target: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, GeoBorder.copy(alpha = 0.8f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GeoMutedText,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${current.toInt()}/${target.toInt()}g",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = GeoOnBackgroundLight,
                    fontSize = 12.sp
                )
            )
        }
    }
}

