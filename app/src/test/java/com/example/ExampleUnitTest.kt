package com.example

import com.example.data.model.ActivityLevel
import com.example.data.model.Gender
import com.example.data.model.HealthGoal
import com.example.data.model.UserProfile
import com.example.data.local.DefaultFoodDatabase
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testBmrAndTdeeCalculation() {
    val profile = UserProfile(
      gender = Gender.FEMALE,
      age = 26,
      heightCm = 165f,
      currentWeightKg = 58.5f,
      activityLevel = ActivityLevel.LIGHT,
      goal = HealthGoal.WEIGHT_LOSS
    )
    assertTrue("BMR should be greater than 1000", profile.bmr > 1000)
    assertTrue("TDEE should be greater than BMR", profile.tdee > profile.bmr)
    assertTrue("Weight loss target calories should be within safe range", profile.calculatedCalorieTarget in 1200..2500)
  }

  @Test
  fun testFoodGramsScaling() {
    val shrimp = DefaultFoodDatabase.presetFoods.first { it.id == "p_shrimp" }
    val scaledShrimp = shrimp.calculateForGrams(200)
    assertEquals(198, scaledShrimp.calories)
    assertEquals(42.0f, scaledShrimp.protein, 0.1f)
  }
}

