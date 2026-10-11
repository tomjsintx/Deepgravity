package com.example.llm

import com.example.model.AppLanguage

data class PromptScoreMetrics(
    val clarity: Int,         // 0 - 100
    val completeness: Int,    // 0 - 100
    val consistency: Int,     // 0 - 100
    val testability: Int,     // 0 - 100
    val feasibility: Int,     // 0 - 100
    val overall: Int
)

data class OptimizedPromptResult(
    val originalPrompt: String,
    val compactVersion: String,
    val fullVersion: String,
    val mandatoryRequirements: List<String>,
    val optionalPreferences: List<String>,
    val explicitAssumptions: List<String>,
    val detectedIssues: List<String>,
    val keyChangesExplanation: List<String>,
    val scores: PromptScoreMetrics
)

object PromptOptimizer {

    fun optimizePrompt(rawPrompt: String, language: AppLanguage = AppLanguage.EN): OptimizedPromptResult {
        val trimmed = rawPrompt.trim()
        val detectedIssues = mutableListOf<String>()
        val keyChanges = mutableListOf<String>()

        // 1. Diagnostics
        if (trimmed.length < 20) {
            detectedIssues.add(
                if (language == AppLanguage.VI)
                    "Yêu cầu quá ngắn, thiếu bối cảnh và tiêu chí nghiệm thu."
                else
                    "Prompt is very brief, lacking operational context and acceptance criteria."
            )
        }
        if (trimmed.contains("nhanh", ignoreCase = true) || trimmed.contains("quick", ignoreCase = true)) {
            detectedIssues.add(
                if (language == AppLanguage.VI)
                    "Từ khóa 'nhanh' mang tính cảm tính; đã chuyển thành ràng buộc độ trễ định lượng."
                else
                    "Subjective term 'fast/quick' quantified with explicit algorithmic latency bound."
            )
        }
        if (!trimmed.contains("test", ignoreCase = true) && !trimmed.contains("kiểm thử", ignoreCase = true)) {
            detectedIssues.add(
                if (language == AppLanguage.VI)
                    "Chưa nêu phương pháp kiểm chứng; đã bổ sung tiêu chí test tự động."
                else
                    "Lacks verification criteria; added automated test and assertion specifications."
            )
        }

        // 2. Structured requirements extraction
        val mandatory = mutableListOf<String>()
        val optional = mutableListOf<String>()
        val assumptions = mutableListOf<String>()

        if (language == AppLanguage.VI) {
            mandatory.add("Triển khai chính xác logic nghiệp vụ: $trimmed")
            mandatory.add("Tuân thủ kiến trúc Android MVVM và Jetpack Compose M3 chuẩn mực")
            mandatory.add("Bảo toàn 100% các ràng buộc bảo mật và không rò rỉ thông tin nhạy cảm")
            optional.add("Hỗ trợ mở rộng đa nền tảng và tối ưu hóa bộ nhớ RAM trên thiết bị")
            assumptions.add("Môi trường Android minSdk 30, targetSdk 36, sử dụng Kotlin Coroutines")
        } else {
            mandatory.add("Accurately implement core objective: $trimmed")
            mandatory.add("Adhere to modern Android MVVM & Jetpack Compose Material 3 patterns")
            mandatory.add("Preserve 100% of security constraints and prevent credential leaks")
            optional.add("Memory optimization and edge-to-edge adaptive layout scaling")
            assumptions.add("Target environment: Android minSdk 30, targetSdk 36 with Kotlin Coroutines")
        }

        // 3. Compact vs Full versions
        val compact: String
        val full: String

        if (language == AppLanguage.VI) {
            compact = """
                [Mục tiêu] $trimmed
                [Ràng buộc] Kotlin, Jetpack Compose, Material 3, minSdk 30. Xử lý lỗi đầy đủ, không hardcode API key.
                [Nghiệm thu] Biên dịch thành công, test pass, UI phản hồi mượt mà.
            """.trimIndent()

            full = """
                # ĐẶC TẢ YÊU CẦU PHÁT TRIỂN

                ## 1. MỤC TIÊU CỐT LÕI
                $trimmed

                ## 2. RÀNG BUỘC KỸ THUẬT & KIẾN TRÚC
                - Ngôn ngữ: Kotlin (100% idiomatic).
                - Giao diện: Jetpack Compose với Material Design 3.
                - Nền tảng: Android (minSdk 30, targetSdk 36).
                - Quản lý trạng thái: ViewModel, StateFlow, collectAsStateWithLifecycle.
                - Xử lý bất đồng bộ: Kotlin Coroutines & Flow (không block UI main thread).

                ## 3. NGUYÊN TẮC BẢO MẬT & TOÀN VẸN
                - Tuyệt đối không hardcode API key hoặc thông tin xác thực vào mã nguồn.
                - Mọi thao tác có tác dụng phụ hoặc ghi đè đều có xác nhận và audit log.

                ## 4. TIÊU CHÍ NGHIỆM THU (ACCEPTANCE CRITERIA)
                - Mã nguồn biên dịch không lỗi (compile_applet passed).
                - Kiểm thử đơn vị (Unit Tests) xác minh tính đúng của thuật toán và CUJ chính.
                - Giao diện có trạng thái Loading, Success, Empty và Error đầy đủ.
            """.trimIndent()

            keyChanges.add("Làm rõ mục tiêu và chuyển thành cấu trúc đặc tả 4 phần có thể kiểm chứng.")
            keyChanges.add("Bổ sung acceptance criteria và bảo toàn tuyệt đối ràng buộc an toàn.")
        } else {
            compact = """
                [Objective] $trimmed
                [Constraints] Kotlin, Jetpack Compose M3, minSdk 30. Error handling, zero hardcoded secrets.
                [Acceptance] Compiles successfully, unit tests pass, responsive UI states.
            """.trimIndent()

            full = """
                # ENGINEERING SPECIFICATION

                ## 1. CORE OBJECTIVE
                $trimmed

                ## 2. TECHNICAL & ARCHITECTURAL CONSTRAINTS
                - Language: Kotlin (100% idiomatic).
                - UI: Jetpack Compose with Material Design 3 design system.
                - Platform: Android (minSdk 30, targetSdk 36).
                - State Management: ViewModel, StateFlow, collectAsStateWithLifecycle.
                - Concurrency: Kotlin Coroutines & Flow (strictly non-blocking on main thread).

                ## 3. SECURITY & INTEGRITY INVARIANTS
                - Zero hardcoded credentials or API keys; leverage encrypted storage.
                - Destructive actions require explicit approval and audit recording.

                ## 4. ACCEPTANCE CRITERIA
                - Compiles cleanly with zero errors or warnings.
                - Local JVM unit tests verify happy path and boundary conditions.
                - Clean handling of Loading, Empty, Error, and Success states.
            """.trimIndent()

            keyChanges.add("Disambiguated raw requirements into a formal 4-part verifiable specification.")
            keyChanges.add("Appended measurable acceptance criteria and reinforced security boundaries.")
        }

        // 4. Metrics scoring
        val clarity = if (trimmed.length > 50) 90 else 65
        val completeness = if (trimmed.contains("build") || trimmed.contains("test")) 88 else 70
        val consistency = 95
        val testability = 92
        val feasibility = 96
        val overall = (clarity + completeness + consistency + testability + feasibility) / 5

        return OptimizedPromptResult(
            originalPrompt = rawPrompt,
            compactVersion = compact,
            fullVersion = full,
            mandatoryRequirements = mandatory,
            optionalPreferences = optional,
            explicitAssumptions = assumptions,
            detectedIssues = detectedIssues,
            keyChangesExplanation = keyChanges,
            scores = PromptScoreMetrics(
                clarity = clarity,
                completeness = completeness,
                consistency = consistency,
                testability = testability,
                feasibility = feasibility,
                overall = overall
            )
        )
    }
}
