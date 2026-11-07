package com.lazybonez.app

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File

/** Runs the installed local model and keeps its engine warm for the current Plotify visit. */
internal class LocalPlotGenerator(private val modelFile: File) : Closeable {
    private var engine: Engine? = null

    suspend fun generate(emojiEntries: List<EmojiEntry>): String = withContext(Dispatchers.Default) {
        check(modelFile.isFile) { "Local model file not found: ${modelFile.absolutePath}" }

        val activeEngine = engine ?: Engine(
            EngineConfig(
                modelPath = modelFile.absolutePath,
                backend = Backend.CPU(),
            ),
        ).also { newEngine ->
            try {
                newEngine.initialize()
                engine = newEngine
            } catch (error: Throwable) {
                newEngine.close()
                throw error
            }
        }

        activeEngine.createConversation(
            ConversationConfig(
                systemInstruction = Contents.of(SYSTEM_INSTRUCTION),
                maxOutputToken = MAX_OUTPUT_TOKENS,
            ),
        ).use { conversation ->
            suspend fun ask(prompt: String): String = conversation.sendMessage(prompt)
                .contents.contents
                .filterIsInstance<Content.Text>()
                .joinToString(separator = "") { it.text }
                .trim()

            val firstAttempt = cleanPlot(ask(buildPrompt(emojiEntries)), emojiEntries)
            if (firstAttempt.isNotBlank()) {
                firstAttempt
            } else {
                val retry = cleanPlot(ask(buildRetryPrompt(emojiEntries)), emojiEntries)
                retry.ifBlank { throw IllegalStateException("The model did not produce a synopsis. Spin again to retry.") }
            }
        }
    }

    override fun close() {
        engine?.close()
        engine = null
    }

    private fun buildPrompt(entries: List<EmojiEntry>): String = buildString {
        appendLine("Write a two-sentence, past-tense synopsis of a complete low-budget horror movie, at most $MAX_WORDS words.")
        appendLine("Use these ideas in one coherent story: ${entries.joinToString("; ") { it.name }}.")
        append("Treat each idea as a theme or story element, not a visual description. No heading or preamble.")
    }

    private fun buildRetryPrompt(entries: List<EmojiEntry>): String = buildString {
        appendLine("Your last reply was only a heading. Write the actual two-sentence movie synopsis now, in past tense, under $MAX_WORDS words.")
        appendLine("Build one complete horror story from these ideas: ${entries.joinToString("; ") { it.name }}.")
        append("Use their meanings, not their pictured appearance. Do not mention anyone's skin tone. Output only the synopsis.")
    }

    private companion object {
        const val MAX_OUTPUT_TOKENS = 120
        const val MAX_WORDS = 50
        val SYSTEM_INSTRUCTION = """
            You write concise horror movie synopses. Use the three supplied English emoji names
            as story ideas, interpreting what they represent instead of describing their pictures.
            Summarize the complete story in past tense: protagonist, central conflict, and outcome.
            Write two complete sentences, no more than 50 words. Do not mention anyone's skin
            tone, physical appearance, or the emoji artwork. Output only the synopsis, with no
            title, heading, or preamble.
        """.trimIndent()

        val OPENING_LEAD = Regex(
            "(?is)^\\s*(?:#{1,6}\\s*)?(?:\\*{1,2})?" +
                "(?:here(?:['’]s| is)\\s+(?:my\\s+)?|this is\\s+)?" +
                "(?:the\\s+)?low[- ]budget\\s+horror(?:\\s+(?:movie|film))?" +
                "(?:\\s+(?:plot|concept|story|idea))?\\s*[:.!?…—–-]+\\s*",
        )
        val OUTPUT_LABEL = Regex(
            "(?is)^\\s*(?:(?:here(?:['’]s| is)\\s+(?:my\\s+)?)|(?:this is\\s+))?" +
                "(?:the\\s+)?(?:synopsis|plot|story|concept)(?:\\s+(?:for|of)\\s+(?:the\\s+)?(?:film|movie|story))?" +
                "\\s*[:.!?…—–-]+\\s*",
        )
        val LIST_ITEM_PREFIX = Regex("^(?:(?:[-*•])\\s+|(?:[1-3][.)])\\s+|(?:\\*{1,2})?[1-3]️⃣(?:\\*{1,2})?\\s*[-—:]?\\s*)")
        val NEXT_LIST_ITEM = Regex("(?m)\\n\\s*(?:(?:[-*•])\\s+|(?:[1-3][.)])\\s+|(?:\\*{1,2})?[1-3]️⃣(?:\\*{1,2})?\\s*[-—:]?\\s*)")
        val INCOMPLETE_SENTENCE_END = Regex(
            "(?i)\\b(?:a|an|the|and|but|or|to|of|in|on|at|for|with|from|by|as|because|that|which|who|" +
                "is|are|was|were|be|been|being|has|have|had|into|onto|through|beneath|until|while|when|if)$",
        )
        val SKIN_TONE_MENTION = Regex(
            "(?i)\\b(?:skin\\s*(?:tone|color)|complexion|" +
                "(?:dark|light|medium(?:[- ](?:light|dark))?|deep|fair|pale|brown|black|white|olive|tan)[- ](?:skinned|skin|complexion)|" +
                "skin\\s+(?:was|is|looked|appeared|became)\\s+(?:dark|light|medium|deep|fair|pale|brown|black|white|olive|tan))\\b",
        )
        val SKINNED_ADJECTIVE = Regex(
            "(?i)\\b(?:dark|light|medium(?:[- ](?:light|dark))?|deep|fair|pale|brown|black|white|olive|tan)-skinned\\b\\s*",
        )
    }

