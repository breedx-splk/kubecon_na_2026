package com.lazybonez.app

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One fully-qualified Unicode emoji and its Unicode English annotation. */
internal data class EmojiEntry(
    val symbol: String,
    val codePoints: String,
    val name: String,
)

/**
 * Loads the complete Unicode emoji-test catalog. Entries to omit for rendering or product reasons
 * belong in assets/emoji-exclusions.txt, one space-separated hexadecimal code point sequence per
 * line. The source catalog remains unmodified and names stay attached to their sequences.
 */
internal object EmojiCatalog {
    @Volatile
    private var cachedEntries: List<EmojiEntry>? = null

    suspend fun load(context: Context): List<EmojiEntry> {
        cachedEntries?.let { return it }
        return loadLocal(context).also { cachedEntries = it }
    }

    suspend fun loadDuringStartup(context: Context): List<EmojiEntry> = withContext(Dispatchers.IO) {
        cachedEntries?.let { return@withContext it }
        val entries = try {
            MiniBackendClient.fetchEmojiCatalog()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            loadLocal(context)
        }
        cachedEntries = entries
        entries
    }

    private suspend fun loadLocal(context: Context): List<EmojiEntry> = withContext(Dispatchers.IO) {
        val exclusions = context.applicationContext.assets.open(EXCLUSIONS_FILE)
            .bufferedReader()
            .useLines { lines ->
                lines.map(String::trim)
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .toSet()
            }

        context.applicationContext.assets.open(CATALOG_FILE)
            .bufferedReader()
            .useLines { lines ->
                lines.mapNotNull(::parseEntry)
                    .filterNot { it.codePoints in exclusions }
                    .distinctBy(EmojiEntry::codePoints)
                    .toList()
            }
    }

    private fun parseEntry(line: String): EmojiEntry? {
        if (line.isBlank() || line.startsWith("#")) return null
        val codePoints = line.substringBefore(';').trim()
        val status = line.substringAfter(';', "").substringBefore('#').trim()
        if (status != FULLY_QUALIFIED) return null

        val annotation = line.substringAfter('#', "").trim()
        val name = ANNOTATION_PATTERN.matchEntire(annotation)?.groupValues?.get(1) ?: return null
        val symbol = buildString {
            codePoints.split(WHITESPACE).forEach { appendCodePoint(it.toInt(16)) }
        }
        return EmojiEntry(symbol = symbol, codePoints = codePoints, name = name)
    }

    private const val CATALOG_FILE = "emoji-test.txt"
    private const val EXCLUSIONS_FILE = "emoji-exclusions.txt"
    private const val FULLY_QUALIFIED = "fully-qualified"
    private val WHITESPACE = Regex("\\s+")
    private val ANNOTATION_PATTERN = Regex("^\\S+\\s+E\\d+(?:\\.\\d+)?\\s+(.+)$")
}
