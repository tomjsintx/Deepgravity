package com.example.repository

import com.example.model.CompactionSummary
import com.example.model.SessionItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class SessionManager(private val baseDir: File) {

    private val sessions = mutableListOf<SessionItem>()
    private var activeSessionId: String = "session_default"

    init {
        // Default initial session
        sessions.add(
            SessionItem(
                id = "session_default",
                title = "Main Architecture Session",
                workspacePath = "workspace",
                boundModelId = "gemini-3.1-pro-preview",
                messageCount = 4,
                estimatedTokens = 1250,
                lastCompactionSummary = "Implemented DeepGravity core MVVM architecture and verified clean compilation."
            )
        )
    }

    fun getActiveSession(): SessionItem {
        return sessions.find { it.id == activeSessionId } ?: sessions.first()
    }

    fun getAllSessions(): List<SessionItem> = sessions.toList()

    fun switchSession(sessionId: String): Boolean {
        if (sessions.any { it.id == sessionId }) {
            activeSessionId = sessionId
            return true
        }
        return false
    }

    fun createNewSession(title: String, modelId: String = "gemini-3.1-pro-preview"): SessionItem {
        val newSession = SessionItem(
            id = "session_" + UUID.randomUUID().toString().substring(0, 8),
            title = title,
            boundModelId = modelId,
            messageCount = 0,
            estimatedTokens = 0
        )
        sessions.add(0, newSession)
        activeSessionId = newSession.id
        return newSession
    }

    fun renameSession(sessionId: String, newTitle: String): Boolean {
        val idx = sessions.indexOfFirst { it.id == sessionId }
        if (idx != -1) {
            val curr = sessions[idx]
            sessions[idx] = curr.copy(title = newTitle, updatedAt = System.currentTimeMillis())
            return true
        }
        return false
    }

    fun toggleArchiveSession(sessionId: String): Boolean {
        val idx = sessions.indexOfFirst { it.id == sessionId }
        if (idx != -1) {
            val curr = sessions[idx]
            sessions[idx] = curr.copy(isArchived = !curr.isArchived, updatedAt = System.currentTimeMillis())
            return true
        }
        return false
    }

    fun deleteSession(sessionId: String): Boolean {
        if (sessions.size <= 1) return false // Keep at least one session
        val removed = sessions.removeIf { it.id == sessionId }
        if (activeSessionId == sessionId) {
            activeSessionId = sessions.first().id
        }
        return removed
    }

    fun exportSessionAsMarkdown(sessionId: String): String {
        val session = sessions.find { it.id == sessionId } ?: getActiveSession()
        return """
            # DeepGravity Session Export: ${session.title}
            - **Session ID:** ${session.id}
            - **Model:** ${session.boundModelId}
            - **Workspace:** ${session.workspacePath}
            - **Tokens (Estimated):** ${session.estimatedTokens}
            - **Summary:** ${session.lastCompactionSummary ?: "No compaction"}
            
            ---
            *Exported safely with zero credential leakage.*
        """.trimIndent()
    }

    fun generateCompactionSummary(
        decisions: List<String>,
        filesChanged: List<String>,
        unresolved: List<String>,
        testResults: String
    ): CompactionSummary {
        val summary = CompactionSummary(
            decisionsMade = decisions,
            filesChanged = filesChanged,
            unresolvedTasks = unresolved,
            testResultsSummary = testResults
        )

        val idx = sessions.indexOfFirst { it.id == activeSessionId }
        if (idx != -1) {
            val s = sessions[idx]
            val briefText = "Decisions: ${decisions.size} | Files: ${filesChanged.size} | Tests: $testResults"
            sessions[idx] = s.copy(lastCompactionSummary = briefText)
        }
        return summary
    }

    // Token estimation according to provider model characteristics (Requirement 17)
    fun estimateTokens(charCount: Int, modelId: String): Int {
        val ratio = when {
            modelId.contains("gemini") -> 4.2
            modelId.contains("claude") -> 3.8
            modelId.contains("deepseek") -> 3.5
            else -> 4.0
        }
        return (charCount / ratio).toInt().coerceAtLeast(1)
    }
}
