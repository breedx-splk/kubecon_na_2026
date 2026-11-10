package com.lazybonez.app

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit

/** HTTP access to the local demo backend. */
internal object MiniBackendClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    fun fetchEmojiCatalog(): List<EmojiEntry> {
        val request = Request.Builder()
            .url("$BASE_URL/emoji.json")
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
}
