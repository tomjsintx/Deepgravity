package com.example.model

enum class MessageSender {
    USER,
    AGENT,
    SYSTEM,
    TOOL
}

enum class ToolStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    ERROR
}

data class ToolInvocation(
    val id: String,
    val toolName: String,
    val summary: String,
    val arguments: String,
    val result: String,
    val status: ToolStatus = ToolStatus.SUCCESS,
    val durationMs: Long = 120
)

data class CodeSnippet(
    val language: String,
    val title: String,
    val code: String,
    val targetPath: String? = null
)

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val thinkingProcess: String? = null,
    val thinkingExpanded: Boolean = false,
    val toolInvocations: List<ToolInvocation> = emptyList(),
    val codeSnippets: List<CodeSnippet> = emptyList(),
    val modelUsed: String? = null,
    val tokensUsed: Int? = null,
    val latencyMs: Long? = null
)
