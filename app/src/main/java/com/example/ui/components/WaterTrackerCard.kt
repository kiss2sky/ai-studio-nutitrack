package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun WaterTrackerCard(
    currentMl: Int,
    targetMl: Int,
    onAddWater: (Int) -> Unit,
    onResetWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (targetMl > 0) (currentMl.toFloat() / targetMl).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "water_progress")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_water_tracker"),
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
                .padding(18.dp)
        ) {
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
                            .background(GeoSnackBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = ColorWater,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "今日饮水追踪",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoDarkText
                            )
                        )
                        Text(
                            text = if (currentMl >= targetMl) "恭喜！今日补水目标已达成 💧" else "还需饮水 ${(targetMl - currentMl).coerceAtLeast(0)} ml",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (currentMl >= targetMl) GeoPrimaryLight else GeoMutedText
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$currentMl",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ColorWater
                        )
                    )
                    Text(
                        text = " / $targetMl ml",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = GeoMutedText
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = ColorWater,
                trackColor = GeoSnackBlue.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { onAddWater(250) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("btn_add_water_250"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GeoSnackBlue.copy(alpha = 0.5f),
                        contentColor = GeoTertiaryLight
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+250 ml (一杯)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                }

                FilledTonalButton(
                    onClick = { onAddWater(500) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("btn_add_water_500"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GeoSnackBlue.copy(alpha = 0.5f),
                        contentColor = GeoTertiaryLight
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+500 ml (一瓶)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                }

                IconButton(
                    onClick = onResetWater,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("btn_reset_water")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "重置饮水量",
                        tint = GeoMutedText
                    )
                }
            }
        }
    }
}
