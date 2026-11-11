package com.lazybonez.app

import io.opentelemetry.context.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** HTTP access to the local demo backend. */
internal object MiniBackendClient {
    private val client by lazy {
        val baseClient = OkHttpClient.Builder()
            .connectTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
        OkHttpTelemetry.builder(LazyBonezApplication.rum.openTelemetry)
            .build()
            .createCallFactory(baseClient)
    }

    fun fetchEmojiCatalog(): List<EmojiEntry> {
        var lastFailure: IOException? = null
        for (path in EMOJI_PATHS) {
            try {
                return fetchEmojiCatalog(path)
            } catch (failure: IOException) {
                lastFailure = failure
            }
        }
        throw lastFailure ?: IOException("Emoji request failed")
    }

    suspend fun submitResults(entries: List<EmojiEntry>, plot: String, parentContext: Context) =
        withContext(Dispatchers.IO) {
            val emojis = JSONArray().apply {
                entries.forEach { entry ->
                    put(JSONObject().apply {
                        put("symbol", entry.symbol)
                        put("codePoints", entry.codePoints)
                        put("name", entry.name)
                    })
                }
            }
            val submissions = listOf(
                "/results/emoji" to JSONObject().put("emojis", emojis),
                "/results/plot" to JSONObject().put("plot", plot),
            )
            var failure: IOException? = null
            for ((path, payload) in submissions) {
                currentCoroutineContext().ensureActive()
                try {
                    // Keep the scope on this thread, without suspending, so both HTTP spans
                    // inherit the spin context rather than the completed stop span.
                    parentContext.makeCurrent().use {
                        val request = Request.Builder()
                            .url("$BASE_URL$path")
                            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                            .build()
                        client.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) {
                                throw IOException("Result POST to $path failed: HTTP ${response.code}")
                            }
                        }
                    }
                } catch (error: IOException) {
                    val previous = failure
                    if (previous == null) failure = error else previous.addSuppressed(error)
                }
            }
            if (failure != null) throw failure
        }

    private fun fetchEmojiCatalog(path: String): List<EmojiEntry> {
        val request = Request.Builder()
            .url("$BASE_URL$path")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Emoji request failed: HTTP ${response.code}")
            val body = response.body?.string() ?: throw IOException("Emoji response was empty")
            val array = JSONArray(body)
            val entries = buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val symbol = item.optString("symbol").takeIf(String::isNotBlank) ?: continue
                    val codePoints = item.optString("codePoints").takeIf(String::isNotBlank) ?: continue
                    val name = item.optString("name").takeIf(String::isNotBlank) ?: continue
                    add(EmojiEntry(symbol, codePoints, name))
                }
            }.distinctBy(EmojiEntry::codePoints)
            if (entries.isEmpty()) throw IOException("Emoji response contained no usable entries")
            return entries
        }
    }

    private const val BASE_URL = "http://10.0.2.2:8080"
    private const val REQUEST_TIMEOUT_SECONDS = 3L
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private val EMOJI_PATHS = listOf("/data/oops/emoji.json", "/emoji.json")
}
