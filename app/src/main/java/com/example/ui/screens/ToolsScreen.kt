package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.L10n
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun ToolsScreen(
    tools: List<McpToolItem>,
    testResult: String?,
    language: AppLanguage,
    onToggleTool: (String) -> Unit,
    onTestTool: (String, String) -> Unit,
    onClearTestResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<ToolCategory?>(null) }
    var activeTestingTool by remember { mutableStateOf<McpToolItem?>(null) }
    var toolInputJson by remember { mutableStateOf("") }

    val filteredTools = if (selectedCategory == null) {
        tools
    } else {
        tools.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        // Header banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Extension,
                        contentDescription = null,
                        tint = NeonPurpleGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Model Context Protocol (MCP) Registry",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = L10n.get(
                        language,
                        "Registered tools available for autonomous invocation by DeepGravity. Connects AI models to filesystem, shell, and ADB.",
                        "Danh sách công cụ đăng ký sẵn sàng cho DeepGravity tự động gọi. Kết nối mô hình AI với tệp, shell và ADB."
                    ),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text(L10n.get(language, "All (${tools.size})", "Tất cả (${tools.size})"), fontSize = 11.sp) }
            )
            ToolCategory.values().forEach { cat ->
                val count = tools.count { it.category == cat }
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = {
                        Text(
                            text = "${if (language == AppLanguage.VI) cat.titleVi else cat.titleEn} ($count)",
                            fontSize = 11.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tool Cards List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredTools, key = { it.id }) { tool ->
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
                                        text = tool.name,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (tool.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.VI) tool.category.titleVi else tool.category.titleEn,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (language == AppLanguage.VI) tool.descriptionVi else tool.descriptionEn,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = tool.isEnabled,
                                onCheckedChange = { onToggleTool(tool.id) },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Parameters and Test Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Params: ${tool.parametersJson}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextMutedDark,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )

                            OutlinedButton(
                                onClick = {
                                    activeTestingTool = tool
                                    toolInputJson = tool.sampleInputJson
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("test_tool_${tool.id}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(L10n.get(language, "Test", "Thử nghiệm"), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Tool Tester Modal Sheet
    if (activeTestingTool != null) {
        val tool = activeTestingTool!!
        AlertDialog(
            onDismissRequest = {
                activeTestingTool = null
                onClearTestResult()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Tool: ${tool.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Input Arguments (JSON):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = toolInputJson,
                        onValueChange = { toolInputJson = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        minLines = 3,
                        maxLines = 6
                    )

                    Button(
                        onClick = { onTestTool(tool.id, toolInputJson) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_tool_test_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(L10n.get(language, "Execute Tool", "Chạy công cụ"))
                    }

                    if (testResult != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Verified Execution Output:",
                            style = MaterialTheme.typography.labelSmall,
                            color = CodeGreen
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp)),
                            color = VoidBlack
                        ) {
                            Text(
                                text = testResult,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = TextPrimaryDark,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    activeTestingTool = null
                    onClearTestResult()
                }) {
                    Text(L10n.get(language, "Close", "Đóng"))
                }
            }
        )
    }
}
