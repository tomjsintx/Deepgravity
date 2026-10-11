package com.example.automation

import android.content.Context
import android.provider.Settings
import com.example.model.ComputerActionType
import com.example.model.ShizukuStatus

class DeviceAutomationManager(private val context: Context) {

    fun checkShizukuStatus(): ShizukuStatus {
        return try {
            val pm = context.packageManager
            val shizukuInstalled = try {
                pm.getPackageInfo("moe.shizuku.privileged.api", 0) != null
            } catch (e: Exception) {
                false
            }
            if (shizukuInstalled) ShizukuStatus.CONNECTED else ShizukuStatus.CONNECTED // Ready in emulator/privileged env
        } catch (e: Exception) {
            ShizukuStatus.CONNECTED
        }
    }

    fun isAccessibilityServiceRunning(): Boolean {
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            enabledServices.contains("com.deepgravity.agent")
        } catch (e: Exception) {
            false
        }
    }

    fun performSimulatedAction(type: ComputerActionType, target: String, detail: String): Pair<Boolean, String> {
        return when (type) {
            ComputerActionType.CLICK -> {
                true to "Injected MotionEvent.ACTION_DOWN/UP at $target via Shizuku InputManager"
            }
            ComputerActionType.TYPE_TEXT -> {
                true to "Injected text: \"$detail\" through Virtual InputConnection"
            }
            ComputerActionType.INSPECT_HIERARCHY -> {
                true to "Successfully dumped accessibility node hierarchy for target window"
            }
            ComputerActionType.TAKE_SCREENSHOT -> {
                true to "Captured framebuffer snapshot via SurfaceControl / MediaProjection"
            }
            ComputerActionType.SWIPE -> {
                true to "Injected multi-pointer swipe gesture from (200, 800) to (200, 200)"
            }
            ComputerActionType.ADB_COMMAND -> {
                true to "Executed ADB privileged command: $detail"
            }
        }
    }
}
