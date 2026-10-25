package com.superprompter.domain

/** Cuts a script into the cards shown one at a time in line-by-line mode. */
object ScriptLines {

    // Split after sentence-ending punctuation (incl. the Arabic question mark), optionally
    // followed by a closing quote or bracket, e.g. `He said "hi." Then…`.
    private val SENTENCE_END = Regex("""(?<=[.!?…؟]["'”’»)]?)\s+""")

    // "1." or "2)" on its own: a list number, not a sentence. Glue it to what follows.
    private val LIST_NUMBER = Regex("""^\d+[.)]$""")

    /**
     * Every non-blank line of [content] becomes a card. With [splitSentences], lines holding
     * several sentences are split further, one sentence per card.
     */
    fun split(content: String, splitSentences: Boolean = true): List<String> =
        content.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .flatMap { line -> if (splitSentences) sentences(line) else listOf(line) }

    private fun sentences(line: String): List<String> {
        val parts = line.split(SENTENCE_END).map { it.trim() }.filter { it.isNotEmpty() }
        val merged = mutableListOf<String>()
        var pendingNumber: String? = null
        for (part in parts) {
            if (LIST_NUMBER.matches(part)) {
                pendingNumber = listOfNotNull(pendingNumber, part).joinToString(" ")
                continue
            }
            merged += listOfNotNull(pendingNumber, part).joinToString(" ")
            pendingNumber = null
        }
        pendingNumber?.let { merged += it }
        return merged
    }
}
