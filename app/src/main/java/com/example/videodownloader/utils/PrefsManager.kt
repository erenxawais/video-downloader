package com.example.videodownloader.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PrefsManager {

    private const val PREF = "video_dl_prefs"
    private const val KEY_HISTORY = "download_history"

    data class DownloadItem(
        val title: String,
        val path: String,
        val type: String,
        val size: Long,
        val timestamp: Long
    )

    fun addDownload(context: Context, item: DownloadItem) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY_HISTORY, "[]"))

        val obj = JSONObject().apply {
            put("title", item.title)
            put("path", item.path)
            put("type", item.type)
            put("size", item.size)
            put("timestamp", item.timestamp)
        }
        arr.put(obj)

        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
    }

    fun getHistory(context: Context): List<DownloadItem> {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY_HISTORY, "[]"))

        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            DownloadItem(
                title = o.optString("title"),
                path = o.optString("path"),
                type = o.optString("type"),
                size = o.optLong("size"),
                timestamp = o.optLong("timestamp")
            )
        }.reversed()
    }

    fun clearHistory(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().remove(KEY_HISTORY).apply()
    }
}
