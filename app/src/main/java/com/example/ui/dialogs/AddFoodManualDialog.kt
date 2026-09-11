package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.DefaultFoodDatabase
import com.example.data.model.FoodItem
import com.example.data.model.MealType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodManualDialog(
    mealType: MealType,
    onDismiss: () -> Unit,
    onConfirmAdd: (FoodItem, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("全部") }
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
    var customGrams by remember { mutableStateOf("100") }
    var notes by remember { mutableStateOf("") }
    var isCustomEntryMode by remember { mutableStateOf(false) }

    // Custom entry fields
    var customName by remember { mutableStateOf("") }
    var customCalories by remember { mutableStateOf("") }
    var customCarbs by remember { mutableStateOf("") }
    var customProtein by remember { mutableStateOf("") }
    var customFat by remember { mutableStateOf("") }
    var customFiber by remember { mutableStateOf("") }

    val categories = listOf("全部", "优质蛋白", "优质慢碳", "高纤蔬菜", "优质脂肪", "低卡水果")

    val filteredFoods = remember(searchQuery, selectedCategory) {
        DefaultFoodDatabase.presetFoods.filter { food ->
            val matchQuery = searchQuery.isBlank() || food.name.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategory == "全部" || food.category == selectedCategory
            matchQuery && matchCategory
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "添加食物到【${mealType.displayName}】",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "搜索健康食物库或自定义记录",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_manual_add")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode switch: Preset vs Custom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isCustomEntryMode,
                        onClick = { isCustomEntryMode = false },
                        label = { Text("📖 预设食物库") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isCustomEntryMode,
                        onClick = { isCustomEntryMode = true },
                        label = { Text("✏️ 自定义添加") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isCustomEntryMode) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索食物（如：鲜虾、鸡蛋、南瓜、西兰花）") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_food"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Categories Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            SuggestionChip(
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (selectedCategory == cat) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Food List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFoods) { food ->
                            val isSelected = selectedFood?.id == food.id
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedFood = food
                                        customGrams = food.grams.toString()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = food.name,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surface
                                            ) {
                                                Text(
                                                    text = food.category,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "每${food.grams}g · 碳水${food.carbs}g · 蛋白${food.protein}g · 脂肪${food.fat}g · 纤维${food.fiber}g",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        if (food.tip.isNotBlank()) {
                                            Text(
                                                text = "💡 ${food.tip}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${food.calories} kcal",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Portion Selector for selected food
                    if (selectedFood != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "所选: ${selectedFood!!.name}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )

                                    val gramsInt = customGrams.toIntOrNull() ?: selectedFood!!.grams
                                    val calculated = selectedFood!!.calculateForGrams(gramsInt)

                                    Text(
                                        text = "= ${calculated.calories} kcal",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = customGrams,
                                        onValueChange = { customGrams = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("分量 (克 g)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_food_grams"),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = notes,
                                        onValueChange = { notes = it },
                                        label = { Text("备注 (可选)") },
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        val gramsInt = customGrams.toIntOrNull() ?: 100
                                        val calculated = selectedFood!!.calculateForGrams(gramsInt)
                                        onConfirmAdd(calculated, notes)
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("btn_confirm_add_preset"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("确认添加到【${mealType.displayName}】", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Custom Entry Mode Form
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                label = { Text("食物名称 *") },
                                placeholder = { Text("例如：水煮大虾") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_custom_food_name"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customGrams,
                                    onValueChange = { customGrams = it.filter { c -> c.isDigit() } },
                                    label = { Text("分量 (g) *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = customCalories,
                                    onValueChange = { customCalories = it.filter { c -> c.isDigit() } },
                                    label = { Text("热量 (kcal) *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customCarbs,
                                    onValueChange = { customCarbs = it },
                                    label = { Text("碳水 (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = customProtein,
                                    onValueChange = { customProtein = it },
                                    label = { Text("蛋白质 (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customFat,
                                    onValueChange = { customFat = it },
                                    label = { Text("脂肪 (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = customFiber,
                                    onValueChange = { customFiber = it },
                                    label = { Text("膳食纤维 (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("备注说明") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (customName.isNotBlank() && customCalories.isNotBlank()) {
                                val food = FoodItem(
                                    id = "custom_${System.currentTimeMillis()}",
                                    name = customName,
                                    grams = customGrams.toIntOrNull() ?: 100,
                                    calories = customCalories.toIntOrNull() ?: 100,
                                    carbs = customCarbs.toFloatOrNull() ?: 0f,
                                    protein = customProtein.toFloatOrNull() ?: 0f,
                                    fat = customFat.toFloatOrNull() ?: 0f,
                                    fiber = customFiber.toFloatOrNull() ?: 0f,
                                    category = "自定义"
                                )
                                onConfirmAdd(food, notes)
                                onDismiss()
                            }
                        },
                        enabled = customName.isNotBlank() && customCalories.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_confirm_custom_food"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("保存自定义记录", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
