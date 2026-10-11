package com.example.compute

import com.example.security.SecretRedactor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class LocalSandboxBackend(
    private val workspaceDir: File
) : ComputeBackend {

    override val type: BackendType = BackendType.APP_LOCAL
    override val id: String = "local_sandbox"
    override val name: String = "Local Sandbox"
    override val capabilities: BackendCapabilities = BackendCapabilities(
        hasShell = true,
        hasGuiDesktop = false, // Honest: CLI only, no GUI desktop!
        hasPackageInstaller = false,
        hasNetworkEgress = true,
        isHardwareIsolated = false,
        memoryLimitMb = 512,
        timeoutSeconds = 30
    )
    override var isConnected: Boolean = true
        private set

    override suspend fun connect(): Result<Unit> {
        isConnected = true
        return Result.success(Unit)
    }

    override suspend fun disconnect() {
        isConnected = false
    }

    override fun getWorkingDir(): File = workspaceDir

    override suspend fun executeCommand(
        command: String,
        workingDir: String?,
        timeoutSeconds: Int
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val dir = if (!workingDir.isNullOrBlank()) File(workingDir) else workspaceDir
        if (!dir.exists()) dir.mkdirs()

        try {
            val process = ProcessBuilder("/system/bin/sh", "-c", command)
                .directory(dir)
                .redirectErrorStream(false)
                .start()

            val stdoutBuilder = StringBuilder()
            val stderrBuilder = StringBuilder()

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            val t1 = Thread {
                var line: String?
                while (stdoutReader.readLine().also { line = it } != null) {
                    stdoutBuilder.append(line).append("\n")
                }
            }
            val t2 = Thread {
                var line: String?
                while (stderrReader.readLine().also { line = it } != null) {
                    stderrBuilder.append(line).append("\n")
                }
            }

            t1.start()
            t2.start()

            val finished = process.waitFor(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            t1.join(1000)
            t2.join(1000)

            val elapsed = System.currentTimeMillis() - start
            val exitCode = if (finished) process.exitValue() else {
                process.destroyForcibly()
                124 // Timed out
            }

            val redactedStdout = SecretRedactor.redact(stdoutBuilder.toString().trim())
            val redactedStderr = SecretRedactor.redact(stderrBuilder.toString().trim())

            ExecutionResult(
                exitCode = exitCode,
                stdout = redactedStdout,
                stderr = if (!finished) "Process timed out after ${timeoutSeconds}s\n$redactedStderr" else redactedStderr,
                elapsedMs = elapsed
            )
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - start
            ExecutionResult(
                exitCode = 1,
                stdout = "",
                stderr = "Execution failed: ${e.message}",
                elapsedMs = elapsed
            )
        }
    }
}
