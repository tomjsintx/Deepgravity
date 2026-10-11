package com.example.model

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    EN("en", "English", "🇺🇸"),
    VI("vi", "Tiếng Việt", "🇻🇳")
}

enum class AppThemeMode(val displayName: String, val displayNameVi: String) {
    DARK("Cyber Dark", "Tối Không gian"),
    LIGHT("Clean Light", "Sáng Hiện đại"),
    CYBER_OLED("Obsidian OLED", "Đen Huyền bí")
}

enum class ThinkingLevel(val displayName: String, val levelNumber: Int) {
    OFF("Off", 0),
    LOW("Low", 1),
    HIGH("High (Apex Reasoner v2)", 2)
}
