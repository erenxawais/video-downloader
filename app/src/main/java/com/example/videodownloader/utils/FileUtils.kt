package com.example.videodownloader.utils

import android.content.Context
import android.os.Environment
import java.io.File

object FileUtils {

    fun getAppDownloadDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "Downloads")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getPublicDownloadDir(): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "VideoDownloader"
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun outputTemplate(dir: File): String {
        return "${dir.absolutePath}/%(title).80s.%(ext)s"
    }

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val i = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", bytes / Math.pow(1024.0, i.toDouble()), units[i])
    }
}
