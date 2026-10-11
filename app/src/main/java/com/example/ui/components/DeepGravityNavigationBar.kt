package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.L10n
import com.example.model.AppLanguage
import com.example.viewmodel.AppScreen

data class NavItem(
    val screen: AppScreen,
    val icon: ImageVector,
    val labelEn: String,
    val labelVi: String,
    val testTag: String
)

val NAV_ITEMS = listOf(
    NavItem(AppScreen.CHAT, Icons.Default.Chat, "Chat", "Chat", "nav_chat"),
    NavItem(AppScreen.WORKSPACE, Icons.Default.FolderZip, "Workspace", "Code", "nav_workspace"),
    NavItem(AppScreen.COMPUTER, Icons.Default.Devices, "Computer", "Máy tính", "nav_computer"),
    NavItem(AppScreen.TERMINAL, Icons.Default.Terminal, "Terminal", "Lệnh", "nav_terminal"),
    NavItem(AppScreen.TOOLS, Icons.Default.Extension, "Tools", "MCP", "nav_tools"),
    NavItem(AppScreen.EXTENSIONS, Icons.Default.AutoAwesomeMosaic, "Skills", "Plugins", "nav_extensions"),
    NavItem(AppScreen.PROVIDERS, Icons.Default.Cloud, "Providers", "Mô hình", "nav_providers"),
    NavItem(AppScreen.AUTOMATION, Icons.Default.SmartToy, "Automation", "Tự động", "nav_automation"),
    NavItem(AppScreen.SETTINGS, Icons.Default.Settings, "Settings", "Cài đặt", "nav_settings")
)

@Composable
fun DeepGravityNavigationBar(
    currentScreen: AppScreen,
    language: AppLanguage,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NAV_ITEMS.forEach { item ->
                val selected = item.screen == currentScreen
                val label = if (language == AppLanguage.VI) item.labelVi else item.labelEn

                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.screen) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = label,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.testTag(item.testTag)
                )
            }
        }
    }
}
