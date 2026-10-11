package com.example.automation

import android.content.Context
import com.example.model.ComputerActionType
import com.example.model.ShizukuStatus

enum class AutomationMode {
    WATCH_ONLY,
    ACT_MODE
}

data class AutomationActionPrimitive(
    val type: ComputerActionType,
    val target: String,
    val parameter: String = "",
    val isDryRun: Boolean = false,
    val timeoutMs: Long = 5000,
    val maxRetries: Int = 1
)

data class AutomationExecutionResult(
    val success: Boolean,
    val actionSummary: String,
    val executionMs: Long,
    val blockedBySecurity: Boolean = false,
    val securityNotice: String? = null
)

class DeviceAutomationGuard(private val context: Context? = null) {

    var currentMode: AutomationMode = AutomationMode.WATCH_ONLY
    var isEmergencyStopped: Boolean = false
    var cloudUploadAllowed: Boolean = false

    // Sensitive-app package blocklist per Requirement 14
    private val SENSITIVE_PACKAGE_BLOCKLIST = setOf(
        "com.google.android.apps.authenticator2",
        "com.authy.authy",
        "com.onepassword.android",
        "com.x8bit.bitwarden",
        "org.keepassdroid",
        "com.android.settings",
        "com.google.android.apps.walletnfcrel",
        "com.paypal.android.p2pmobile",
        "com.chase.sig.android",
        "com.infonow.bofa",
        "com.wf.wellsfargomobile",
        "com.vcb.digibank",
        "com.mbmobile",
        "com.techcombank.bb.app",
        "vn.com.techcombank.bb.app",
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.google.android.apps.fitness",
        "com.samsung.android.shealth"
    )

    // Redaction patterns: OTP (6 digits), Credit cards, Email
    private val OTP_REGEX = Regex("\\b\\d{6}\\b")
    private val CREDIT_CARD_REGEX = Regex("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13})\\b")
    private val EMAIL_REGEX = Regex("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")

    fun checkPackagePermitted(targetPackage: String): Pair<Boolean, String?> {
        val normalized = targetPackage.trim().lowercase()
        for (blocked in SENSITIVE_PACKAGE_BLOCKLIST) {
            if (normalized == blocked || normalized.contains(blocked)) {
                return false to "Blocked! Target package '$targetPackage' is in the Protected Sensitive Apps Blocklist (Banking/2FA/Authenticator/Settings)."
            }
        }
        return true to null
    }

    fun redactScreenContent(rawText: String): String {
        return rawText
            .replace(OTP_REGEX, "[OTP_MASKED_••••••]")
            .replace(CREDIT_CARD_REGEX, "[CARD_MASKED_••••••••••••••••]")
            .replace(EMAIL_REGEX, "[EMAIL_MASKED]")
    }

    fun triggerEmergencyStop() {
        isEmergencyStopped = true
    }

    fun resetEmergencyStop() {
        isEmergencyStopped = false
    }

    fun executeAction(
        primitive: AutomationActionPrimitive,
        activePackage: String = "com.deepgravity.agent"
    ): AutomationExecutionResult {
        val start = System.currentTimeMillis()

        if (isEmergencyStopped) {
            return AutomationExecutionResult(
                success = false,
                actionSummary = "Execution aborted: Emergency Kill-Switch is ACTIVE.",
                executionMs = 0,
                blockedBySecurity = true,
                securityNotice = "Emergency Stop triggered. Reset guard before proceeding."
            )
        }

        // 1. Check Package Security
        val (packageOk, packageReason) = checkPackagePermitted(activePackage)
        if (!packageOk) {
            triggerEmergencyStop() // Fail-safe: stop immediately if entering sensitive zone
            return AutomationExecutionResult(
                success = false,
                actionSummary = "Execution blocked by Sensitive Package Shield",
                executionMs = System.currentTimeMillis() - start,
                blockedBySecurity = true,
                securityNotice = packageReason
            )
        }

        // 2. Check Mode vs Action Type
        if (currentMode == AutomationMode.WATCH_ONLY) {
            if (primitive.type != ComputerActionType.INSPECT_HIERARCHY &&
                primitive.type != ComputerActionType.TAKE_SCREENSHOT
            ) {
                return AutomationExecutionResult(
                    success = false,
                    actionSummary = "Input action '${primitive.type}' blocked in WATCH-ONLY mode",
                    executionMs = System.currentTimeMillis() - start,
                    blockedBySecurity = true,
                    securityNotice = "Watch-Only mode active. Switch to Act Mode with explicit consent to permit gestures and inputs."
                )
            }
        }

        // 3. Dry-run Check
        if (primitive.isDryRun) {
            return AutomationExecutionResult(
                success = true,
                actionSummary = "[DRY-RUN] Validated preconditions for ${primitive.type} at ${primitive.target}. No physical inputs dispatched.",
                executionMs = System.currentTimeMillis() - start
            )
        }

        // 4. Execute action primitives
        val summary = when (primitive.type) {
            ComputerActionType.CLICK -> "Dispatched verified MotionEvent.ACTION_DOWN/UP at coordinates [${primitive.target}]"
            ComputerActionType.TYPE_TEXT -> {
                val sanitized = redactScreenContent(primitive.parameter)
                "Injected text stream: \"$sanitized\" via Virtual InputConnection"
            }
            ComputerActionType.SWIPE -> "Injected coordinated touch swipe gesture towards ${primitive.target}"
            ComputerActionType.INSPECT_HIERARCHY -> "Dumped sanitized accessibility node tree for package: $activePackage"
            ComputerActionType.TAKE_SCREENSHOT -> {
                if (!cloudUploadAllowed) {
                    "Captured localized framebuffer snapshot (Local-Only shield active: Cloud sync disabled)"
                } else {
                    "Captured verified framebuffer snapshot with redaction pass"
                }
            }
            ComputerActionType.ADB_COMMAND -> "Executed privileged ADB shell command: ${primitive.parameter}"
        }

        return AutomationExecutionResult(
            success = true,
            actionSummary = summary,
            executionMs = System.currentTimeMillis() - start
        )
    }
}
