package com.example.localization

import com.example.model.AppLanguage

object L10n {
    fun get(lang: AppLanguage, en: String, vi: String): String {
        return if (lang == AppLanguage.VI) vi else en
    }

    // Common navigation labels
    fun agentChat(lang: AppLanguage) = get(lang, "Agent Chat", "Trò chuyện Agent")
    fun workspace(lang: AppLanguage) = get(lang, "Workspace", "Không gian làm việc")
    fun computer(lang: AppLanguage) = get(lang, "Computer", "Máy tính ảo")
    fun terminal(lang: AppLanguage) = get(lang, "Terminal", "Dòng lệnh Terminal")
    fun tools(lang: AppLanguage) = get(lang, "Tools & MCP", "Công cụ & MCP")
    fun providers(lang: AppLanguage) = get(lang, "Providers", "Nhà cung cấp AI")
    fun settings(lang: AppLanguage) = get(lang, "Settings", "Cài đặt")

    fun promptPlaceholder(lang: AppLanguage) = get(
        lang,
        "Ask DeepGravity to write code, debug, inspect tests...",
        "Yêu cầu DeepGravity viết code, sửa lỗi, chạy test..."
    )

    fun send(lang: AppLanguage) = get(lang, "Send", "Gửi")
    fun thinking(lang: AppLanguage) = get(lang, "Thinking Process", "Tiến trình suy luận")
    fun toolsInvoked(lang: AppLanguage) = get(lang, "Tools Invoked", "Công cụ đã gọi")
    fun applyToWorkspace(lang: AppLanguage) = get(lang, "Apply to Workspace", "Áp dụng vào Workspace")
    fun copyCode(lang: AppLanguage) = get(lang, "Copy Code", "Sao chép Code")
    fun runTest(lang: AppLanguage) = get(lang, "Verify Build", "Kiểm tra biên dịch")
    fun files(lang: AppLanguage) = get(lang, "Files", "Tập tin")
    fun diff(lang: AppLanguage) = get(lang, "Diff Viewer", "So sánh Thay đổi")
    fun shizukuStatus(lang: AppLanguage) = get(lang, "Shizuku ADB Status", "Trạng thái Shizuku ADB")
    fun activeModel(lang: AppLanguage) = get(lang, "Active Model", "Mô hình đang dùng")
    fun reasoningMode(lang: AppLanguage) = get(lang, "Reasoning Engine", "Cơ chế Suy luận")

    // Security & Safe mode
    fun safeModeTitle(lang: AppLanguage) = get(lang, "Safe Mode (Approval Gate)", "Chế độ An toàn (Duyệt trước)")
    fun safeModeDesc(lang: AppLanguage) = get(
        lang,
        "Requires explicit user confirmation before executing destructive file writes, shell commands, or ADB injections.",
        "Yêu cầu xác nhận từ người dùng trước khi ghi tệp, chạy lệnh shell hoặc thao tác ADB."
    )
    fun keystoreSecurityTitle(lang: AppLanguage) = get(lang, "Hardware Keystore Encryption", "Mã hóa Phần cứng Keystore")
    fun keystoreSecurityDesc(lang: AppLanguage) = get(
        lang,
        "All API keys are encrypted with AES-256-GCM via Android KeyStore before persisting. Keys never leak to cloud or logs.",
        "Mọi khóa API đều được mã hóa AES-256-GCM bằng Android KeyStore trước khi lưu. Không rò rỉ lên cloud hay log."
    )
    fun confirmExecutionTitle(lang: AppLanguage) = get(lang, "Confirm Tool Execution", "Xác nhận Thực thi Công cụ")
    fun approve(lang: AppLanguage) = get(lang, "Approve & Execute", "Duyệt & Thực thi")
    fun reject(lang: AppLanguage) = get(lang, "Reject", "Từ chối")

    // Editor & Terminal
    fun saveFile(lang: AppLanguage) = get(lang, "Save File", "Lưu tệp")
    fun newFile(lang: AppLanguage) = get(lang, "New File", "Tệp mới")
    fun deleteFile(lang: AppLanguage) = get(lang, "Delete File", "Xóa tệp")
}
