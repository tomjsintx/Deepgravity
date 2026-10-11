package com.example.model

data class SessionItem(
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val workspacePath: String = "workspace",
    val boundModelId: String = "gemini-3.1-pro-preview",
    val isArchived: Boolean = false,
    val messageCount: Int = 0,
    val estimatedTokens: Int = 0,
    val lastCompactionSummary: String? = null
)

data class CompactionSummary(
    val decisionsMade: List<String>,
    val filesChanged: List<String>,
    val unresolvedTasks: List<String>,
    val testResultsSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)
