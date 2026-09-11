package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "dietary_advices")
data class DietaryAdviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val healthScore: Int,
    val summaryTitle: String,
    val goalAssessment: String,
    val mealBreakdownAdvice: String,
    val nutrientGapsJson: String, // comma or json separated
    val actionableTipsJson: String, // comma or json separated
    val suggestedNextMeal: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface DietaryAdviceDao {
    @Query("SELECT * FROM dietary_advices WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    fun getAdviceForDate(date: String): Flow<DietaryAdviceEntity?>

    @Query("SELECT * FROM dietary_advices ORDER BY timestamp DESC LIMIT 20")
    fun getAllAdvices(): Flow<List<DietaryAdviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvice(advice: DietaryAdviceEntity): Long
}

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val intakeMl: Int = 0
)

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs WHERE date = :date")
    fun getWaterLog(date: String): Flow<WaterLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWaterLog(log: WaterLogEntity)
}
