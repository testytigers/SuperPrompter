package com.superprompter.domain

enum class TextAlignment { LEFT, CENTER, RIGHT }

/** Scroll the whole script continuously, or show it one line at a time like cue cards. */
enum class PrompterMode(val label: String) {
    SCROLL("Scroll"),
    LINES("Line by line");

    fun toggled(): PrompterMode = if (this == SCROLL) LINES else SCROLL
}

/** How much of the screen, from the top, the prompter text takes up. */
enum class PrompterSize(val fraction: Float, val label: String) {
    FULL(1f, "Full"),
    HALF(1f / 2f, "1/2"),
    THIRD(1f / 3f, "1/3"),
    QUARTER(1f / 4f, "1/4"),
    FIFTH(1f / 5f, "1/5");

    fun next(): PrompterSize = entries[(ordinal + 1) % entries.size]
}

data class UserPreferences(
    val scrollSpeed: Float = DEFAULT_SCROLL_SPEED,
    val fontSize: Float = DEFAULT_FONT_SIZE,
    val overlayOpacity: Float = DEFAULT_OPACITY,
    val textAlignment: TextAlignment = TextAlignment.CENTER,
    val autoReverse: Boolean = false,
    val mirrorHorizontal: Boolean = false,
    val mirrorVertical: Boolean = false,
    val showWordCounter: Boolean = true,
    val prompterSize: PrompterSize = PrompterSize.FULL,
    val prompterMode: PrompterMode = PrompterMode.SCROLL,
    /** In line-by-line mode, also split paragraphs into one sentence per card. */
    val splitSentences: Boolean = true,
    val activeScriptId: Long? = null,
) {
    companion object {
        const val MIN_SCROLL_SPEED = 10f
        const val MAX_SCROLL_SPEED = 400f
        const val DEFAULT_SCROLL_SPEED = 30f // dp/s; roughly 150 wpm at the default font size

        const val MIN_FONT_SIZE = 14f
        const val MAX_FONT_SIZE = 96f
        const val DEFAULT_FONT_SIZE = 32f

        const val MIN_OPACITY = 0.1f
        const val MAX_OPACITY = 1.0f
        const val DEFAULT_OPACITY = 0.85f
    }
}
