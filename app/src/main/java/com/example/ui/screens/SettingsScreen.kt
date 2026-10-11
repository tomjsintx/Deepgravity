package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.L10n
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage,
    currentTheme: AppThemeMode,
    currentThinkingLevel: ThinkingLevel,
    safeModeEnabled: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (AppThemeMode) -> Unit,
    onThinkingLevelChange: (ThinkingLevel) -> Unit,
    onSafeModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var telemetryEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section: Security & Safe Mode
        SettingsSectionCard(title = L10n.safeModeTitle(currentLanguage)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = L10n.get(currentLanguage, "Execution Approval Gate", "Cổng duyệt thực thi an toàn"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = L10n.safeModeDesc(currentLanguage),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = safeModeEnabled,
                        onCheckedChange = onSafeModeChange,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = L10n.get(
                                currentLanguage,
                                "Hardware Android KeyStore: AES-256-GCM Vault Active",
                                "Phần cứng Android KeyStore: Kho lưu AES-256-GCM đang hoạt động"
                            ),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = NeonCyan
                        )
                    }
                }
            }
        }

        // Section: Language
        SettingsSectionCard(title = L10n.get(currentLanguage, "Language & Localization", "Ngôn ngữ & Bản địa hóa")) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.values().forEach { lang ->
                    val isSelected = currentLanguage == lang
                    OutlinedButton(
                        onClick = { onLanguageChange(lang) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("setting_lang_${lang.name.lowercase()}"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    ) {
                        Text(
                            text = "${lang.flag} ${lang.displayName}",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Section: Theme
        SettingsSectionCard(title = L10n.get(currentLanguage, "Appearance & Theme", "Giao diện & Chủ đề")) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppThemeMode.values().forEach { theme ->
                    val isSelected = currentTheme == theme
                    Surface(
                        onClick = { onThemeChange(theme) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setting_theme_${theme.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.VI) theme.displayNameVi else theme.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { onThemeChange(theme) },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Thinking Level
        SettingsSectionCard(title = L10n.get(currentLanguage, "Reasoning Engine (Apex Reasoner v2)", "Động cơ Suy luận (Apex Reasoner v2)")) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = L10n.get(
                        currentLanguage,
                        "Thinking mode generates rigorous chain-of-thought proofs (SPEC -> PATTERN -> PROVE -> BOUND -> BUILD -> BREAK) before tool execution.",
                        "Chế độ suy luận tạo lập chuỗi chứng minh logic chặt chẽ (SPEC -> PATTERN -> PROVE -> BOUND -> BUILD -> BREAK) trước khi thực thi công cụ."
                    ),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                ThinkingLevel.values().forEach { level ->
                    val isSelected = currentThinkingLevel == level
                    Surface(
                        onClick = { onThinkingLevelChange(level) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setting_thinking_${level.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = level.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (level == ThinkingLevel.HIGH) {
                                    Text(
                                        text = "Uses gemini-3.1-pro-preview with ThinkingLevel.HIGH",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = NeonPurpleGlow
                                    )
                                }
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { onThinkingLevelChange(level) },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Telemetry & Privacy
        SettingsSectionCard(title = L10n.get(currentLanguage, "Telemetry & Privacy", "Đo lường & Quyền riêng tư")) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = L10n.get(currentLanguage, "Telemetry Disabled by Default", "Đo lường Tắt mặc định"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = L10n.get(
                            currentLanguage,
                            "Zero external data egress. Crash logs and execution history stay strictly on your local device.",
                            "Không truyền dữ liệu ra ngoài. Nhật ký lỗi và lịch sử thực thi được lưu trữ cục bộ trên máy."
                        ),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = telemetryEnabled,
                    onCheckedChange = { telemetryEnabled = it }
                )
            }
        }

        // Section: Release Signing & Export
        SettingsSectionCard(title = L10n.get(currentLanguage, "Release & Build Distribution", "Phân phối Bản dựng & Ký APK")) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Application ID: com.deepgravity.agent",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = NeonCyan
                )
                Text(
                    text = "Target ABI: arm64-v8a (Debug: + x86_64)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Release Key: Injected via KEYSTORE_PATH & STORE_PASSWORD environment variables or exportable signed release APK.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}
