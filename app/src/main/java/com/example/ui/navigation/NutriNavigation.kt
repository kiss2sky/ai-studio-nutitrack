package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AdviceScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.NutritionViewModel

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    object Dashboard : Screen("dashboard", "今日记录", Icons.Filled.RestaurantMenu, Icons.Outlined.RestaurantMenu, "tab_dashboard")
    object Advice : Screen("advice", "AI 建议", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_advice")
    object Analytics : Screen("analytics", "营养分析", Icons.Filled.BarChart, Icons.Outlined.BarChart, "tab_analytics")
    object Profile : Screen("profile", "目标与档案", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutriApp() {
    val navController = rememberNavController()
    val viewModel: NutritionViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val items = listOf(
        Screen.Dashboard,
        Screen.Advice,
        Screen.Analytics,
        Screen.Profile
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val currentScreen = items.find { it.route == currentRoute }
                    Text(
                        text = when (currentRoute) {
                            Screen.Dashboard.route -> "NutriTrack 营养追踪"
                            Screen.Advice.route -> "AI 饮食评估与建议"
                            Screen.Analytics.route -> "营养分布与能量图表"
                            Screen.Profile.route -> "健康档案与目标设定"
                            else -> "NutriTrack"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeoDarkText
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GeoBackgroundLight,
                    titleContentColor = GeoDarkText
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = GeoNavBg,
                tonalElevation = 0.dp
            ) {
                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GeoOnPrimaryContainerLight,
                            selectedTextColor = GeoOnPrimaryContainerLight,
                            indicatorColor = GeoNavIndicator,
                            unselectedIconColor = GeoOnSurfaceVariantLight,
                            unselectedTextColor = GeoMutedText
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        },
        containerColor = GeoBackgroundLight,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToAdvice = {
                        navController.navigate(Screen.Advice.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Advice.route) {
                AdviceScreen(viewModel = viewModel)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = viewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = viewModel)
            }
        }
    }
}
