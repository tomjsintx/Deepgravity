package com.example.llm

enum class ProviderType(
    val id: String,
    val displayName: String,
    val defaultBaseUrl: String,
    val headerAuthKey: String,
    val authPrefix: String,
    val requiresApiKey: Boolean = true
) {
    GEMINI("gemini", "Google Gemini", "https://generativelanguage.googleapis.com/v1beta", "x-goog-api-key", ""),
    OPENAI("openai", "OpenAI", "https://api.openai.com/v1", "Authorization", "Bearer "),
    CLAUDE("claude", "Anthropic Claude", "https://api.anthropic.com/v1", "x-api-key", ""),
    DEEPSEEK("deepseek", "DeepSeek", "https://api.deepseek.com/v1", "Authorization", "Bearer "),
    GLM("glm", "Zhipu GLM", "https://open.bigmodel.cn/api/paas/v4", "Authorization", "Bearer "),
    MISTRAL("mistral", "Mistral AI", "https://api.mistral.ai/v1", "Authorization", "Bearer "),
    OPENROUTER("openrouter", "OpenRouter", "https://openrouter.ai/api/v1", "Authorization", "Bearer "),
    XKIRO("xkiro", "xKiro / Moonshot", "https://api.moonshot.cn/v1", "Authorization", "Bearer "),
    LOCAL_OLLAMA("ollama", "Local Ollama / vLLM", "http://127.0.0.1:11434", "", "", requiresApiKey = false),
    CUSTOM("custom", "Custom BYO Endpoint", "", "Authorization", "Bearer ")
}

data class ModelSpec(
    val modelId: String,
    val displayName: String,
    val contextWindowTokens: Int,
    val maxOutputTokens: Int,
    val supportsStreaming: Boolean = true,
    val supportsToolCalling: Boolean = true,
    val supportsMultimodal: Boolean = false,
    val supportsThinking: Boolean = false,
    val description: String
)

data class ProviderConfig(
    val providerType: ProviderType,
    val customBaseUrl: String? = null,
    val customModelId: String? = null,
    val selectedModelId: String,
    val availableModels: List<ModelSpec>,
    val isEnabled: Boolean = true
) {
    val effectiveBaseUrl: String
        get() = if (!customBaseUrl.isNullOrBlank()) customBaseUrl else providerType.defaultBaseUrl
}

data class LlmMessage(
    val role: String, // "user", "assistant", "system", "tool"
    val content: String,
    val toolCallId: String? = null,
    val name: String? = null
)

data class LlmCompletionRequest(
    val modelId: String,
    val messages: List<LlmMessage>,
    val systemPrompt: String? = null,
    val temperature: Float = 0.7f,
    val thinkingLevelHigh: Boolean = false,
    val tools: List<McpToolDeclaration> = emptyList()
)

data class McpToolDeclaration(
    val name: String,
    val description: String,
    val parametersJsonSchema: String
)

data class LlmCompletionResponse(
    val text: String,
    val thinkingTrace: String? = null,
    val toolCalls: List<LlmToolCall> = emptyList(),
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val finishReason: String? = null,
    val latencyMs: Long = 0
)

data class LlmToolCall(
    val id: String,
    val functionName: String,
    val argumentsJson: String
)
