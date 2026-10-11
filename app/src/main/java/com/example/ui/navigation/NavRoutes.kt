package com.example.ui.navigation

import com.example.viewmodel.AppScreen

/**
 * Route constants for the Jetpack Compose navigation graph in DeepGravity.
 */
object NavRoutes {
    const val ROUTE_CHAT = "chat"
    const val ROUTE_WORKSPACE = "workspace"
    const val ROUTE_TERMINAL = "terminal"
    const val ROUTE_COMPUTER = "computer"
    const val ROUTE_TOOLS = "tools"
    const val ROUTE_PROVIDERS = "providers"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_EXTENSIONS = "extensions"
    const val ROUTE_AUTOMATION = "automation"

    fun fromAppScreen(screen: AppScreen): String = when (screen) {
        AppScreen.CHAT -> ROUTE_CHAT
        AppScreen.WORKSPACE -> ROUTE_WORKSPACE
        AppScreen.TERMINAL -> ROUTE_TERMINAL
        AppScreen.COMPUTER -> ROUTE_COMPUTER
        AppScreen.TOOLS -> ROUTE_TOOLS
        AppScreen.PROVIDERS -> ROUTE_PROVIDERS
        AppScreen.SETTINGS -> ROUTE_SETTINGS
        AppScreen.EXTENSIONS -> ROUTE_EXTENSIONS
        AppScreen.AUTOMATION -> ROUTE_AUTOMATION
    }

    fun toAppScreen(route: String?): AppScreen = when (route) {
        ROUTE_CHAT -> AppScreen.CHAT
        ROUTE_WORKSPACE -> AppScreen.WORKSPACE
        ROUTE_TERMINAL -> AppScreen.TERMINAL
        ROUTE_COMPUTER -> AppScreen.COMPUTER
        ROUTE_TOOLS -> AppScreen.TOOLS
        ROUTE_PROVIDERS -> AppScreen.PROVIDERS
        ROUTE_SETTINGS -> AppScreen.SETTINGS
        ROUTE_EXTENSIONS -> AppScreen.EXTENSIONS
        ROUTE_AUTOMATION -> AppScreen.AUTOMATION
        else -> AppScreen.CHAT
    }
}
