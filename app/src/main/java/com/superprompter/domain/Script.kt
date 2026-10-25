package com.superprompter.domain

import kotlin.math.roundToInt

data class Script(
    val id: Long = 0L,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val wordCount: Int
        get() = countWords(content)

    /** Estimated speaking time at a typical presenting pace. */
    val readingTimeSeconds: Int
        get() = (wordCount * 60f / WORDS_PER_MINUTE).roundToInt()

    companion object {
        const val WORDS_PER_MINUTE = 150
        private val WHITESPACE = "\\s+".toRegex()

        fun countWords(text: String): Int =
            if (text.isBlank()) 0 else text.trim().split(WHITESPACE).size
    }
}

fun formatDuration(totalSeconds: Int): String =
    "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
