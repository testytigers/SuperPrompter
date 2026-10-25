package com.superprompter.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ScriptLinesTest {

    @Test
    fun `each typed line becomes a card and blank lines are skipped`() {
        val script = "Hello everyone\n\n   \nWelcome back\n  Let's start  "

        assertEquals(
            listOf("Hello everyone", "Welcome back", "Let's start"),
            ScriptLines.split(script),
        )
    }

    @Test
    fun `paragraphs are split into sentences`() {
        val script = "Hi there. Today we cook! Ready? Let's go… Now."

        assertEquals(
            listOf("Hi there.", "Today we cook!", "Ready?", "Let's go…", "Now."),
            ScriptLines.split(script),
        )
    }

    @Test
    fun `with sentence splitting off, lines stay exactly as typed`() {
        val script = "Hi there. Today we cook!\nSecond take."

        assertEquals(
            listOf("Hi there. Today we cook!", "Second take."),
            ScriptLines.split(script, splitSentences = false),
        )
    }

    @Test
    fun `closing quotes stay with their sentence`() {
        assertEquals(
            listOf("He said \"hello.\"", "Then he left."),
            ScriptLines.split("He said \"hello.\" Then he left."),
        )
    }

    @Test
    fun `arabic question mark ends a sentence`() {
        assertEquals(
            listOf("كيف حالك؟", "أنا بخير."),
            ScriptLines.split("كيف حالك؟ أنا بخير."),
        )
    }

    @Test
    fun `list numbers are not cut off from their item`() {
        assertEquals(
            listOf("1. Open the app.", "2. Pick a script."),
            ScriptLines.split("1. Open the app. 2. Pick a script."),
        )
    }

    @Test
    fun `text without punctuation is a single card`() {
        assertEquals(listOf("no punctuation here"), ScriptLines.split("no punctuation here"))
    }

    @Test
    fun `empty script has no cards`() {
        assertEquals(emptyList<String>(), ScriptLines.split("  \n \n"))
    }
}
