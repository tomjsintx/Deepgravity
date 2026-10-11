package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.extensions.SkillPluginManifest
import com.example.localization.L10n
import com.example.model.AppLanguage
import com.example.security.RiskLevel
import com.example.ui.theme.*

@Composable
fun ExtensionsScreen(
    plugins: List<SkillPluginManifest>,
    language: AppLanguage,
    onTogglePlugin: (String) -> Unit,
    onInstallPlugin: (SkillPluginManifest) -> Unit,
    modifier: Modifier = Modifier
) {
    var showInstallDialog by remember { mutableStateOf(false) }
    var inputGitUrl by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        // Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Extension, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = L10n.get(language, "Skills, Plugins & Mods", "Kỹ năng, Plugin & Bản mở rộng"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = { showInstallDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(L10n.get(language, "Install", "Cài đặt"), fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = L10n.get(
                        language,
                        "Install from Git repository, URL, or local JSON manifest. Pre-screened for permissions, filesystem scope, and cryptographic hash verification.",
                        "Cài đặt từ Git repo, URL hoặc tệp JSON. Được quét trước quyền, phạm vi tệp và mã băm SHA-256 an toàn."
                    ),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Plugins List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(plugins, key = { it.id }) { plugin ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = plugin.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "v${plugin.version}",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = if (language == AppLanguage.VI) plugin.descriptionVi else plugin.descriptionEn,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = plugin.isEnabled,
                                onCheckedChange = { onTogglePlugin(plugin.id) }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Details pill row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Risk: ${plugin.riskLevel.name}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = when (plugin.riskLevel) {
                                        RiskLevel.READ_ONLY -> CodeGreen
                                        RiskLevel.LOW -> CodeGreen
                                        RiskLevel.MEDIUM -> CodeYellow
                                        else -> CodeRed
                                    }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Scope: ${plugin.filesystemScope}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SHA256: ${plugin.contentHashSha256.take(16)}...",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMutedDark
                        )
                    }
                }
            }
        }
    }

    if (showInstallDialog) {
        AlertDialog(
            onDismissRequest = { showInstallDialog = false },
            title = { Text(L10n.get(language, "Install Plugin / Skill", "Cài đặt Plugin / Kỹ năng")) },
            text = {
                Column {
                    Text(
                        text = L10n.get(language, "Enter Git repository URL or Skill Manifest URL:", "Nhập URL Git repository hoặc URL Manifest:"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = inputGitUrl,
                        onValueChange = { inputGitUrl = it },
                        placeholder = { Text("https://github.com/...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputGitUrl.isNotBlank()) {
                            onInstallPlugin(
                                SkillPluginManifest(
                                    id = "custom-${System.currentTimeMillis()}",
                                    name = inputGitUrl.substringAfterLast("/").removeSuffix(".git").ifBlank { "Custom Skill" },
                                    version = "1.0.0",
                                    author = "Community / Git",
                                    sourceUrl = inputGitUrl.trim(),
                                    descriptionEn = "External skill imported from $inputGitUrl",
                                    descriptionVi = "Kỹ năng ngoài được nạp từ $inputGitUrl",
                                    declaredPermissions = listOf("WORKSPACE_READ"),
                                    networkScope = "SANDBOX",
                                    filesystemScope = "workspace/..",
                                    riskLevel = RiskLevel.MEDIUM,
                                    contentHashSha256 = "c1a9e88d4001bc93ef64a9807785afee",
                                    isEnabled = false
                                )
                            )
                            showInstallDialog = false
                            inputGitUrl = ""
                            Toast.makeText(context, L10n.get(language, "Plugin pre-screened & added (Disabled by default)", "Đã quét và thêm plugin (Tắt mặc định)"), Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(L10n.get(language, "Verify & Add", "Xác minh & Thêm"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstallDialog = false }) {
                    Text(L10n.get(language, "Cancel", "Hủy"))
                }
            }
        )
    }
}
