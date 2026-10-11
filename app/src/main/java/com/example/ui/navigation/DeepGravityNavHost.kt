package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.model.AppLanguage
import com.example.model.AppThemeMode
import com.example.model.ThinkingLevel
import com.example.ui.screens.*
import com.example.viewmodel.DeepGravityViewModel

/**
 * Jetpack Compose NavHost managing navigation routes for DeepGravity screens:
 * - Agent Chat
 * - Workspace
 * - Terminal
 * - Computer
 * - Tools
 * - Providers
 * - Settings
 * - Extensions
 * - Automation
 */
@Composable
fun DeepGravityNavHost(
    navController: NavHostController,
    viewModel: DeepGravityViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val thinkingLevel by viewModel.thinkingLevel.collectAsStateWithLifecycle()
    val selectedModelId by viewModel.selectedModelId.collectAsStateWithLifecycle()
    val shizukuStatus by viewModel.shizukuStatus.collectAsStateWithLifecycle()
    val safeModeEnabled by viewModel.safeModeEnabled.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.ROUTE_CHAT,
        modifier = modifier
    ) {
        // Agent Chat Screen
        composable(NavRoutes.ROUTE_CHAT) {
            val messages by viewModel.messages.collectAsStateWithLifecycle()
            val agentState by viewModel.agentState.collectAsStateWithLifecycle()
            val pendingApproval by viewModel.pendingApproval.collectAsStateWithLifecycle()

            AgentChatScreen(
                messages = messages,
                activeModel = selectedModelId,
                language = language,
                thinkingLevel = thinkingLevel,
                agentState = agentState,
                pendingApproval = pendingApproval,
                onSendMessage = { prompt ->
                    viewModel.sendMessage(prompt)
                },
                onCancelAgent = {
                    viewModel.cancelAgent()
                },
                onApplyCodeToWorkspace = { code, path ->
                    val targetFile = viewModel.files.value.find { it.path == path }
                    if (targetFile != null) {
                        viewModel.selectFile(targetFile)
                        viewModel.updateFileContent(code)
                    } else {
                        viewModel.updateFileContent(code)
                    }
                    navController.navigate(NavRoutes.ROUTE_WORKSPACE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Workspace Screen
        composable(NavRoutes.ROUTE_WORKSPACE) {
            val files by viewModel.files.collectAsStateWithLifecycle()
            val activeFile by viewModel.activeFile.collectAsStateWithLifecycle()
            val activeDiff by viewModel.activeDiff.collectAsStateWithLifecycle()
            val buildStatus by viewModel.buildStatus.collectAsStateWithLifecycle()

            WorkspaceScreen(
                files = files,
                activeFile = activeFile,
                activeDiff = activeDiff,
                buildStatus = buildStatus,
                language = language,
                onSelectFile = { file ->
                    viewModel.selectFile(file)
                },
                onUpdateContent = { newContent ->
                    viewModel.updateFileContent(newContent)
                },
                onCreateNewFile = { path, content ->
                    viewModel.createNewFile(path, content)
                },
                onDeleteFile = { path ->
                    viewModel.deleteFile(path)
                },
                onTriggerBuild = {
                    viewModel.triggerBuild()
                }
            )
        }

        // Terminal Screen
        composable(NavRoutes.ROUTE_TERMINAL) {
            val entries by viewModel.terminalEntries.collectAsStateWithLifecycle()
            TerminalScreen(
                entries = entries,
                language = language,
                onExecuteCommand = { cmd ->
                    viewModel.executeTerminalCommand(cmd)
                }
            )
        }

        // Computer Simulation Screen
        composable(NavRoutes.ROUTE_COMPUTER) {
            val backendName by viewModel.activeBackendName.collectAsStateWithLifecycle()
            val backendType by viewModel.activeBackendType.collectAsStateWithLifecycle()
            val hasGui by viewModel.activeBackendHasGui.collectAsStateWithLifecycle()
            val isConnected by viewModel.activeBackendConnected.collectAsStateWithLifecycle()

            ComputerScreen(
                activeBackendName = backendName,
                backendType = backendType,
                hasGuiDesktop = hasGui,
                isConnected = isConnected,
                language = language,
                onNavigateToTerminal = {
                    navController.navigate(NavRoutes.ROUTE_TERMINAL) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProviders = {
                    navController.navigate(NavRoutes.ROUTE_PROVIDERS) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Tools Screen (MCP)
        composable(NavRoutes.ROUTE_TOOLS) {
            val tools by viewModel.mcpTools.collectAsStateWithLifecycle()
            val testResult by viewModel.toolTestResult.collectAsStateWithLifecycle()

            ToolsScreen(
                tools = tools,
                testResult = testResult,
                language = language,
                onToggleTool = { toolId ->
                    viewModel.toggleMcpTool(toolId)
                },
                onTestTool = { toolId, inputJson ->
                    viewModel.testMcpTool(toolId, inputJson)
                },
                onClearTestResult = {
                    viewModel.clearToolTestResult()
                }
            )
        }

        // Providers Screen
        composable(NavRoutes.ROUTE_PROVIDERS) {
            val providers by viewModel.providers.collectAsStateWithLifecycle()
            val selectedProviderId by viewModel.selectedProviderId.collectAsStateWithLifecycle()

            ProvidersScreen(
                providers = providers,
                selectedProviderId = selectedProviderId,
                selectedModelId = selectedModelId,
                language = language,
                onSelectModel = { providerId, modelId ->
                    viewModel.selectModel(providerId, modelId)
                },
                onSaveApiKey = { providerId, apiKey ->
                    viewModel.saveApiKey(providerId, apiKey)
                }
            )
        }

        // Settings Screen
        composable(NavRoutes.ROUTE_SETTINGS) {
            SettingsScreen(
                currentLanguage = language,
                currentTheme = themeMode,
                currentThinkingLevel = thinkingLevel,
                safeModeEnabled = safeModeEnabled,
                onLanguageChange = { newLang ->
                    viewModel.setLanguage(newLang)
                },
                onThemeChange = { newTheme ->
                    viewModel.setThemeMode(newTheme)
                },
                onThinkingLevelChange = { newLevel ->
                    viewModel.setThinkingLevel(newLevel)
                },
                onSafeModeChange = { enabled ->
                    viewModel.setSafeMode(enabled)
                }
            )
        }

        // Extensions / Skills Screen
        composable(NavRoutes.ROUTE_EXTENSIONS) {
            val plugins by viewModel.plugins.collectAsStateWithLifecycle()
            ExtensionsScreen(
                plugins = plugins,
                language = language,
                onTogglePlugin = { pluginId ->
                    viewModel.togglePlugin(pluginId)
                },
                onInstallPlugin = { manifest ->
                    viewModel.installPlugin(manifest)
                }
            )
        }

        // Device Automation Screen
        composable(NavRoutes.ROUTE_AUTOMATION) {
            val actions by viewModel.computerActions.collectAsStateWithLifecycle()
            val automationMode by viewModel.automationMode.collectAsStateWithLifecycle()
            val emergencyStopped by viewModel.emergencyStopActive.collectAsStateWithLifecycle()

            AutomationScreen(
                actions = actions,
                shizukuStatus = shizukuStatus,
                language = language,
                mode = automationMode,
                emergencyStopped = emergencyStopped,
                onToggleMode = { viewModel.toggleAutomationMode() },
                onEmergencyStop = { viewModel.triggerEmergencyStop() },
                onResetEmergencyStop = { viewModel.resetEmergencyStop() },
                onTriggerAction = { type, target, detail ->
                    viewModel.triggerComputerAction(type, target, detail)
                }
            )
        }
    }
}
