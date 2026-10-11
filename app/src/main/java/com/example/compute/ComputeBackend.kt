package com.example.compute

import java.io.File

enum class BackendType(val displayName: String, val description: String) {
    APP_LOCAL("App-Local Sandbox", "Native Android app-private workspace. Non-root shell via ProcessBuilder."),
    TERMUX("Termux Integration", "Termux environment via intent/socket. Access to apt/pkg, Python, Node, Clang."),
    PROOT_LINUX("PRoot Linux User-Space", "Chroot-like user-space Linux environment. Note: Emulated root, NOT hardware VM."),
    REMOTE_SSH("Remote SSH (BYO)", "User-owned remote Linux server over verified SSH. Requires host key validation."),
    REMOTE_VM_CONTAINER("Remote Container / VM", "Headless Docker/K8s/LXC VM instance with optional web desktop."),
    LOCAL_MODEL_RUNTIME("Local Model Runtime", "On-device or LAN Ollama/vLLM daemon for local LLM inference.")
}

data class BackendCapabilities(
    val hasShell: Boolean,
    val hasGuiDesktop: Boolean,
    val hasPackageInstaller: Boolean,
    val hasNetworkEgress: Boolean,
    val isHardwareIsolated: Boolean,
    val memoryLimitMb: Int,
    val timeoutSeconds: Int
)

data class ExecutionResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val elapsedMs: Long
)

interface ComputeBackend {
    val type: BackendType
    val id: String
    val name: String
    val capabilities: BackendCapabilities
    val isConnected: Boolean

    suspend fun connect(): Result<Unit>
    suspend fun disconnect()
    suspend fun executeCommand(command: String, workingDir: String? = null, timeoutSeconds: Int = 30): ExecutionResult
    fun getWorkingDir(): File
}
