package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MealType

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val mealType: String, // BREAKFAST, LUNCH, DINNER, SNACK
    val foodName: String,
    val grams: Int,
    val calories: Int,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val portionDesc: String = "",
    val imageUri: String? = null,
    val notes: String = "",
    val aiRecognized: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
