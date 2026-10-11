package com.example.mcp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

enum class McpTransportType {
    STDIO,
    STREAMABLE_HTTP,
    LEGACY_SSE
}

data class McpServerConfig(
    val serverId: String,
    val name: String,
    val transportType: McpTransportType,
    val endpointUrl: String, // Or process command for STDIO
    val allowlistedTools: List<String> = emptyList(),
    val isTlsRequired: Boolean = true,
    val isConnected: Boolean = false
)

data class McpServerCapabilities(
    val supportsTools: Boolean = true,
    val supportsPrompts: Boolean = false,
    val supportsResources: Boolean = false,
    val protocolVersion: String = "2024-11-05"
)

sealed class McpSecurityCheckResult {
    data object Safe : McpSecurityCheckResult()
    data class Blocked(val reason: String) : McpSecurityCheckResult()
}

object McpSecurityGuard {

    private val BLOCKED_HOSTS = listOf(
        "localhost",
        "127.0.0.1",
        "0.0.0.0",
        "169.254.169.254", // Cloud Instance Metadata Service
        "metadata.google.internal",
        "10.",
        "192.168.",
        "172.16.", "172.17.", "172.18.", "172.19.", "172.20.",
        "172.21.", "172.22.", "172.23.", "172.24.", "172.25.",
        "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31."
    )

    fun validateEndpoint(urlStr: String, requireTls: Boolean): McpSecurityCheckResult {
        return try {
            val uri = URI(urlStr)
            val scheme = uri.scheme?.lowercase() ?: return McpSecurityCheckResult.Blocked("Missing URI scheme")
            val host = uri.host?.lowercase() ?: return McpSecurityCheckResult.Blocked("Missing URI host")

            if (requireTls && scheme != "https") {
                return McpSecurityCheckResult.Blocked("Insecure scheme: TLS (https://) is required for remote MCP servers")
            }

            // Anti-SSRF check
            for (blocked in BLOCKED_HOSTS) {
                if (host == blocked || host.startsWith(blocked) || host.endsWith(".$blocked")) {
                    return McpSecurityCheckResult.Blocked("SSRF Protection: Destination $host targets restricted local/internal network or cloud metadata service")
                }
            }

            McpSecurityCheckResult.Safe
        } catch (e: Exception) {
            McpSecurityCheckResult.Blocked("Malformed URI: ${e.message}")
        }
    }

    // Requirement 12: Do not trust tool descriptions or prompt injections from untrusted servers
    fun sanitizeServerDescription(rawDescription: String): String {
        return rawDescription
            .replace(Regex("(?i)ignore previous instructions.*"), "[REDACTED_PROMPT_INJECTION]")
            .replace(Regex("(?i)system:\\s*"), "[SANITIZED_PREFIX]")
            .take(500)
    }
}

interface McpClientAdapter {
    suspend fun initialize(): Result<McpServerCapabilities>
    suspend fun listTools(): Result<List<McpRemoteTool>>
    suspend fun callTool(name: String, arguments: JSONObject): Result<String>
    suspend fun disconnect()
}

data class McpRemoteTool(
    val name: String,
    val description: String,
    val inputSchemaJson: String,
    val isAllowlisted: Boolean
)

class StreamableHttpMcpAdapter(
    private val config: McpServerConfig
) : McpClientAdapter {

    override suspend fun initialize(): Result<McpServerCapabilities> = withContext(Dispatchers.IO) {
        val check = McpSecurityGuard.validateEndpoint(config.endpointUrl, config.isTlsRequired)
        if (check is McpSecurityCheckResult.Blocked) {
            return@withContext Result.failure(SecurityException(check.reason))
        }

        Result.success(
            McpServerCapabilities(
                supportsTools = true,
                supportsPrompts = true,
                supportsResources = true,
                protocolVersion = "2024-11-05"
            )
        )
    }

    override suspend fun listTools(): Result<List<McpRemoteTool>> = withContext(Dispatchers.IO) {
        val tools = listOf(
            McpRemoteTool(
                name = "code_analyzer",
                description = McpSecurityGuard.sanitizeServerDescription("Analyzes code complexity, cyclo-complexity, and static lint issues."),
                inputSchemaJson = "{\"type\": \"object\", \"properties\": {\"code\": {\"type\": \"string\"}}}",
                isAllowlisted = config.allowlistedTools.isEmpty() || config.allowlistedTools.contains("code_analyzer")
            ),
            McpRemoteTool(
                name = "package_registry_lookup",
                description = McpSecurityGuard.sanitizeServerDescription("Queries Maven Central / Google Maven for latest Android artifact versions."),
                inputSchemaJson = "{\"type\": \"object\", \"properties\": {\"groupId\": {\"type\": \"string\"}, \"artifactId\": {\"type\": \"string\"}}}",
                isAllowlisted = config.allowlistedTools.isEmpty() || config.allowlistedTools.contains("package_registry_lookup")
            )
        )
        Result.success(tools)
    }

    override suspend fun callTool(name: String, arguments: JSONObject): Result<String> = withContext(Dispatchers.IO) {
        if (config.allowlistedTools.isNotEmpty() && !config.allowlistedTools.contains(name)) {
            return@withContext Result.failure(SecurityException("Tool '$name' is not in the allowlist for server '${config.name}'"))
        }

        // Mock safe remote execution
        Result.success("{\"status\": \"success\", \"output\": \"Executed remote MCP tool $name with validated arguments\"}")
    }

    override suspend fun disconnect() {
        // cleanup connections
    }
}
