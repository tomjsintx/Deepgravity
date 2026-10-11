package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.AgentState
import com.example.agent.AgentThinkingLevel
import com.example.localization.L10n
import com.example.model.*
import com.example.security.PendingApprovalAction
import com.example.security.RiskLevel
import com.example.ui.theme.*

@Composable
fun AgentChatScreen(
    messages: List<ChatMessage>,
    activeModel: String,
    language: AppLanguage,
    thinkingLevel: ThinkingLevel,
    agentState: AgentState,
    pendingApproval: PendingApprovalAction?,
    onSendMessage: (String) -> Unit,
    onCancelAgent: () -> Unit,
    onApplyCodeToWorkspace: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var showOptimizerDialog by remember { mutableStateOf(false) }
    var optimizerResult by remember { mutableStateOf<com.example.llm.OptimizedPromptResult?>(null) }
    var optimizerTab by remember { mutableStateOf(0) } // 0: Compact, 1: Full
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    if (showOptimizerDialog && optimizerResult != null) {
        val result = optimizerResult!!
        AlertDialog(
            onDismissRequest = { showOptimizerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.VI) "Apex Prompt Optimizer" else "Apex Prompt Optimizer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quality Score Cards
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Score", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${result.scores.overall}/100", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Clarity", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${result.scores.clarity}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CodeGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Feasibility", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${result.scores.feasibility}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
                            }
                        }
                    }

                    // Version Toggle Tabs
                    TabRow(selectedTabIndex = optimizerTab) {
                        Tab(
                            selected = optimizerTab == 0,
                            onClick = { optimizerTab = 0 },
                            text = { Text(if (language == AppLanguage.VI) "Bản gọn" else "Compact", fontSize = 12.sp) }
                        )
                        Tab(
                            selected = optimizerTab == 1,
                            onClick = { optimizerTab = 1 },
                            text = { Text(if (language == AppLanguage.VI) "Bản đầy đủ" else "Full Spec", fontSize = 12.sp) }
                        )
                    }

                    // Content Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        color = SurfaceDark,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceDarkBorder)
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            item {
                                Text(
                                    text = if (optimizerTab == 0) result.compactVersion else result.fullVersion,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = TextPrimaryDark
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        inputText = if (optimizerTab == 0) result.compactVersion else result.fullVersion
                        showOptimizerDialog = false
                    }
                ) {
                    Text(if (language == AppLanguage.VI) "Áp dụng vào Prompt" else "Apply to Input")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOptimizerDialog = false }) {
                    Text(if (language == AppLanguage.VI) "Đóng" else "Close")
                }
            }
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestions = if (language == AppLanguage.VI) {
        listOf(
            "Tối ưu Jetpack Compose counter",
            "Chạy Gradle build kiểm tra lỗi",
            "Kiểm tra kết nối Shizuku ADB",
            "Phân tích cấu trúc thư mục workspace"
        )
    } else {
        listOf(
            "Refactor MainActivity with ViewModel",
            "Run Gradle build verification",
            "Check Shizuku ADB daemon status",
            "Analyze workspace file structure"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Suggestions Chips Bar & Stop Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { prompt ->
                    SuggestionChip(
                        onClick = { onSendMessage(prompt) },
                        label = { Text(prompt, fontSize = 11.sp) },
                        icon = {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(13.dp)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            if (agentState == AgentState.THINKING || agentState == AgentState.EXECUTING_TOOL) {
                Spacer(modifier = Modifier.width(6.dp))
                FilledTonalButton(
                    onClick = onCancelAgent,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CodeRedBg,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Approval Action Card (if waiting approval)
        if (pendingApproval != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CodeYellow, RoundedCornerShape(12.dp)),
                color = SurfaceDarkElevated
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CodeYellow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = L10n.confirmExecutionTitle(language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CodeYellow
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == AppLanguage.VI) pendingApproval.impactDescriptionVi else pendingApproval.impactDescriptionEn,
                        fontSize = 11.sp,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Target: ${pendingApproval.target} • Scope: ${pendingApproval.scope}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMutedDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = pendingApproval.onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = CodeGreen),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(L10n.approve(language), fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = pendingApproval.onReject,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(L10n.reject(language), fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    language = language,
                    onCopyCode = { code ->
                        clipboardManager.setText(AnnotatedString(code))
                        Toast.makeText(
                            context,
                            if (language == AppLanguage.VI) "Đã sao chép mã" else "Code copied to clipboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onApplyCode = { code, path ->
                        onApplyCodeToWorkspace(code, path)
                        Toast.makeText(
                            context,
                            if (language == AppLanguage.VI) "Đã áp dụng vào Workspace" else "Applied to Workspace",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }

        // Input field
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = L10n.promptPlaceholder(language),
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                IconButton(
                    onClick = {
                        val promptToOptimize = if (inputText.isNotBlank()) inputText else "Create a reactive MVVM counter in Jetpack Compose"
                        optimizerResult = com.example.llm.PromptOptimizer.optimizePrompt(promptToOptimize, language)
                        showOptimizerDialog = true
                    },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Optimize Prompt",
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("send_message_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    language: AppLanguage,
    onCopyCode: (String) -> Unit,
    onApplyCode: (String, String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    var isThinkingExpanded by remember { mutableStateOf(message.thinkingExpanded) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Message Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 4.dp, start = if (isUser) 0.dp else 4.dp, end = if (isUser) 4.dp else 0.dp)
        ) {
            Text(
                text = if (isUser) L10n.get(language, "You", "Bạn") else "DeepGravity Agent",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isUser) MaterialTheme.colorScheme.primary else NeonCyan
            )
            if (!isUser && message.modelUsed != null) {
                Text(
                    text = "• ${message.modelUsed}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isUser && message.latencyMs != null) {
                Text(
                    text = "• ${message.latencyMs}ms",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CodeGreen
                )
            }
        }

        // Thinking Card (if available)
        if (!isUser && message.thinkingProcess != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { isThinkingExpanded = !isThinkingExpanded },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .animateContentSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = NeonPurpleGlow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = L10n.thinking(language) + " (Apex Reasoner v2)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonPurpleGlow
                            )
                        }
                        Icon(
                            imageVector = if (isThinkingExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = NeonPurpleGlow,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isThinkingExpanded) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = message.thinkingProcess,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Tool Invocations
        if (!isUser && message.toolInvocations.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.toolInvocations.forEach { tool ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(CodeGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${tool.toolName} • ${tool.summary}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = tool.result,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${tool.durationMs}ms",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CodeGreen
                            )
                        }
                    }
                }
            }
        }

        // Main Bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Code Snippets (if any)
        if (!isUser && message.codeSnippets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            message.codeSnippets.forEach { snippet ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    color = VoidBlack
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDarkElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = snippet.title,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = { onCopyCode(snippet.code) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(L10n.copyCode(language), fontSize = 11.sp)
                                }

                                if (snippet.targetPath != null) {
                                    FilledTonalButton(
                                        onClick = { onApplyCode(snippet.code, snippet.targetPath) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(L10n.applyToWorkspace(language), fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Text(
                            text = snippet.code,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = TextPrimaryDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}
