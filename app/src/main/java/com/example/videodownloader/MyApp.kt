package com.example.videodownloader

import android.app.Application
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initYtDlp()
    }

    private fun initYtDlp() {
        Thread {
            try {
                YoutubeDL.getInstance().init(this)
                FFmpeg.getInstance().init(this)
                Log.d("MyApp", "yt-dlp + ffmpeg initialized ✅")
            } catch (e: Exception) {
                Log.e("MyApp", "Init failed ❌", e)
            }
        }.start()
    }
}
