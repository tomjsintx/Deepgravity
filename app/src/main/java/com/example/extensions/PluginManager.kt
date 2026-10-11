package com.example.extensions

import com.example.security.RiskLevel

data class SkillPluginManifest(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val sourceUrl: String,
    val descriptionEn: String,
    val descriptionVi: String,
    val declaredPermissions: List<String>,
    val networkScope: String,
    val filesystemScope: String,
    val riskLevel: RiskLevel,
    val contentHashSha256: String,
    val isEnabled: Boolean = false,
    val isVerified: Boolean = true
)

class PluginManager {

    private val installedPlugins = mutableListOf<SkillPluginManifest>()

    init {
        // Built-in verified skills
        installedPlugins.add(
            SkillPluginManifest(
                id = "git-workflow-pro",
                name = "Git Workflow Pro",
                version = "1.2.0",
                author = "DeepGravity Core",
                sourceUrl = "builtin://plugins/git-workflow",
                descriptionEn = "Automated git branch management, squashing, conventional commits, and conflict resolution.",
                descriptionVi = "Quản lý nhánh Git tự động, gộp commit và giải quyết xung đột mã nguồn.",
                declaredPermissions = listOf("WORKSPACE_READ", "WORKSPACE_WRITE", "GIT_EXEC"),
                networkScope = "NONE (Local repo only)",
                filesystemScope = "workspace/..",
                riskLevel = RiskLevel.MEDIUM,
                contentHashSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                isEnabled = true
            )
        )
        installedPlugins.add(
            SkillPluginManifest(
                id = "compose-m3-lint",
                name = "Compose M3 Validator",
                version = "2.0.1",
                author = "Android Architecture Skills",
                sourceUrl = "builtin://plugins/compose-m3",
                descriptionEn = "Static analysis for Jetpack Compose Material 3 touch targets, semantics, and recomposition traps.",
                descriptionVi = "Phân tích tĩnh Jetpack Compose M3 về kích thước chạm, khả năng truy cập và recomposition.",
                declaredPermissions = listOf("WORKSPACE_READ"),
                networkScope = "NONE",
                filesystemScope = "workspace/**/*.kt",
                riskLevel = RiskLevel.READ_ONLY,
                contentHashSha256 = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
                isEnabled = true
            )
        )
        installedPlugins.add(
            SkillPluginManifest(
                id = "shizuku-device-controller",
                name = "Shizuku Device Controller",
                version = "1.0.4",
                author = "Rikka / Shizuku Integration",
                sourceUrl = "https://github.com/DeepGravity/skill-shizuku-adb",
                descriptionEn = "Extends autonomous device capabilities with privileged non-root ADB binder calls.",
                descriptionVi = "Mở rộng khả năng tự động hóa thiết bị qua giao thức ADB Shizuku đặc quyền.",
                declaredPermissions = listOf("SHIZUKU_IPC", "INJECT_EVENTS"),
                networkScope = "LOCAL_IPC (Port 5555)",
                filesystemScope = "NONE",
                riskLevel = RiskLevel.HIGH,
                contentHashSha256 = "8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4",
                isEnabled = true
            )
        )
    }

    fun getAllPlugins(): List<SkillPluginManifest> = installedPlugins.toList()

    fun togglePlugin(id: String): Boolean {
        val idx = installedPlugins.indexOfFirst { it.id == id }
        if (idx != -1) {
            val curr = installedPlugins[idx]
            installedPlugins[idx] = curr.copy(isEnabled = !curr.isEnabled)
            return true
        }
        return false
    }

    fun installFromManifest(manifest: SkillPluginManifest): Result<Unit> {
        // Validation check
        if (manifest.id.isBlank() || manifest.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Invalid plugin manifest: missing id or name"))
        }
        installedPlugins.add(manifest.copy(isEnabled = false)) // Default disabled for safety
        return Result.success(Unit)
    }
}
