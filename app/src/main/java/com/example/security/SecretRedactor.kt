package com.example.security

import java.util.regex.Pattern

object SecretRedactor {

    private val patterns = listOf(
        // Generic API keys like sk-..., AIza..., etc.
        Pattern.compile("""(?i)(sk-[a-zA-Z0-9_\-]{20,})"""),
        Pattern.compile("""(?i)(AIza[0-9A-Za-z\-_]{35})"""),
        Pattern.compile("""(?i)(bearer\s+[a-zA-Z0-9_\-\.]{20,})"""),
        Pattern.compile("""(?i)(x-api-key\s*:\s*[a-zA-Z0-9_\-]{20,})"""),
        Pattern.compile("""(?i)(password\s*=\s*['"][^'"]+['"])""")
    )

    fun redact(text: String): String {
        var result = text
        for (pattern in patterns) {
            val matcher = pattern.matcher(result)
            val sb = StringBuffer()
            while (matcher.find()) {
                val group = matcher.group()
                val masked = if (group.length > 8) {
                    group.take(3) + "••••••••" + group.takeLast(4)
                } else {
                    "••••••••"
                }
                matcher.appendReplacement(sb, MatcherQuoteReplacement(masked))
            }
            matcher.appendTail(sb)
            result = sb.toString()
        }
        return result
    }

    private fun MatcherQuoteReplacement(s: String): String {
        return java.util.regex.Matcher.quoteReplacement(s)
    }
}
