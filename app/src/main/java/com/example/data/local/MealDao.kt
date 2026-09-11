package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE date = :date ORDER BY timestamp ASC")
    fun getMealsByDate(date: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, timestamp ASC")
    fun getMealsBetweenDates(startDate: String, endDate: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals ORDER BY timestamp DESC LIMIT 50")
    fun getRecentMeals(): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealEntity>)

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMealById(id: Long)

    @Query("SELECT DISTINCT date FROM meals ORDER BY date DESC")
    fun getAllLoggedDates(): Flow<List<String>>
}
