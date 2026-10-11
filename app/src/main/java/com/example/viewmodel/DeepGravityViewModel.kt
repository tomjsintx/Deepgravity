package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.AgentExecutionEngine
import com.example.agent.AgentState
import com.example.agent.AgentStepLog
import com.example.compute.BackendRegistry
import com.example.extensions.PluginManager
import com.example.extensions.SkillPluginManifest
import com.example.model.*
import com.example.repository.AgentRepository
import com.example.security.PendingApprovalAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreen(val route: String) {
    CHAT("chat"),
    WORKSPACE("workspace"),
    COMPUTER("computer"),
    TERMINAL("terminal"),
    TOOLS("tools"),
    EXTENSIONS("extensions"),
    PROVIDERS("providers"),
    AUTOMATION("automation"),
    SETTINGS("settings")
}

class DeepGravityViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: AgentRepository = AgentRepository(application)
    private val pluginManager: PluginManager = PluginManager()
    private val backendRegistry: BackendRegistry = BackendRegistry(
        java.io.File(application.filesDir, "workspace").apply { if (!exists()) mkdirs() }
    )

    private val _currentScreen = MutableStateFlow(AppScreen.CHAT)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val messages: StateFlow<List<ChatMessage>> = repository.messages
    val files: StateFlow<List<WorkspaceFile>> = repository.files
    val activeFile: StateFlow<WorkspaceFile?> = repository.activeFile
    val activeDiff: StateFlow<CodeDiff?> = repository.activeDiff
    val buildStatus: StateFlow<ProjectBuildStatus> = repository.buildStatus
    val terminalEntries: StateFlow<List<TerminalEntry>> = repository.terminalEntries
    val computerActions: StateFlow<List<ComputerAction>> = repository.computerActions
    val shizukuStatus: StateFlow<ShizukuStatus> = repository.shizukuStatus
    val mcpTools: StateFlow<List<McpToolItem>> = repository.mcpTools
    val providers: StateFlow<List<AiProviderItem>> = repository.providers
    val selectedProviderId: StateFlow<String> = repository.selectedProviderId
    val selectedModelId: StateFlow<String> = repository.selectedModelId
    val appLanguage: StateFlow<AppLanguage> = repository.appLanguage
    val themeMode: StateFlow<AppThemeMode> = repository.themeMode
    val thinkingLevel: StateFlow<ThinkingLevel> = repository.thinkingLevel
    val safeModeEnabled: StateFlow<Boolean> = repository.safeModeEnabled

    private val _agentState = MutableStateFlow(AgentState.IDLE)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()

    private val _pendingApproval = MutableStateFlow<PendingApprovalAction?>(null)
    val pendingApproval: StateFlow<PendingApprovalAction?> = _pendingApproval.asStateFlow()

    private val _plugins = MutableStateFlow<List<SkillPluginManifest>>(pluginManager.getAllPlugins())
    val plugins: StateFlow<List<SkillPluginManifest>> = _plugins.asStateFlow()

    private val _activeBackendName = MutableStateFlow(backendRegistry.getActiveBackend().name)
    val activeBackendName: StateFlow<String> = _activeBackendName.asStateFlow()

    private val _activeBackendType = MutableStateFlow(backendRegistry.getActiveBackend().type)
    val activeBackendType = _activeBackendType.asStateFlow()

    private val _activeBackendHasGui = MutableStateFlow(backendRegistry.getActiveBackend().capabilities.hasGuiDesktop)
    val activeBackendHasGui: StateFlow<Boolean> = _activeBackendHasGui.asStateFlow()

    private val _activeBackendConnected = MutableStateFlow(backendRegistry.getActiveBackend().isConnected)
    val activeBackendConnected: StateFlow<Boolean> = _activeBackendConnected.asStateFlow()

    private val _toolTestResult = MutableStateFlow<String?>(null)
    val toolTestResult: StateFlow<String?> = _toolTestResult.asStateFlow()

    // Autonomous Modules Flows (Requirements 11-20)
    val sessions = repository.sessions
    val activeSession = repository.activeSession
    val auditRecords = repository.auditRecords
    val automationMode = repository.automationMode
    val emergencyStopActive = repository.emergencyStopActive
    val gitStatus = repository.gitStatus

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun switchSession(sessionId: String) {
        repository.switchSession(sessionId)
    }

    fun createNewSession(title: String) {
        repository.createNewSession(title, selectedModelId.value)
    }

    fun toggleAutomationMode() {
        repository.toggleAutomationMode()
    }

    fun triggerEmergencyStop() {
        repository.triggerEmergencyStop()
    }

    fun resetEmergencyStop() {
        repository.resetEmergencyStop()
    }

    fun runGitCommand(cmd: String): String {
        return repository.runGitCommand(cmd)
    }

    fun optimizePrompt(prompt: String): com.example.llm.OptimizedPromptResult {
        return repository.optimizePrompt(prompt)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        _agentState.value = AgentState.THINKING
        viewModelScope.launch {
            repository.sendUserMessage(text)
            _agentState.value = AgentState.COMPLETED
        }
    }

    fun cancelAgent() {
        _agentState.value = AgentState.CANCELLED
        _pendingApproval.value = null
    }

    fun selectFile(file: WorkspaceFile) {
        repository.selectFile(file)
    }

    fun updateFileContent(content: String) {
        repository.updateActiveFileContent(content)
    }

    fun createNewFile(path: String, content: String) {
        repository.createNewFile(path, content)
    }

    fun deleteFile(path: String) {
        repository.deleteFile(path)
    }

    fun executeTerminalCommand(command: String) {
        if (command.isBlank()) return
        viewModelScope.launch {
            repository.runTerminalCommand(command)
        }
    }

    fun triggerBuild() {
        viewModelScope.launch {
            repository.triggerBuild()
        }
    }

    fun triggerComputerAction(type: ComputerActionType, target: String, detail: String) {
        repository.triggerComputerAction(type, target, detail)
    }

    fun testMcpTool(toolId: String, inputJson: String) {
        viewModelScope.launch {
            val result = repository.testExecuteMcpTool(toolId, inputJson)
            _toolTestResult.value = result
        }
    }

    fun clearToolTestResult() {
        _toolTestResult.value = null
    }

    fun toggleMcpTool(toolId: String) {
        repository.toggleMcpTool(toolId)
    }

    fun togglePlugin(id: String) {
        pluginManager.togglePlugin(id)
        _plugins.value = pluginManager.getAllPlugins()
    }

    fun installPlugin(manifest: SkillPluginManifest) {
        pluginManager.installFromManifest(manifest)
        _plugins.value = pluginManager.getAllPlugins()
    }

    fun selectModel(providerId: String, modelId: String) {
        repository.selectModel(providerId, modelId)
    }

    fun saveApiKey(providerId: String, apiKey: String) {
        repository.saveProviderApiKey(providerId, apiKey)
    }

    fun setLanguage(language: AppLanguage) {
        repository.setLanguage(language)
    }

    fun setThemeMode(theme: AppThemeMode) {
        repository.setThemeMode(theme)
    }

    fun setThinkingLevel(level: ThinkingLevel) {
        repository.setThinkingLevel(level)
    }

    fun setSafeMode(enabled: Boolean) {
        repository.setSafeMode(enabled)
    }
}
