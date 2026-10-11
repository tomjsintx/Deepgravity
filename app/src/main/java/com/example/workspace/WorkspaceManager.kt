package com.example.workspace

import android.content.Context
import com.example.model.WorkspaceFile
import java.io.File

class WorkspaceManager(private val context: Context) {

    val workspaceRoot: File = File(context.filesDir, "workspace").apply {
        if (!exists()) mkdirs()
    }

    init {
        ensureInitialProjectFiles()
    }

    private fun ensureInitialProjectFiles() {
        val mainActivity = File(workspaceRoot, "app/src/main/java/com/deepgravity/agent/MainActivity.kt")
        if (!mainActivity.exists()) {
            mainActivity.parentFile?.mkdirs()
            mainActivity.writeText(
                """
package com.deepgravity.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DeepGravityApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DeepGravityApp(modifier: Modifier = Modifier) {
    var count by remember { mutableStateOf(0) }
    Column(modifier = modifier.padding(16.dp)) {
        Text("DeepGravity Autonomous Mobile Agent", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { count++ }) {
            Text("Execution Count: ${'$'}count")
        }
    }
}
                """.trimIndent()
            )
        }

        val viewModelFile = File(workspaceRoot, "app/src/main/java/com/deepgravity/agent/ui/CounterViewModel.kt")
        if (!viewModelFile.exists()) {
            viewModelFile.parentFile?.mkdirs()
            viewModelFile.writeText(
                """
package com.deepgravity.agent.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CounterViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    fun increment() {
        _count.value += 1
    }

    fun reset() {
        _count.value = 0
    }
}
                """.trimIndent()
            )
        }

        val gradleFile = File(workspaceRoot, "app/build.gradle.kts")
        if (!gradleFile.exists()) {
            gradleFile.parentFile?.mkdirs()
            gradleFile.writeText(
                """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.deepgravity.agent"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.deepgravity.agent"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
}
                """.trimIndent()
            )
        }

        val manifestFile = File(workspaceRoot, "app/src/main/AndroidManifest.xml")
        if (!manifestFile.exists()) {
            manifestFile.parentFile?.mkdirs()
            manifestFile.writeText(
                """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:label="DeepGravity"
        android:supportsRtl="true">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
                """.trimIndent()
            )
        }
    }

    fun getAllWorkspaceFiles(): List<WorkspaceFile> {
        val result = mutableListOf<WorkspaceFile>()
        workspaceRoot.walkTopDown().forEach { file ->
            if (file.isFile) {
                val relPath = file.relativeTo(workspaceRoot).path
                val lang = detectLanguage(file.name)
                val content = try { file.readText() } catch (e: Exception) { "" }
                result.add(
                    WorkspaceFile(
                        path = relPath,
                        name = file.name,
                        isDirectory = false,
                        content = content,
                        language = lang
                    )
                )
            }
        }
        return result.sortedBy { it.path }
    }

    fun readFile(relativePath: String): String? {
        val file = File(workspaceRoot, relativePath)
        return if (file.exists() && file.isFile) file.readText() else null
    }

    fun writeFile(relativePath: String, content: String): Boolean {
        return try {
            val file = File(workspaceRoot, relativePath)
            file.parentFile?.mkdirs()
            val tempFile = File(file.parentFile, "${file.name}.tmp_${System.currentTimeMillis()}")
            tempFile.writeText(content)
            
            // Validate byte integrity before atomic rename
            if (tempFile.length() != content.toByteArray(Charsets.UTF_8).size.toLong()) {
                tempFile.delete()
                return false
            }
            
            val renamed = tempFile.renameTo(file)
            if (!renamed) {
                tempFile.copyTo(file, overwrite = true)
                tempFile.delete()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteFile(relativePath: String): Boolean {
        return try {
            val file = File(workspaceRoot, relativePath)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    private fun detectLanguage(filename: String): String {
        return when {
            filename.endsWith(".kt") -> "kotlin"
            filename.endsWith(".kts") -> "gradle"
            filename.endsWith(".xml") -> "xml"
            filename.endsWith(".json") -> "json"
            filename.endsWith(".md") -> "markdown"
            filename.endsWith(".sh") -> "shell"
            else -> "text"
        }
    }
}
