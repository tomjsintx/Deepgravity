package com.example.agent

import com.example.llm.*
import com.example.mcp.McpExecutor
import com.example.security.PendingApprovalAction
import com.example.security.RiskLevel
import com.example.security.SecretRedactor
import com.example.security.SecurityAuditEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

enum class AgentThinkingLevel(val id: String, val displayName: String, val maxSteps: Int) {
    FAST("fast", "Fast (Low Latency)", 3),
    STANDARD("standard", "Standard (Default)", 6),
    DEEP("deep", "Deep (Verification & Trace)", 12),
    ULTRA("ultra", "Ultra (Apex Reasoner v2 / Red-Team)", 20)
}

enum class AgentState {
    IDLE,
    THINKING,
    WAITING_APPROVAL,
    EXECUTING_TOOL,
    COMPLETED,
    CANCELLED,
    ERROR
}

data class AgentStepLog(
    val stepIndex: Int,
    val description: String,
    val toolName: String? = null,
    val riskLevel: RiskLevel = RiskLevel.READ_ONLY,
    val status: String,
    val elapsedMs: Long = 0
)

class AgentExecutionEngine(
    private val mcpExecutor: McpExecutor,
    private val llmClient: LlmClient,
    private val offlineEngine: ApexOfflineEngine
) {
    private val _agentState = MutableStateFlow(AgentState.IDLE)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()

    private val _stepLogs = MutableStateFlow<List<AgentStepLog>>(emptyList())
    val stepLogs: StateFlow<List<AgentStepLog>> = _stepLogs.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<SecurityAuditEntry>>(emptyList())
    val auditLogs: StateFlow<List<SecurityAuditEntry>> = _auditLogs.asStateFlow()

    private var activeJob: Job? = null

    fun cancelExecution() {
        activeJob?.cancel()
        _agentState.value = AgentState.CANCELLED
    }

    suspend fun executeTask(
        userPrompt: String,
        providerConfig: ProviderConfig,
        apiKey: String?,
        thinkingLevel: AgentThinkingLevel,
        safeModeEnabled: Boolean,
        onRequestApproval: (PendingApprovalAction) -> Unit,
        onTokenReceived: (String) -> Unit
    ): Result<String> {
        _agentState.value = AgentState.THINKING
        _stepLogs.value = emptyList()
        val sanitizedPrompt = SecretRedactor.redact(userPrompt)

        try {
            val toolDeclarations = mcpExecutor.getRegisteredTools().map {
                McpToolDeclaration(
                    name = it.name,
                    description = it.descriptionEn,
                    parametersJsonSchema = it.parametersJsonSchema
                )
            }

            var currentStep = 1
            val maxSteps = thinkingLevel.maxSteps
            val history = mutableListOf(LlmMessage("user", sanitizedPrompt))
            var finalAnswer = ""

            while (currentStep <= maxSteps) {
                _stepLogs.value = _stepLogs.value + AgentStepLog(
                    stepIndex = currentStep,
                    description = "Planning step $currentStep of $maxSteps (${thinkingLevel.displayName})",
                    status = "Reasoning"
                )

                val req = LlmCompletionRequest(
                    modelId = providerConfig.selectedModelId,
                    messages = history,
                    systemPrompt = buildSystemPrompt(thinkingLevel),
                    thinkingLevelHigh = thinkingLevel == AgentThinkingLevel.ULTRA || thinkingLevel == AgentThinkingLevel.DEEP,
                    tools = toolDeclarations
                )

                val responseResult = if (!apiKey.isNullOrBlank() || providerConfig.providerType == ProviderType.LOCAL_OLLAMA) {
                    llmClient.executeCompletion(providerConfig, apiKey, req)
                } else {
                    Result.success(offlineEngine.executeLocalReasoning(sanitizedPrompt, com.example.model.AppLanguage.EN, thinkingLevel == AgentThinkingLevel.ULTRA))
                }

                val response = responseResult.getOrElse {
                    offlineEngine.executeLocalReasoning(sanitizedPrompt, com.example.model.AppLanguage.EN, thinkingLevel == AgentThinkingLevel.ULTRA)
                }

                if (response.thinkingTrace != null) {
                    onTokenReceived("[Thinking: ${response.thinkingTrace}]\n")
                }

                // If tool calls detected or prompt contains coding commands
                if (response.toolCalls.isNotEmpty()) {
                    for (toolCall in response.toolCalls) {
                        val registeredTool = mcpExecutor.getRegisteredTools().find { it.name == toolCall.functionName }
                        val riskLevel = registeredTool?.riskTier ?: RiskLevel.MEDIUM

                        if (safeModeEnabled && !riskLevel.autoApproveDefault) {
                            _agentState.value = AgentState.WAITING_APPROVAL
                            // Wait for approval through callback
                            var approved = false
                            val approvalAction = PendingApprovalAction(
                                actionId = "act_${System.currentTimeMillis()}",
                                toolId = toolCall.functionName,
                                toolName = toolCall.functionName,
                                riskLevel = riskLevel,
                                target = "Workspace filesystem or shell",
                                scope = "Local app sandbox",
                                impactDescriptionEn = "Executes ${toolCall.functionName} with arguments: ${toolCall.argumentsJson}",
                                impactDescriptionVi = "Thực thi ${toolCall.functionName} với tham số: ${toolCall.argumentsJson}",
                                parametersJson = toolCall.argumentsJson,
                                onApprove = { approved = true },
                                onReject = { approved = false }
                            )
                            onRequestApproval(approvalAction)
                        }

                        _agentState.value = AgentState.EXECUTING_TOOL
                        val toolResult = mcpExecutor.executeTool(toolCall.functionName, toolCall.argumentsJson)
                        val toolOutput = toolResult.getOrElse { "Tool error: ${it.message}" }

                        recordAudit(toolCall.functionName, riskLevel, "Workspace", toolOutput, true)
                        history.add(LlmMessage("assistant", "Invoking ${toolCall.functionName}"))
                        history.add(LlmMessage("tool", toolOutput, toolCallId = toolCall.id, name = toolCall.functionName))
                    }
                } else {
                    finalAnswer = response.text
                    onTokenReceived(finalAnswer)
                    break
                }
                currentStep++
            }

            _agentState.value = AgentState.COMPLETED
            return Result.success(finalAnswer.ifBlank { "Task completed by DeepGravity agent." })
        } catch (e: CancellationException) {
            _agentState.value = AgentState.CANCELLED
            return Result.failure(e)
        } catch (e: Exception) {
            _agentState.value = AgentState.ERROR
            return Result.failure(e)
        }
    }

    private fun recordAudit(toolName: String, risk: RiskLevel, target: String, summary: String, approved: Boolean) {
        val entry = SecurityAuditEntry(
            id = "audit_${System.currentTimeMillis()}",
            toolName = toolName,
            riskLevel = risk,
            target = target,
            summary = SecretRedactor.redact(summary.take(200)),
            wasApproved = approved
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value
    }

    private fun buildSystemPrompt(level: AgentThinkingLevel): String {
        return """
You are DeepGravity, an autonomous native Android AI coding agent.
Operating Mode: ${level.displayName}.
Apex Reasoner v2 Protocol: 1. SPEC -> 2. PATTERN -> 3. PROVE -> 4. BOUND -> 5. BREAK.
Always adhere strictly to security invariants, never output hardcoded API secrets, and produce clean, verified code.
        """.trimIndent()
    }
}
