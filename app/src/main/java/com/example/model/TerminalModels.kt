package com.example.model

data class TerminalEntry(
    val id: String,
    val command: String,
    val workingDir: String,
    val output: String,
    val exitCode: Int,
    val timestamp: Long = System.currentTimeMillis()
)
