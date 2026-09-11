package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DietaryAdvice
import com.example.ui.theme.*
import com.example.ui.viewmodel.NutritionViewModel

@Composable
fun AdviceScreen(
    viewModel: NutritionViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val storedAdvice by viewModel.storedAdvice.collectAsStateWithLifecycle()
    val isGeneratingAdvice by viewModel.isGeneratingAdvice.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()

    var chatInputText by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "🎃 贝贝南瓜为什么适合减脂？",
        "🦐 减脂期蛋白质怎么补充最有效？",
        "🌙 晚上突然饿了吃什么不发胖？",
        "⚖️ 怎样打破减脂平台期？"
    )

    LaunchedEffect(Unit) {
        if (storedAdvice == null) {
            viewModel.requestPersonalizedAdvice()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBackgroundLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Geometric Balance Top Header Card
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GeoPrimaryLight.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GeoPrimaryLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "AI 个性化饮食与营养指导",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GeoDarkText
                                )
                            )
                            Text(
                                text = "针对【${userProfile.goal.title}】的专属科学建议",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GeoMutedText
                                )
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { viewModel.requestPersonalizedAdvice() },
                        enabled = !isGeneratingAdvice,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = GeoPrimaryLight.copy(alpha = 0.15f),
                            contentColor = GeoPrimaryLight
                        ),
                        modifier = Modifier.testTag("btn_refresh_advice")
                    ) {
                        if (isGeneratingAdvice) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = GeoPrimaryLight)
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("重新评估", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Advice Report Card
        storedAdvice?.let { advice ->
            item {
                AdviceReportCard(advice = advice, targetCalories = dailySummary.targetCalories)
            }
        }

        // AI Nutritionist Consultation Section Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = GeoPrimaryLight,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "AI 营养师实时问答",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GeoDarkText
                    )
                )
            }
        }

        // Quick Suggestion Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickQuestions) { q ->
                    SuggestionChip(
                        onClick = {
                            val cleanQ = q.substringAfter(" ")
                            viewModel.sendNutritionChat(cleanQ)
                        },
                        label = { Text(q, fontSize = 12.sp, color = GeoDarkText, fontWeight = FontWeight.Medium) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoBorderLight),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = GeoSurfaceLight
                        )
                    )
                }
            }
        }

        // Chat Message List
        items(chatMessages) { message ->
            ChatBubble(message = message)
        }

        if (isChatLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GeoSurfaceLight
                        ),
                        border = BorderStroke(1.dp, GeoBorderLight)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = GeoPrimaryLight)
                            Text("营养师正在分析营养数据并生成建议...", style = MaterialTheme.typography.bodySmall, color = GeoMutedText)
                        }
                    }
                }
            }
        }

        // Chat Input Box
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
                border = BorderStroke(1.dp, GeoBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatInputText,
                        onValueChange = { chatInputText = it },
                        placeholder = { Text("向营养师提问饮食搭配、食材或热量问题...", color = GeoMutedText, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_chat_nutrition"),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeoPrimaryLight,
                            unfocusedBorderColor = GeoBorderLight
                        )
                    )

                    IconButton(
                        onClick = {
                            if (chatInputText.isNotBlank()) {
                                viewModel.sendNutritionChat(chatInputText)
                                chatInputText = ""
                            }
                        },
                        enabled = chatInputText.isNotBlank() && !isChatLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (chatInputText.isNotBlank()) GeoPrimaryLight
                                else GeoBorderLight
                            )
                            .testTag("btn_send_chat")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "发送",
                            tint = if (chatInputText.isNotBlank()) Color.White
                            else GeoMutedText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdviceReportCard(advice: DietaryAdvice, targetCalories: Int) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
        border = BorderStroke(1.dp, GeoBorderLight),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_advice_report")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Health Score & Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = advice.summaryTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeoDarkText
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = advice.goalAssessment,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GeoMutedText
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GeoProteinCyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, GeoProteinCyan.copy(alpha = 0.3f))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${advice.healthScore}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = GeoProteinCyan
                            )
                        )
                        Text(
                            text = "健康分",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoProteinCyan
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = GeoBorderLight)

            // Actionable Tips List
            Text(
                text = "✨ 核心饮食指导建议:",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GeoDarkText
                )
            )

            advice.actionableTips.forEach { tip ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GeoProteinCyan,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GeoDarkText
                        )
                    )
                }
            }

            // Gaps & Watchouts
            if (advice.nutrientGaps.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GeoCarbCoral.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, GeoCarbCoral.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = GeoCarbCoral,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "需要注意的营养缺口 / 调整项:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GeoCarbCoral
                                )
                            )
                        }
                        advice.nutrientGaps.forEach { gap ->
                            Text(
                                text = "• $gap",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GeoDarkText
                                )
                            )
                        }
                    }
                }
            }

            // Next Suggested Meal Plate
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = GeoPrimaryLight.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, GeoPrimaryLight.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GeoPrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "营养师推荐下餐方案",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoPrimaryLight
                            )
                        )
                        Text(
                            text = advice.suggestedNextMeal,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = GeoDarkText
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: com.example.ui.viewmodel.ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GeoPrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) GeoPrimaryLight else GeoSurfaceLight
            ),
            border = if (message.isUser) null else BorderStroke(1.dp, GeoBorderLight),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (message.isUser) Color.White else GeoDarkText
                ),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}
