package com.example.data.model

enum class MealType(val displayName: String, val defaultTime: String, val iconName: String) {
    BREAKFAST("早餐", "08:00", "wb_sunny"),
    LUNCH("午餐", "12:30", "restaurant"),
    DINNER("晚餐", "18:30", "dinner_dining"),
    SNACK("加餐/点心", "15:30", "cookie")
}

enum class HealthGoal(val title: String, val desc: String, val calorieAdjustmentRatio: Float, val carbsRatio: Float, val proteinRatio: Float, val fatRatio: Float) {
    WEIGHT_LOSS("健康减脂", "保持热量缺口，高蛋白低脂饱腹", -0.20f, 0.40f, 0.35f, 0.25f),
    MUSCLE_GAIN("增肌塑形", "充足蛋白质与复合碳水赋能", 0.15f, 0.50f, 0.30f, 0.20f),
    MAINTENANCE("健康均衡", "能量摄入与消耗维持动态平衡", 0.0f, 0.50f, 0.20f, 0.30f),
    LOW_CARB("低碳轻食", "减少精制碳水，控制血糖平稳", -0.10f, 0.25f, 0.35f, 0.40f),
    SUGAR_CONTROL("控糖稳脂", "低GI慢碳，高膳食纤维防波动", -0.10f, 0.35f, 0.35f, 0.30f)
}

enum class ActivityLevel(val title: String, val factor: Float, val desc: String) {
    SEDENTARY("久坐不动", 1.2f, "办公室伏案，日常极少运动"),
    LIGHT("轻度活动", 1.375f, "每周运动 1-3 天，日常散步"),
    MODERATE("中度运动", 1.55f, "每周运动 3-5 天，中等强度"),
    ACTIVE("高强度运动", 1.725f, "每周运动 6-7 天，重体力或高强度训练")
}

enum class Gender(val title: String) {
    MALE("男士"),
    FEMALE("女士")
}
