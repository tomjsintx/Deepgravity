package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.L10n
import com.example.model.AppLanguage
import com.example.model.TerminalEntry
import com.example.ui.theme.*

@Composable
fun TerminalScreen(
    entries: List<TerminalEntry>,
    language: AppLanguage,
    onExecuteCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickCommands = listOf(
        "gradle check",
        "git status",
        "git diff",
        "shizuku status",
        "ls -la",
        "pwd",
        "mcp list",
        "clear"
    )

    val terminalKeys = listOf("Ctrl+C", "Esc", "Tab", "|", ">", "&", "..", "clear")

    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) {
            listState.animateScrollToItem(entries.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // Quick command suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickCommands.forEach { cmd ->
                SuggestionChip(
                    onClick = { onExecuteCommand(cmd) },
                    label = {
                        Text(
                            text = cmd,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = NeonCyan
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = SurfaceDarkElevated
                    )
                )
            }
        }

        // Terminal Scrollback
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "DeepGravity Linux Terminal Engine [Kernel 6.1-android15 / arm64-v8a]\nSandboxed inside app workspace. Real ProcessBuilder execution active.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextMutedDark,
                    lineHeight = 16.sp
                )
            }

            items(entries, key = { it.id }) { entry ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "deepgravity@android:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CodeGreen
                        )
                        Text(
                            text = "${entry.workingDir}$ ",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = entry.command,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    if (entry.output.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.output,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = if (entry.exitCode == 0) TextSecondaryDark else CodeRed,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }

        // Virtual Touch Key Accessory Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            terminalKeys.forEach { keyLabel ->
                Surface(
                    onClick = {
                        when (keyLabel) {
                            "clear" -> onExecuteCommand("clear")
                            "Ctrl+C" -> { commandInput = "" }
                            "Tab" -> { commandInput += "    " }
                            else -> { commandInput += " $keyLabel " }
                        }
                    },
                    shape = RoundedCornerShape(4.dp),
                    color = SurfaceDarkElevated,
                    modifier = Modifier.height(26.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = keyLabel,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }
            }
        }

        // Bottom Command Input
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDarkElevated
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ ",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeonCyan
                )

                TextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    placeholder = {
                        Text(
                            text = L10n.get(language, "Enter shell command...", "Nhập lệnh shell..."),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMutedDark
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextPrimaryDark
                    ),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_input_field")
                )

                IconButton(
                    onClick = {
                        if (commandInput.isNotBlank()) {
                            onExecuteCommand(commandInput)
                            commandInput = ""
                        }
                    },
                    modifier = Modifier.testTag("terminal_run_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run",
                        tint = NeonCyan
                    )
                }
            }
        }
    }
}
