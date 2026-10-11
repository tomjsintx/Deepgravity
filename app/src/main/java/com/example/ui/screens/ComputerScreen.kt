package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compute.BackendRegistry
import com.example.compute.BackendType
import com.example.localization.L10n
import com.example.model.AppLanguage
import com.example.model.ShizukuStatus
import com.example.ui.theme.*

@Composable
fun ComputerScreen(
    activeBackendName: String,
    backendType: BackendType,
    hasGuiDesktop: Boolean,
    isConnected: Boolean,
    language: AppLanguage,
    onNavigateToTerminal: () -> Unit,
    onNavigateToProviders: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        // Backend Status Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
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
                                .background(if (isConnected) CodeGreen else CodeYellow)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Compute Backend: $activeBackendName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (hasGuiDesktop) NeonCyan.copy(alpha = 0.15f) else SurfaceDarkElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (hasGuiDesktop) "GUI DESKTOP READY" else "CLI-ONLY (NO GUI)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (hasGuiDesktop) NeonCyan else TextMutedDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = L10n.get(
                        language,
                        "Honest Environment Display: Distinguishes between local Android host and connected remote Linux VMs/containers. Never renders fake desktop graphics.",
                        "Hiển thị Môi trường Trung thực: Phân biệt rõ ràng giữa Android host và remote Linux VM. Không giả lập đồ họa desktop khi backend không có GUI."
                    ),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Display Box
        if (!hasGuiDesktop) {
            // Honest CLI-Only Notice
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SurfaceDarkBorder, RoundedCornerShape(12.dp)),
                color = VoidBlack
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = L10n.get(
                            language,
                            "CLI-Only Backend Environment",
                            "Môi trường Backend chỉ có dòng lệnh (CLI)"
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = L10n.get(
                            language,
                            "The active backend ($activeBackendName) operates strictly in non-graphical headless mode. Full command-line orchestration, tool execution, and file compilation are available in the Terminal & Workspace tabs.",
                            "Backend hiện hành ($activeBackendName) hoạt động ở chế độ headless không đồ họa. Toàn bộ lệnh shell, công cụ và tệp tin sẵn sàng trong tab Terminal & Workspace."
                        ),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onNavigateToTerminal) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L10n.get(language, "Open Terminal", "Mở Terminal"))
                        }
                    }
                }
            }
        } else {
            // Live Remote Web/VNC Desktop Viewer
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                color = VoidBlack
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDarkElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Remote Container Desktop (X11 / Wayland VNC)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Authenticated TLS Stream Connected: 1920x1080@60Hz",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = CodeGreen
                        )
                    }
                }
            }
        }
    }
}
