package com.example.videodownloader

import android.content.Context
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.example.videodownloader.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

object Downloader {

    enum class Type { MP4_360, MP4_480, MP4_HD, MP3 }

    data class VideoInfo(
        val title: String,
        val description: String,
        val thumbnailUrl: String,
        val duration: Int = 0,
        val uploader: String = ""
    )

    data class Result(
        val success: Boolean,
        val filePath: String? = null,
        val fileSize: Long = 0L,
        val error: String? = null
    )

    suspend fun fetchInfo(url: String): VideoInfo? = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(url).apply {
                addOption("--dump-single-json")
                addOption("--no-playlist")
                addOption("--no-warnings")
                addOption("--skip-download")
            }
            val response = YoutubeDL.getInstance().execute(request)
            val json = JSONObject(response.out.trim().lines().last())
            VideoInfo(
                title = json.optString("title", "Untitled"),
                description = json.optString("description", ""),
                thumbnailUrl = json.optString("thumbnail", ""),
                duration = json.optInt("duration", 0),
                uploader = json.optString("uploader", "")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildRequest(url: String, type: Type, outDir: File): YoutubeDLRequest {
        return YoutubeDLRequest(url).apply {
            when (type) {
                Type.MP4_360 -> {
                    addOption("-f", "best[height<=360]/best")
                    addOption("--merge-output-format", "mp4")
                }
                Type.MP4_480 -> {
                    addOption("-f", "best[height<=480]/best")
                    addOption("--merge-output-format", "mp4")
                }
                Type.MP4_HD -> {
                    addOption("-f", "bestvideo[height<=2160]+bestaudio/best[height<=2160]/best")
                    addOption("--merge-output-format", "mp4")
                }
                Type.MP3 -> {
                    addOption("-x")
                    addOption("--audio-format", "mp3")
                    addOption("--audio-quality", "0")
                }
            }
            addOption("-o", FileUtils.outputTemplate(outDir))
            addOption("--no-playlist")
            addOption("--no-mtime")
            addOption("--no-warnings")
            addOption("--newline")
            addOption("--retries", "5")
            addOption("--fragment-retries", "5")
            addOption("--print", "after_move:filepath")
        }
    }

    suspend fun download(
        context: Context,
        url: String,
        type: Type,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result = withContext(Dispatchers.IO) {
        try {
            val outDir = FileUtils.getAppDownloadDir(context)
            val before = outDir.listFiles()?.toHashSet() ?: hashSetOf()

            val request = buildRequest(url, type, outDir)
            val response = YoutubeDL.getInstance().execute(
                request, null,
                object : YoutubeDL.Callback {
                    override fun onProgressUpdate(progress: Float, etaInSeconds: Long, line: String?) {
                        onProgress(progress, line ?: "")
                    }
                }
            )

            val printed = response.out.lines().map { it.trim() }
                .lastOrNull { it.isNotEmpty() && File(it).exists() }

            val outFile = if (printed != null) File(printed)
                else outDir.listFiles()?.toHashSet()?.minus(before)?.firstOrNull()

            if (outFile != null && outFile.exists()) {
                Result(true, outFile.absolutePath, outFile.length())
            } else {
                Result(false, error = "File not found after download")
            }
        } catch (e: Exception) {
            Result(false, error = e.message ?: "Unknown error")
        }
    }
}