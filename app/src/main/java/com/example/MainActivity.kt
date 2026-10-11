package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.*
import com.example.ui.components.DeepGravityNavigationBar
import com.example.ui.components.DeepGravityTopBar
import com.example.ui.navigation.DeepGravityNavHost
import com.example.ui.navigation.NavRoutes
import com.example.ui.theme.DeepGravityTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.DeepGravityViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: DeepGravityViewModel = viewModel()
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.ROUTE_CHAT
            val currentScreen = NavRoutes.toAppScreen(currentRoute)

            val language by viewModel.appLanguage.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val selectedModelId by viewModel.selectedModelId.collectAsStateWithLifecycle()
            val shizukuStatus by viewModel.shizukuStatus.collectAsStateWithLifecycle()

            // Keep ViewModel in sync with active route
            LaunchedEffect(currentScreen) {
                viewModel.navigateTo(currentScreen)
            }

            val isDark = when (themeMode) {
                AppThemeMode.DARK, AppThemeMode.CYBER_OLED -> true
                AppThemeMode.LIGHT -> false
            }

            DeepGravityTheme(darkTheme = isDark) {
                BackHandler(enabled = currentRoute != NavRoutes.ROUTE_CHAT) {
                    navController.popBackStack(NavRoutes.ROUTE_CHAT, inclusive = false)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        DeepGravityTopBar(
                            activeModel = selectedModelId,
                            shizukuStatus = shizukuStatus,
                            currentLanguage = language,
                            onLanguageToggle = {
                                val nextLang = if (language == AppLanguage.EN) AppLanguage.VI else AppLanguage.EN
                                viewModel.setLanguage(nextLang)
                            },
                            onOpenSettings = {
                                navController.navigate(NavRoutes.ROUTE_SETTINGS) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    },
                    bottomBar = {
                        DeepGravityNavigationBar(
                            currentScreen = currentScreen,
                            language = language,
                            onNavigate = { screen ->
                                val targetRoute = NavRoutes.fromAppScreen(screen)
                                navController.navigate(targetRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        DeepGravityNavHost(
                            navController = navController,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
