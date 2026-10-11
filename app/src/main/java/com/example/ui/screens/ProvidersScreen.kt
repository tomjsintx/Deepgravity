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

@Composable
fun ProvidersScreen(
    providers: List<AiProviderItem>,
    selectedProviderId: String,
    selectedModelId: String,
    language: AppLanguage,
    onSelectModel: (String, String) -> Unit,
    onSaveApiKey: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var configProviderModal by remember { mutableStateOf<AiProviderItem?>(null) }
    var enteredApiKey by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        // Security Banner with Keystore indicator
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = L10n.keystoreSecurityTitle(language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = L10n.keystoreSecurityDesc(language),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = L10n.get(language, "Configured AI Providers & Models", "Nhà cung cấp AI & Danh mục Mô hình"),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Providers List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(providers, key = { it.id }) { provider ->
                val isProviderActive = provider.id == selectedProviderId

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (isProviderActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(12.dp)
                        ),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Provider Header
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
                                        .background(
                                            when (provider.status) {
                                                ProviderStatus.READY -> CodeGreen
                                                ProviderStatus.CONFIG_REQUIRED -> CodeYellow
                                                ProviderStatus.OFFLINE -> CodeRed
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = provider.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = provider.baseUrl,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (provider.pingMs != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CodeGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${provider.pingMs}ms",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CodeGreen
                                        )
                                    }
                                }

                                if (!provider.isLocal) {
                                    OutlinedButton(
                                        onClick = {
                                            configProviderModal = provider
                                            enteredApiKey = ""
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (provider.hasApiKey) "Key ✓" else "Key +", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Models list under provider
                        provider.models.forEach { model ->
                            val isSelected = isProviderActive && model.modelId == selectedModelId

                            Surface(
                                onClick = { onSelectModel(provider.id, model.modelId) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("select_model_${model.modelId}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSelectModel(provider.id, model.modelId) },
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = model.displayName,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (model.supportsThinking) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(NeonPurple.copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "Thinking: HIGH",
                                                        fontSize = 9.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NeonPurpleGlow
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = model.description,
                                            fontSize = 10.sp,
                                            lineHeight = 14.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 28.dp)
                                        )
                                    }

                                    Text(
                                        text = model.contextWindow,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Configure API Key Dialog
    if (configProviderModal != null) {
        val provider = configProviderModal!!
        AlertDialog(
            onDismissRequest = { configProviderModal = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(L10n.get(language, "Configure Key: ${provider.name}", "Cấu hình Khóa: ${provider.name}"))
                }
            },
            text = {
                Column {
                    Text(
                        text = L10n.get(
                            language,
                            "Enter API Key to encrypt and store inside Android KeyStore:",
                            "Nhập khóa API để mã hóa và lưu trữ an toàn trong Android KeyStore:"
                        ),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredApiKey,
                        onValueChange = { enteredApiKey = it },
                        placeholder = { Text("sk-...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveApiKey(provider.id, enteredApiKey.trim())
                        configProviderModal = null
                        Toast.makeText(
                            context,
                            L10n.get(language, "Key encrypted & stored in Keystore", "Khóa đã được mã hóa trong Keystore"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text(L10n.get(language, "Save & Encrypt", "Lưu & Mã hóa"))
                }
            },
            dismissButton = {
                TextButton(onClick = { configProviderModal = null }) {
                    Text(L10n.get(language, "Cancel", "Hủy"))
                }
            }
        )
    }
}
