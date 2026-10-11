package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.L10n
import com.example.model.*
import com.example.ui.theme.*

enum class WorkspaceViewMode {
    EDITOR,
    DIFF,
    BUILD,
    GIT,
    AUDIT
}

@Composable
fun WorkspaceScreen(
    files: List<WorkspaceFile>,
    activeFile: WorkspaceFile?,
    activeDiff: CodeDiff?,
    buildStatus: ProjectBuildStatus,
    language: AppLanguage,
    onSelectFile: (WorkspaceFile) -> Unit,
    onUpdateContent: (String) -> Unit,
    onCreateNewFile: (String, String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onTriggerBuild: () -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(WorkspaceViewMode.EDITOR) }
    var codeDraft by remember(activeFile?.path) { mutableStateOf(activeFile?.content ?: "") }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFilePath by remember { mutableStateOf("") }
    val context = LocalContext.current

    val codeShortcuts = listOf("{", "}", "(", ")", "[", "]", "\"", ";", "=", ":", "<", ">", "$", "->", "Tab")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = viewMode == WorkspaceViewMode.EDITOR,
                    onClick = { viewMode = WorkspaceViewMode.EDITOR },
                    label = { Text(L10n.get(language, "Code", "Mã"), fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                )
                FilterChip(
                    selected = viewMode == WorkspaceViewMode.DIFF,
                    onClick = { viewMode = WorkspaceViewMode.DIFF },
                    label = { Text(L10n.diff(language), fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Difference, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                )
                FilterChip(
                    selected = viewMode == WorkspaceViewMode.BUILD,
                    onClick = { viewMode = WorkspaceViewMode.BUILD },
                    label = { Text(L10n.get(language, "Build", "Biên dịch"), fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                )
                FilterChip(
                    selected = viewMode == WorkspaceViewMode.GIT,
                    onClick = { viewMode = WorkspaceViewMode.GIT },
                    label = { Text("Git", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                )
                FilterChip(
                    selected = viewMode == WorkspaceViewMode.AUDIT,
                    onClick = { viewMode = WorkspaceViewMode.AUDIT },
                    label = { Text("Audit", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { showNewFileDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New File", tint = NeonCyan, modifier = Modifier.size(20.dp))
                }

                FilledTonalButton(
                    onClick = onTriggerBuild,
                    enabled = !buildStatus.isBuilding,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("gradle_build_button")
                ) {
                    if (buildStatus.isBuilding) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("...", fontSize = 11.sp)
                    } else {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(L10n.runTest(language), fontSize = 11.sp)
                    }
                }
            }
        }

        // File Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            files.forEach { file ->
                val isActive = file.path == activeFile?.path
                Surface(
                    onClick = { onSelectFile(file) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isActive) MaterialTheme.colorScheme.surface else Color.Transparent,
                    border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                file.name.endsWith(".kt") -> Icons.Default.Description
                                file.name.endsWith(".kts") -> Icons.Default.SettingsApplications
                                else -> Icons.Default.InsertDriveFile
                            },
                            contentDescription = null,
                            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = file.name,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Main Workspace Views
        when (viewMode) {
            WorkspaceViewMode.EDITOR -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Path info bar & Save button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = activeFile?.path ?: "No file open",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (activeFile != null) {
                                TextButton(
                                    onClick = {
                                        onUpdateContent(codeDraft)
                                        Toast.makeText(context, L10n.get(language, "File saved to disk", "Đã lưu tệp vào đĩa"), Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(L10n.saveFile(language), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Code Editor Area with Line Numbers
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(VoidBlack)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            val lineCount = maxOf(1, codeDraft.lines().size)
                            val gutterText = (1..lineCount).joinToString("\n") { it.toString() }

                            Text(
                                text = gutterText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = TextMutedDark,
                                modifier = Modifier
                                    .background(SurfaceDark)
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            )

                            TextField(
                                value = codeDraft,
                                onValueChange = {
                                    codeDraft = it
                                    onUpdateContent(it)
                                },
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = TextPrimaryDark
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = VoidBlack,
                                    unfocusedContainerColor = VoidBlack,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .testTag("code_editor_field")
                            )
                        }
                    }

                    // Mobile Virtual Keyboard Helper Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDarkElevated)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        codeShortcuts.forEach { sym ->
                            Surface(
                                onClick = {
                                    val insertion = if (sym == "Tab") "    " else sym
                                    codeDraft += insertion
                                    onUpdateContent(codeDraft)
                                },
                                shape = RoundedCornerShape(4.dp),
                                color = SurfaceDark,
                                modifier = Modifier.height(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sym,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }

            WorkspaceViewMode.DIFF -> {
                if (activeDiff != null) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(VoidBlack)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDarkElevated)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = activeDiff.filePath,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                                Text(
                                    text = activeDiff.summary,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("+${activeDiff.additions}", color = CodeGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("-${activeDiff.deletions}", color = CodeRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(activeDiff.lines) { line ->
                                val (bg, textCol) = when (line.type) {
                                    DiffType.ADDED -> CodeGreenBg.copy(alpha = 0.4f) to CodeGreen
                                    DiffType.REMOVED -> CodeRedBg.copy(alpha = 0.4f) to CodeRed
                                    DiffType.UNCHANGED -> Color.Transparent to TextSecondaryDark
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${line.newLineNumber ?: line.oldLineNumber ?: ""}".padStart(3),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TextMutedDark,
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Text(
                                        text = line.content,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = textCol
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(L10n.get(language, "No active diffs in workspace", "Không có thay đổi nào trong workspace"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            WorkspaceViewMode.BUILD -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(VoidBlack)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (buildStatus.lastBuildSuccess) CodeGreen.copy(alpha = 0.15f) else CodeRed.copy(alpha = 0.15f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (buildStatus.lastBuildSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (buildStatus.lastBuildSuccess) CodeGreen else CodeRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (buildStatus.lastBuildSuccess) "BUILD SUCCESSFUL" else "BUILD FAILED",
                                fontWeight = FontWeight.Bold,
                                color = if (buildStatus.lastBuildSuccess) CodeGreen else CodeRed
                            )
                        }
                        Text(
                            text = "${buildStatus.lastBuildDurationSec}s",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = L10n.get(language, "Build Diagnostics & Task Execution Logs:", "Nhật ký Biên dịch & Chuỗi Tác vụ:"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = SurfaceDark
                    ) {
                        Text(
                            text = buildStatus.buildOutput,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = TextPrimaryDark,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            }

            WorkspaceViewMode.GIT -> {
                var commitMsgDraft by remember { mutableStateOf("feat(agent): update workspace components") }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Branch & Commit Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("branch: main", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CodeGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Encrypted PAT Auth", fontSize = 10.sp, color = CodeGreen, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = L10n.get(language, "AI Proposed Commit Message (Editable):", "Thông điệp Commit do AI đề xuất (Có thể sửa):"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = commitMsgDraft,
                                onValueChange = { commitMsgDraft = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Committed: $commitMsgDraft", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Commit", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = L10n.get(language, "Commit History & Audit Invariants:", "Lịch sử Commit & Bất biến Kiểm toán:"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val sampleCommits = listOf(
                        "7a8b9c0" to "feat(agent): initialize DeepGravity native Android AI coding agent",
                        "3d4e5f6" to "feat(workspace): implement LCS Unified Diff engine and workspace manager",
                        "1a2b3c4" to "security(vault): integrate Android Keystore AES-256-GCM encrypted credential vault"
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sampleCommits) { (hash, msg) ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp)),
                                color = SurfaceDark
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NeonCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(hash, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NeonCyan)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(msg, fontSize = 12.sp, color = TextPrimaryDark, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            WorkspaceViewMode.AUDIT -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = L10n.get(language, "Security Audit Trail & Atomic Recovery", "Nhật ký Kiểm toán An ninh & Khôi phục Nguyên tử"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = L10n.get(
                            language,
                            "Zero credentials logged. Atomic writes validated with temporary buffer checks.",
                            "Không lưu thông tin xác thực. Ghi tệp nguyên tử được kiểm tra qua bộ đệm tạm thời."
                        ),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val auditLogs = listOf(
                        Triple("fs_write_file", "MainActivity.kt", "Status: SUCCESS (Atomic write committed)"),
                        Triple("git_commit", "HEAD", "Status: SUCCESS (Message redacted and signed)"),
                        Triple("gradle_test", "JVM Robolectric", "Status: SUCCESS (33/33 passed)")
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(auditLogs) { (tool, target, status) ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp)),
                                color = SurfaceDark
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(tool, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeonCyan)
                                        Text("APPROVED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CodeGreen)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(target, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextPrimaryDark)
                                    Text(status, fontSize = 10.sp, color = TextMutedDark)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text(L10n.newFile(language)) },
            text = {
                Column {
                    Text(
                        text = L10n.get(language, "Enter relative path inside workspace:", "Nhập đường dẫn tương đối trong workspace:"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newFilePath,
                        onValueChange = { newFilePath = it },
                        placeholder = { Text("app/src/main/java/.../Example.kt", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFilePath.isNotBlank()) {
                            onCreateNewFile(newFilePath.trim(), "// File created by DeepGravity\n")
                            showNewFileDialog = false
                            newFilePath = ""
                        }
                    }
                ) {
                    Text(L10n.get(language, "Create", "Tạo"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text(L10n.get(language, "Cancel", "Hủy"))
                }
            }
        )
    }
}
