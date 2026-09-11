package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActivityLevel
import com.example.data.model.Gender
import com.example.data.model.HealthGoal
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "健康践行者",
    val gender: String = Gender.FEMALE.name,
    val age: Int = 26,
    val heightCm: Float = 165f,
    val currentWeightKg: Float = 58.5f,
    val targetWeightKg: Float = 52.0f,
    val activityLevel: String = ActivityLevel.LIGHT.name,
    val goal: String = HealthGoal.WEIGHT_LOSS.name,
    val customCalorieTarget: Int? = null,
    val customCarbsTarget: Float? = null,
    val customProteinTarget: Float? = null,
    val customFatTarget: Float? = null,
    val waterGoalMl: Int = 2000,
    val dietaryPreferences: String = "少油少盐, 高蛋白, 控糖",
    val allergies: String = "无"
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            id = id,
            name = name,
            gender = try { Gender.valueOf(gender) } catch (e: Exception) { Gender.FEMALE },
            age = age,
            heightCm = heightCm,
            currentWeightKg = currentWeightKg,
            targetWeightKg = targetWeightKg,
            activityLevel = try { ActivityLevel.valueOf(activityLevel) } catch (e: Exception) { ActivityLevel.LIGHT },
            goal = try { HealthGoal.valueOf(goal) } catch (e: Exception) { HealthGoal.WEIGHT_LOSS },
            customCalorieTarget = customCalorieTarget,
            customCarbsTarget = customCarbsTarget,
            customProteinTarget = customProteinTarget,
            customFatTarget = customFatTarget,
            waterGoalMl = waterGoalMl,
            dietaryPreferences = dietaryPreferences,
            allergies = allergies
        )
    }

    companion object {
        fun fromDomain(domain: UserProfile): UserProfileEntity {
            return UserProfileEntity(
                id = domain.id,
                name = domain.name,
                gender = domain.gender.name,
                age = domain.age,
                heightCm = domain.heightCm,
                currentWeightKg = domain.currentWeightKg,
                targetWeightKg = domain.targetWeightKg,
                activityLevel = domain.activityLevel.name,
                goal = domain.goal.name,
                customCalorieTarget = domain.customCalorieTarget,
                customCarbsTarget = domain.customCarbsTarget,
                customProteinTarget = domain.customProteinTarget,
                customFatTarget = domain.customFatTarget,
                waterGoalMl = domain.waterGoalMl,
                dietaryPreferences = domain.dietaryPreferences,
                allergies = domain.allergies
            )
        }
    }
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}
