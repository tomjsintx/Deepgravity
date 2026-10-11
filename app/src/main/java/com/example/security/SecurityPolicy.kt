package com.example.security

enum class RiskLevel(val labelEn: String, val labelVi: String, val autoApproveDefault: Boolean) {
    READ_ONLY("Read Only", "Chỉ đọc", true),
    LOW("Low Risk", "Rủi ro thấp", true),
    MEDIUM("Medium Risk", "Rủi ro trung bình", false),
    HIGH("High Risk", "Rủi ro cao", false),
    CRITICAL("Critical", "Nguy kịch", false)
}

typealias RiskTier = RiskLevel

data class SecurityAuditEntry(
    val id: String,
    val toolName: String,
    val riskLevel: RiskLevel,
    val target: String,
    val summary: String,
    val wasApproved: Boolean,
    val executedAt: Long = System.currentTimeMillis()
)

data class PendingApprovalAction(
    val actionId: String,
    val toolId: String,
    val toolName: String,
    val riskLevel: RiskLevel,
    val target: String,
    val scope: String,
    val impactDescriptionEn: String,
    val impactDescriptionVi: String,
    val parametersJson: String,
    val onApprove: () -> Unit = {},
    val onReject: () -> Unit = {}
)

typealias PendingToolApproval = PendingApprovalAction
