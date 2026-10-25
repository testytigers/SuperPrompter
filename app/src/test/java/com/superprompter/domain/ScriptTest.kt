package com.superprompter.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ScriptTest {

    @Test
    fun `word count is zero for blank content`() {
        assertEquals(0, Script.countWords(""))
        assertEquals(0, Script.countWords("   \n\t  "))
    }

    @Test
    fun `word count ignores repeated and surrounding whitespace`() {
        assertEquals(4, Script.countWords("  Hello   there,\n\nbig\tworld  "))
    }

    @Test
    fun `reading time uses 150 words per minute`() {
        val script = Script(title = "t", content = List(150) { "word" }.joinToString(" "))
        assertEquals(60, script.readingTimeSeconds)

        val short = Script(title = "t", content = List(75) { "word" }.joinToString(" "))
        assertEquals(30, short.readingTimeSeconds)
    }

    @Test
    fun `durations are formatted as minutes and padded seconds`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:07", formatDuration(7))
        assertEquals("1:05", formatDuration(65))
        assertEquals("12:00", formatDuration(720))
    }
}
