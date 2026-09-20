package com.example.videodownloader

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.videodownloader.utils.PrefsManager
import kotlinx.coroutines.*
import java.io.File

class DownloadService : Service() {

    companion object {
        const val ACTION_PROGRESS = "com.example.videodownloader.PROGRESS"
        const val ACTION_DONE     = "com.example.videodownloader.DONE"
        const val ACTION_ERROR    = "com.example.videodownloader.ERROR"
        const val EXTRA_URL  = "url"
        const val EXTRA_TYPE = "type"
        private const val CHANNEL_ID = "download_channel"
        private const val NOTIF_ID = 1001
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
        val typeName = intent.getStringExtra(EXTRA_TYPE) ?: Downloader.Type.MP4_360.name
        val type = runCatching { Downloader.Type.valueOf(typeName) }
            .getOrDefault(Downloader.Type.MP4_360)

        createChannel()
        startForeground(NOTIF_ID, buildNotification("Starting…", 0))

        scope.launch {
            val result = Downloader.download(applicationContext, url, type) { percent, _ ->
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIF_ID, buildNotification("Downloading… ${percent.toInt()}%", percent.toInt()))
                sendBroadcast(Intent(ACTION_PROGRESS).apply {
                    setPackage(packageName)
                    putExtra("percent", percent)
                })
            }

            withContext(Dispatchers.Main) {
                if (result.success) {
                    PrefsManager.addDownload(
                        applicationContext,
                        PrefsManager.DownloadItem(
                            title = result.filePath?.let { File(it).name } ?: "video",
                            path  = result.filePath ?: "",
                            type  = type.name,
                            size  = result.fileSize,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    sendBroadcast(Intent(ACTION_DONE).apply { setPackage(packageName) })
                } else {
                    sendBroadcast(Intent(ACTION_ERROR).apply {
                        setPackage(packageName)
                        putExtra("error", result.error)
                    })
                }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_LOW)
                )
            }
        }
    }

    private fun buildNotification(text: String, progress: Int): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Video Downloader")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pi)
            .setOngoing(true)
            .setProgress(100, progress, progress == 0)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}