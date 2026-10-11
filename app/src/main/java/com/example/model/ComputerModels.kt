package com.example.model

enum class ShizukuStatus {
    CONNECTED,
    PERMISSION_NEEDED,
    DISCONNECTED
}

enum class ComputerActionType {
    CLICK,
    TYPE_TEXT,
    SWIPE,
    INSPECT_HIERARCHY,
    TAKE_SCREENSHOT,
    ADB_COMMAND
}

data class ComputerAction(
    val id: String,
    val type: ComputerActionType,
    val target: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)

data class SimulatedAppScreen(
    val title: String,
    val currentPackage: String,
    val currentActivity: String,
    val elements: List<SimulatedUiElement>
)

data class SimulatedUiElement(
    val id: String,
    val text: String,
    val resourceId: String,
    val className: String,
    val bounds: String,
    val isClickable: Boolean
)
