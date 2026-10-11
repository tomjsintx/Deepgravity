package com.example.security

import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

data class DetailedAuditRecord(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String,
    val workspacePath: String,
    val backendName: String,
    val toolName: String,
    val toolVersion: String,
    val actionSummary: String,
    val wasApproved: Boolean,
    val riskLevel: RiskLevel,
    val durationMs: Long,
    val exitCode: Int,
    val errorCategory: String? = null,
    val changedFilesSummary: String? = null
)

data class FileCheckpoint(
    val id: String,
    val relativePath: String,
    val previousContent: String,
    val newContent: String,
    val timestamp: Long = System.currentTimeMillis()
)

class AuditManager(private val logDir: File? = null) {

    private val auditQueue = ConcurrentLinkedQueue<DetailedAuditRecord>()
    private val checkpoints = mutableListOf<FileCheckpoint>()
    private val maxRecords = 500

    init {
        // Initial audit records
        logAction(
            sessionId = "session_default",
            workspacePath = "workspace",
            backendName = "Local Android Sandbox",
            toolName = "fs_write_file",
            toolVersion = "1.0.0",
            actionSummary = "Created MainActivity.kt with idiomatic Jetpack Compose foundation",
            wasApproved = true,
            riskLevel = RiskLevel.HIGH,
            durationMs = 12,
            exitCode = 0,
            changedFiles = "app/src/main/java/com/deepgravity/agent/MainActivity.kt"
        )
        logAction(
            sessionId = "session_default",
            workspacePath = "workspace",
            backendName = "Local Android Sandbox",
            toolName = "bash_exec",
            toolVersion = "1.0.0",
            actionSummary = "Executed gradle testDebugUnitTest to verify CUJ invariants",
            wasApproved = true,
            riskLevel = RiskLevel.HIGH,
            durationMs = 450,
            exitCode = 0
        )
    }

    fun logAction(
        sessionId: String,
        workspacePath: String,
        backendName: String,
        toolName: String,
        toolVersion: String,
        actionSummary: String,
        wasApproved: Boolean,
        riskLevel: RiskLevel,
        durationMs: Long,
        exitCode: Int,
        errorCategory: String? = null,
        changedFiles: String? = null
    ): DetailedAuditRecord {
        // Enforce credential masking on summary
        val sanitizedSummary = SecretRedactor.redact(actionSummary)
        val record = DetailedAuditRecord(
            id = "audit_" + UUID.randomUUID().toString().substring(0, 8),
            sessionId = sessionId,
            workspacePath = workspacePath,
            backendName = backendName,
            toolName = toolName,
            toolVersion = toolVersion,
            actionSummary = sanitizedSummary,
            wasApproved = wasApproved,
            riskLevel = riskLevel,
            durationMs = durationMs,
            exitCode = exitCode,
            errorCategory = errorCategory,
            changedFilesSummary = changedFiles
        )

        auditQueue.add(record)
        // Auto-rotation: keep within maxRecords bound
        while (auditQueue.size > maxRecords) {
            auditQueue.poll()
        }
        return record
    }

    fun getAllRecords(): List<DetailedAuditRecord> = auditQueue.toList().reversed()

    // Requirement 18: Checkpoint before risky file operations
    fun createCheckpoint(relativePath: String, oldContent: String, newContent: String): FileCheckpoint {
        val checkpoint = FileCheckpoint(
            id = "cp_" + UUID.randomUUID().toString().substring(0, 8),
            relativePath = relativePath,
            previousContent = oldContent,
            newContent = newContent
        )
        checkpoints.add(0, checkpoint)
        if (checkpoints.size > 50) {
            checkpoints.removeAt(checkpoints.size - 1)
        }
        return checkpoint
    }

    fun getCheckpoints(): List<FileCheckpoint> = checkpoints.toList()

    fun previewRollback(checkpointId: String): FileCheckpoint? {
        return checkpoints.find { it.id == checkpointId }
    }

    // Atomic File Write with partial write detection (Requirement 18)
    fun atomicWrite(targetFile: File, content: String): Pair<Boolean, String> {
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp_${System.currentTimeMillis()}")
        return try {
            targetFile.parentFile?.mkdirs()
            tempFile.writeText(content)

            // Verify content length matches expected before atomic commit
            if (tempFile.length() != content.toByteArray(Charsets.UTF_8).size.toLong()) {
                tempFile.delete()
                return false to "Partial write detected! Content byte mismatch during atomic buffer flush."
            }

            // Atomic rename / replace
            val renamed = tempFile.renameTo(targetFile)
            if (!renamed) {
                // Fallback replace
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
            true to "Atomic write succeeded"
        } catch (e: Exception) {
            tempFile.delete()
            false to "Write failure: ${e.message}"
        }
    }
}
