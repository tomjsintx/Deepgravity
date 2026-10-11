package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.localization.L10n
import com.example.model.AppLanguage
import com.example.model.ComputerAction
import com.example.model.ComputerActionType
import com.example.model.ShizukuStatus
import com.example.ui.theme.*

@Composable
fun AutomationScreen(
    actions: List<ComputerAction>,
    shizukuStatus: ShizukuStatus,
    language: AppLanguage,
    onTriggerAction: (ComputerActionType, String, String) -> Unit,
    mode: com.example.automation.AutomationMode = com.example.automation.AutomationMode.WATCH_ONLY,
    emergencyStopped: Boolean = false,
    onToggleMode: () -> Unit = {},
    onEmergencyStop: () -> Unit = {},
    onResetEmergencyStop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var tapCoordinate by remember { mutableStateOf("540 960") }
    var textInjection by remember { mutableStateOf("DeepGravity Agent") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        // Automation Mode & Emergency Kill-Switch (Requirement 14)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    1.dp,
                    if (emergencyStopped) CodeRed else if (mode == com.example.automation.AutomationMode.ACT_MODE) CodeYellow else CodeGreen.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                ),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (emergencyStopped) CodeRed else if (mode == com.example.automation.AutomationMode.ACT_MODE) CodeYellow else CodeGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (emergencyStopped) {
                                    L10n.get(language, "EMERGENCY STOPPED", "DỪNG KHẨN CẤP")
                                } else if (mode == com.example.automation.AutomationMode.ACT_MODE) {
                                    L10n.get(language, "ACT MODE (Input Dispatched)", "CHẾ ĐỘ THAO TÁC (Có phát Input)")
                                } else {
                                    L10n.get(language, "WATCH-ONLY (Read Only)", "CHẾ ĐỘ CHỈ QUAN SÁT (Không phát Input)")
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (emergencyStopped) CodeRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = L10n.get(
                                language,
                                "Sensitive App Shield active: Banking, 2FA, Wallet & Settings protected.",
                                "Lá chắn ứng dụng nhạy cảm: Bảo vệ Ngân hàng, 2FA, Ví & Cài đặt."
                            ),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onToggleMode,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mode == com.example.automation.AutomationMode.ACT_MODE) CodeYellow.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (mode == com.example.automation.AutomationMode.ACT_MODE) CodeYellow else MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (mode == com.example.automation.AutomationMode.ACT_MODE) "Mode: ACT" else "Mode: WATCH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                if (emergencyStopped) onResetEmergencyStop() else onEmergencyStop()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (emergencyStopped) CodeGreen else CodeRed,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (emergencyStopped) "RESET" else "STOP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        // Automation Header & Daemon Status
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
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (shizukuStatus == ShizukuStatus.CONNECTED) CodeGreen else CodeRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = L10n.get(language, "Controlled Android Automation", "Tự động hóa Android có kiểm soát"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CodeGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Shizuku + Accessibility",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CodeGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = L10n.get(
                        language,
                        "Non-root automation bridge using Shizuku binder IPC and Android AccessibilityService. Restricted strictly to authorized target app scopes.",
                        "Cầu nối tự động hóa không cần root sử dụng Shizuku binder IPC và AccessibilityService. Giới hạn nghiêm ngặt trong phạm vi ứng dụng được cấp quyền."
                    ),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions Testbed Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = L10n.get(language, "Direct Gesture Injection", "Giả lập Thao tác Trực tiếp"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val parts = tapCoordinate.trim().split(" ")
                            val x = parts.getOrNull(0) ?: "540"
                            val y = parts.getOrNull(1) ?: "960"
                            onTriggerAction(ComputerActionType.CLICK, "($x, $y)", "Injected tap at ($x, $y)")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(L10n.get(language, "Tap Point", "Chạm điểm"), fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            onTriggerAction(ComputerActionType.INSPECT_HIERARCHY, "Window 0", "Dumped accessibility node tree")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(L10n.get(language, "Inspect Tree", "Cây UI"), fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            onTriggerAction(ComputerActionType.TAKE_SCREENSHOT, "Display 0", "Captured frame buffer")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(L10n.get(language, "Snapshot", "Ảnh chụp"), fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInjection,
                        onValueChange = { textInjection = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Text to type...", fontSize = 11.sp) }
                    )
                    Button(
                        onClick = {
                            onTriggerAction(ComputerActionType.TYPE_TEXT, "Active Focus", textInjection)
                        }
                    ) {
                        Text(L10n.get(language, "Type Text", "Gõ chữ"), fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // History Log
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = L10n.get(language, "Automation Event Journal", "Nhật ký Tự động hóa"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${actions.size} events",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(actions, key = { it.id }) { action ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${action.type.name} • ${action.target}",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = action.detail,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
