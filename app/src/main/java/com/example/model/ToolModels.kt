package com.example.model

enum class ToolCategory(val titleEn: String, val titleVi: String) {
    FILESYSTEM("Filesystem", "Hệ thống Tệp"),
    EXECUTION("Execution & Shell", "Thực thi & Shell"),
    GIT("Version Control", "Quản lý Phiên bản"),
    DEVICE("Device & ADB", "Thiết bị & ADB"),
    MCP("Model Context Protocol", "Giao thức MCP"),
    INTELLIGENCE("AI Reasoning", "Trí tuệ Nhân tạo")
}

data class McpToolItem(
    val id: String,
    val name: String,
    val category: ToolCategory,
    val descriptionEn: String,
    val descriptionVi: String,
    val parametersJson: String,
    val sampleInputJson: String,
    val isEnabled: Boolean = true,
    val executionCount: Int = 0
)

enum class ProviderStatus {
    READY,
    CONFIG_REQUIRED,
    OFFLINE
}

data class ModelOption(
    val modelId: String,
    val displayName: String,
    val contextWindow: String,
    val supportsThinking: Boolean = false,
    val isFastLite: Boolean = false,
    val description: String
)

data class AiProviderItem(
    val id: String,
    val name: String,
    val baseUrl: String,
    val status: ProviderStatus,
    val isLocal: Boolean,
    val models: List<ModelOption>,
    val selectedModelId: String,
    val hasApiKey: Boolean,
    val pingMs: Long? = null
)
