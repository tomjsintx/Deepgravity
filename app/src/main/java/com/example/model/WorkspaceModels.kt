package com.example.model

data class WorkspaceFile(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val content: String = "",
    val language: String = "kotlin",
    val isModified: Boolean = false,
    val children: List<WorkspaceFile> = emptyList()
)

enum class DiffType {
    ADDED,
    REMOVED,
    UNCHANGED
}

data class DiffLine(
    val type: DiffType,
    val oldLineNumber: Int?,
    val newLineNumber: Int?,
    val content: String
)

data class CodeDiff(
    val filePath: String,
    val summary: String,
    val additions: Int,
    val deletions: Int,
    val lines: List<DiffLine>
)

data class ProjectBuildStatus(
    val isBuilding: Boolean = false,
    val lastBuildSuccess: Boolean = true,
    val lastBuildDurationSec: Double = 2.4,
    val errorCount: Int = 0,
    val warningCount: Int = 1,
    val buildOutput: String = "BUILD SUCCESSFUL in 2s\n18 actionable tasks: 18 executed"
)
