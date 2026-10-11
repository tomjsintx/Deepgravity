package com.example.workspace

import com.example.model.CodeDiff
import com.example.model.DiffLine
import com.example.model.DiffType

object DiffEngine {

    fun computeDiff(filePath: String, oldContent: String, newContent: String): CodeDiff {
        val oldLines = oldContent.lines()
        val newLines = newContent.lines()

        val lcs = computeLcs(oldLines, newLines)
        val diffLines = mutableListOf<DiffLine>()

        var i = 0
        var j = 0
        var oldLineNum = 1
        var newLineNum = 1
        var additions = 0
        var deletions = 0

        while (i < oldLines.size || j < newLines.size) {
            if (i < oldLines.size && j < newLines.size && oldLines[i] == newLines[j]) {
                diffLines.add(
                    DiffLine(
                        type = DiffType.UNCHANGED,
                        oldLineNumber = oldLineNum++,
                        newLineNumber = newLineNum++,
                        content = oldLines[i]
                    )
                )
                i++
                j++
            } else if (j < newLines.size && (i >= oldLines.size || lcs[i][j + 1] >= lcs[i + 1][j])) {
                diffLines.add(
                    DiffLine(
                        type = DiffType.ADDED,
                        oldLineNumber = null,
                        newLineNumber = newLineNum++,
                        content = "+ ${newLines[j]}"
                    )
                )
                additions++
                j++
            } else if (i < oldLines.size) {
                diffLines.add(
                    DiffLine(
                        type = DiffType.REMOVED,
                        oldLineNumber = oldLineNum++,
                        newLineNumber = null,
                        content = "- ${oldLines[i]}"
                    )
                )
                deletions++
                i++
            }
        }

        val summary = "Modified $filePath (+${additions}, -${deletions})"

        return CodeDiff(
            filePath = filePath,
            summary = summary,
            additions = additions,
            deletions = deletions,
            lines = diffLines
        )
    }

    private fun computeLcs(a: List<String>, b: List<String>): Array<IntArray> {
        val n = a.size
        val m = b.size
        val dp = Array(n + 1) { IntArray(m + 1) }

        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                if (a[i] == b[j]) {
                    dp[i][j] = dp[i + 1][j + 1] + 1
                } else {
                    dp[i][j] = maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }
        return dp
    }
}