    private fun cleanPlot(text: String, entries: List<EmojiEntry>): String {
        var withoutLead = OPENING_LEAD.replaceFirst(text, "").trim()
        withoutLead = OUTPUT_LABEL.replaceFirst(withoutLead, "").trim()
        entries.forEach { entry ->
            val symbol = Regex.escape(entry.symbol)
            val name = Regex.escape(entry.name)
            val repeatedLabel = Regex(
                "(?i)^\\s*(?:$symbol\\s*(?:[-—:]\\s*)?(?:$name)?|$name)\\s*[-—:]\\s*",
            )
            withoutLead = repeatedLabel.replaceFirst(withoutLead, "")
        }
        val firstParagraph = withoutLead.split(Regex("\\n\\s*\\n"))
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
        val withoutListPrefix = LIST_ITEM_PREFIX.replaceFirst(firstParagraph.trim(), "")
        val oneStory = NEXT_LIST_ITEM.replaceFirst(withoutListPrefix, "")
        val cleaned = oneStory
            .replace("**", "")
            .replace("__", "")
            .replace(Regex("\\s+"), " ")
            .trim()
        val skinSafe = cleaned.split(Regex("(?<=[.!?])\\s+"))
            .filterNot(SKIN_TONE_MENTION::containsMatchIn)
            .joinToString(" ")
            .let { SKINNED_ADJECTIVE.replace(it, "") }
            .replace(Regex("\\s+"), " ")
            .trim()
        val completeSentences = skinSafe.split(Regex("(?<=[.!?])\\s+"))
            .map(String::trim)
            .filter { sentence ->
                sentence.lastOrNull()?.let { it in ".!?" } == true &&
                    !INCOMPLETE_SENTENCE_END.containsMatchIn(sentence.trimEnd('.', '!', '?', '"', '\'', '’', '”'))
            }
        val selected = mutableListOf<String>()
        var wordCount = 0
        for (sentence in completeSentences) {
            val sentenceWords = sentence.split(Regex("\\s+")).size
            if (selected.isNotEmpty() && wordCount + sentenceWords > MAX_WORDS) break
            selected += sentence
            wordCount += sentenceWords
            if (wordCount >= MAX_WORDS) break
        }
        if (selected.isNotEmpty()) return selected.joinToString(" ")

        // A small local model may return an awkwardly punctuated but still useful response.
        // Never let sentence cleanup erase the entire plot.
        val fallback = skinSafe
        return fallback.split(Regex("\\s+")).take(MAX_WORDS).joinToString(" ")
    }
}

internal const val LOCAL_MODEL_FILE_NAME = "gemma3-1b-it-int4.litertlm"
