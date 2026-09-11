package com.example.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.FoodItem
import com.example.data.model.MealType
import com.example.data.model.NutritionAnalysisResult
import com.example.ui.theme.*
import com.example.util.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMealScanDialog(
    initialMealType: MealType = MealType.LUNCH,
    isAnalyzing: Boolean,
    analysisResult: NutritionAnalysisResult?,
    onDismiss: () -> Unit,
    onAnalyzePhoto: (Bitmap, String) -> Unit,
    onAnalyzeText: (String) -> Unit,
    onLoadSamplePlate: () -> Unit,
    onConfirmRecord: (MealType) -> Unit
) {
    val context = LocalContext.current
    var selectedMealType by remember { mutableStateOf(initialMealType) }
    var textInput by remember { mutableStateOf("") }
    var photoHintText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var activeTab by remember { mutableIntStateOf(0) } // 0: 拍照/图片识别, 1: 文本描述识别

    // High-resolution FileProvider Camera Launcher
    val takePictureToFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess && cameraPhotoUri != null) {
            val uri = cameraPhotoUri!!
            selectedImageUri = uri
            val bitmap = ImageUtils.decodeUriToSafeBitmap(context, uri)
            if (bitmap != null) {
                capturedBitmap = bitmap
                onAnalyzePhoto(bitmap, photoHintText.ifBlank { "请精准识别图中食物并计算卡路里及营养素" })
            } else {
                Toast.makeText(context, "无法加载拍摄的照片，请重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Direct Camera Preview Fallback Launcher
    val takePicturePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val safeBmp = ImageUtils.ensureSoftwareBitmap(bitmap)
            capturedBitmap = safeBmp
            selectedImageUri = null
            onAnalyzePhoto(safeBmp, photoHintText.ifBlank { "请精准识别图中食物并计算卡路里及营养素" })
        }
    }

    fun startCameraCapture() {
        try {
            val cacheFolder = java.io.File(context.cacheDir, "camera_photos").apply { mkdirs() }
            val tempFile = java.io.File.createTempFile("meal_cam_${System.currentTimeMillis()}", ".jpg", cacheFolder)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            cameraPhotoUri = uri
            takePictureToFileLauncher.launch(uri)
        } catch (e: Exception) {
            // Fallback to preview capture
            takePicturePreviewLauncher.launch(null)
        }
    }

    // Permission Launcher for Camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCameraCapture()
        } else {
            Toast.makeText(context, "需要相机权限以进行拍照识别，请在设置中允许", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            val bitmap = ImageUtils.decodeUriToSafeBitmap(context, uri)
            if (bitmap != null) {
                capturedBitmap = bitmap
                onAnalyzePhoto(bitmap, photoHintText.ifBlank { "请精准识别图中食物并计算卡路里及营养素" })
            } else {
                Toast.makeText(context, "无法读取相册图片，请选择其他图片", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val sampleTextQueries = listOf(
        "一碗牛肉拉面 + 1颗水煮蛋",
        "香煎鸡胸肉150g + 蒸紫薯 + 西兰花",
        "生椰拿铁1杯 + 全麦欧包1个",
        "清蒸鲈鱼120g + 炒菠菜 + 杂粮饭"
    )

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
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "AI 智能营养与热量识别",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "基于 Gemini 多模态视觉与营养算法",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_ai_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Switcher
                PrimaryTabRow(
                    selectedTabIndex = activeTab,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("📷 拍照 / 图片识别", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("✍️ 文字智能识别", fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Input Section based on Tab
                    if (activeTab == 0) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Image Preview Area
                                    if (capturedBitmap != null) {
                                        Image(
                                            bitmap = capturedBitmap!!.asImageBitmap(),
                                            contentDescription = "已拍摄的食物照片",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    } else if (selectedImageUri != null) {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "选择的餐食照片",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // Three Action Buttons: Camera, Gallery, Sample
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // 1. Direct Camera Button
                                        Button(
                                            onClick = {
                                                val hasPermission = ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.CAMERA
                                                ) == PackageManager.PERMISSION_GRANTED

                                                if (hasPermission) {
                                                    startCameraCapture()
                                                } else {
                                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_take_photo"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("拍照", fontWeight = FontWeight.Bold)
                                        }

                                        // 2. Gallery Button
                                        FilledTonalButton(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_pick_photo"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("相册")
                                        }

                                        // 3. Preset Sample Plate Button
                                        OutlinedButton(
                                            onClick = {
                                                capturedBitmap = null
                                                selectedImageUri = null
                                                onLoadSamplePlate()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_load_sample_plate"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("示例盘")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Optional hint input for multimodal reasoning
                                    OutlinedTextField(
                                        value = photoHintText,
                                        onValueChange = { photoHintText = it },
                                        placeholder = { Text("可选：输入补充描述（如：去皮鸡胸肉、少油少盐）", style = MaterialTheme.typography.bodySmall) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    } else {
                        // Text Description Tab
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = textInput,
                                        onValueChange = { textInput = it },
                                        placeholder = { Text("输入吃的食物，例如：清蒸鲜虾6只、水煮蛋1个、贝贝南瓜100g、西兰花和清炖肉丸") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .testTag("input_ai_text"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Quick chips
                                    Text("快速填入示例：", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(sampleTextQueries) { sample ->
                                            SuggestionChip(
                                                onClick = { textInput = sample },
                                                label = { Text(sample, style = MaterialTheme.typography.labelSmall) },
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { onAnalyzeText(textInput) },
                                        enabled = textInput.isNotBlank() && !isAnalyzing,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_submit_text_ai"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("开始智能解析")
                                    }
                                }
                            }
                        }
                    }

                    // Analyzing Indicator
                    if (isAnalyzing) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Gemini AI 正在智能识别食材与营养...",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                        Text(
                                            text = "正在精准分析食材成分、热量、宏量比例与升糖指数",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Analysis Result Display
                    if (analysisResult != null && !isAnalyzing) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = CardDefaults.outlinedCardBorder(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Title & Health Score Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = analysisResult.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Text(
                                                text = analysisResult.summary,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Text(
                                                text = "健康分 ${analysisResult.healthScore} · ${analysisResult.ratingTag}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Total Nutrition Banner
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            NutrientBadge("总热量", "${analysisResult.totalCalories} kcal", ColorCalorieFlame)
                                            NutrientBadge("碳水", "${analysisResult.totalCarbs.toInt()}g", ColorCarbs)
                                            NutrientBadge("蛋白质", "${analysisResult.totalProtein.toInt()}g", ColorProtein)
                                            NutrientBadge("脂肪", "${analysisResult.totalFat.toInt()}g", ColorFat)
                                            NutrientBadge("纤维", "${analysisResult.totalFiber.toInt()}g", ColorFiber)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "识别到的食材明细 (${analysisResult.items.size} 项):",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    analysisResult.items.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "• ${item.name}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Text(
                                                        text = "${item.grams}g",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "${item.calories} kcal (P:${item.protein}g C:${item.carbs}g F:${item.fat}g)",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }

                                    if (analysisResult.highlights.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "💡 营养师亮点点评:",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        analysisResult.highlights.forEach { h ->
                                            Text(
                                                text = "✓ $h",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurface
                                                ),
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (analysisResult.suggestions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "🥗 搭配改善建议:",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        )
                                        analysisResult.suggestions.forEach { s ->
                                            Text(
                                                text = "• $s",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurface
                                                ),
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Footer
                if (analysisResult != null && !isAnalyzing) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        tonalElevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "选择记录到餐次：",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MealType.values().forEach { type ->
                                    FilterChip(
                                        selected = selectedMealType == type,
                                        onClick = { selectedMealType = type },
                                        label = { Text(type.displayName) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    onConfirmRecord(selectedMealType)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_confirm_ai_meal"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("一键记录到【${selectedMealType.displayName}】", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientBadge(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}
