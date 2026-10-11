package com.example.compute

import java.io.File

data class RemoteSshConfig(
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val hasPrivateKey: Boolean = false,
    val verifiedHostKeySha256: String? = null
)

data class RemoteVmConfig(
    val endpointUrl: String = "",
    val vncWebUrl: String? = null,
    val isTlsVerified: Boolean = true
)

class BackendRegistry(
    workspaceDir: File
) {
    val localBackend = LocalSandboxBackend(workspaceDir)

    var activeBackendId: String = localBackend.id
        private set

    val registeredBackends = listOf(
        localBackend,
        object : ComputeBackend {
            override val type = BackendType.TERMUX
            override val id = "termux_bridge"
            override val name = "Termux Bridge"
            override val capabilities = BackendCapabilities(
                hasShell = true,
                hasGuiDesktop = false,
                hasPackageInstaller = true,
                hasNetworkEgress = true,
                isHardwareIsolated = false,
                memoryLimitMb = 1024,
                timeoutSeconds = 60
            )
            override var isConnected = false
            override suspend fun connect(): Result<Unit> = Result.failure(IllegalStateException("Termux not detected or Termux:API package missing on this device."))
            override suspend fun disconnect() { isConnected = false }
            override suspend fun executeCommand(command: String, workingDir: String?, timeoutSeconds: Int) = ExecutionResult(1, "", "Termux disconnected", 0)
            override fun getWorkingDir(): File = workspaceDir
        },
        object : ComputeBackend {
            override val type = BackendType.PROOT_LINUX
            override val id = "proot_linux"
            override val name = "PRoot Linux"
            override val capabilities = BackendCapabilities(
                hasShell = true,
                hasGuiDesktop = false,
                hasPackageInstaller = true,
                hasNetworkEgress = true,
                isHardwareIsolated = false,
                memoryLimitMb = 1024,
                timeoutSeconds = 60
            )
            override var isConnected = false
            override suspend fun connect(): Result<Unit> = Result.failure(IllegalStateException("PRoot rootfs not initialized in app files directory."))
            override suspend fun disconnect() { isConnected = false }
            override suspend fun executeCommand(command: String, workingDir: String?, timeoutSeconds: Int) = ExecutionResult(1, "", "PRoot not initialized", 0)
            override fun getWorkingDir(): File = workspaceDir
        },
        object : ComputeBackend {
            override val type = BackendType.REMOTE_SSH
            override val id = "remote_ssh"
            override val name = "Remote SSH (BYO)"
            override val capabilities = BackendCapabilities(
                hasShell = true,
                hasGuiDesktop = false,
                hasPackageInstaller = true,
                hasNetworkEgress = true,
                isHardwareIsolated = true,
                memoryLimitMb = 4096,
                timeoutSeconds = 120
            )
            override var isConnected = false
            override suspend fun connect(): Result<Unit> = Result.failure(IllegalStateException("Configure SSH host, credentials and host key verification in Settings."))
            override suspend fun disconnect() { isConnected = false }
            override suspend fun executeCommand(command: String, workingDir: String?, timeoutSeconds: Int) = ExecutionResult(1, "", "SSH connection not configured", 0)
            override fun getWorkingDir(): File = workspaceDir
        },
        object : ComputeBackend {
            override val type = BackendType.REMOTE_VM_CONTAINER
            override val id = "remote_vm"
            override val name = "Cloud Container / VM"
            override val capabilities = BackendCapabilities(
                hasShell = true,
                hasGuiDesktop = true, // Has VNC/Web Desktop if remote backend configured
                hasPackageInstaller = true,
                hasNetworkEgress = true,
                isHardwareIsolated = true,
                memoryLimitMb = 8192,
                timeoutSeconds = 300
            )
            override var isConnected = false
            override suspend fun connect(): Result<Unit> = Result.failure(IllegalStateException("No remote container endpoint configured. Enter BYO container URL."))
            override suspend fun disconnect() { isConnected = false }
            override suspend fun executeCommand(command: String, workingDir: String?, timeoutSeconds: Int) = ExecutionResult(1, "", "No container connected", 0)
            override fun getWorkingDir(): File = workspaceDir
        }
    )

    fun getActiveBackend(): ComputeBackend {
        return registeredBackends.find { it.id == activeBackendId } ?: localBackend
    }

    fun selectBackend(id: String) {
        if (registeredBackends.any { it.id == id }) {
            activeBackendId = id
        }
    }
}
