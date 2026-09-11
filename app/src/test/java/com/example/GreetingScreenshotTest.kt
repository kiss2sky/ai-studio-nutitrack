package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.DailyNutritionSummary
import com.example.ui.components.CalorieHeroCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun calorieHeroCard_screenshot() {
    val sampleSummary = DailyNutritionSummary(
        date = "2026-09-10",
        totalCalories = 1450,
        targetCalories = 2000,
        totalCarbs = 180f,
        targetCarbs = 250f,
        totalProtein = 85f,
        targetProtein = 110f,
        totalFat = 45f,
        targetFat = 60f,
        totalFiber = 22f,
        targetFiber = 28f,
        waterIntakeMl = 1250,
        waterTargetMl = 2000
    )

    composeTestRule.setContent {
        MyApplicationTheme {
            CalorieHeroCard(summary = sampleSummary)
        }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/calorie_hero.png")
  }
}

