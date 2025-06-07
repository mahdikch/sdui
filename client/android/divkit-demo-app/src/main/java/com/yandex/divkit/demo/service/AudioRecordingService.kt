package com.yandex.divkit.demo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.IOException

class AudioRecordingService : Service() {
    companion object {
        const val CHANNEL_ID = "recording_channel"
        const val ACTION_STOP = "action_stop_recording"
    }

    private var recorder: MediaRecorder? = null
    private lateinit var outputFile: String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }

            else -> {
                startForgroundServiceWithNotification()
                startRecording()
                return START_STICKY
            }
        }
    }

    private fun startForgroundServiceWithNotification() {
        createNotificationChannel()
        val stopIntent = Intent(this, AudioRecordingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("در حال ضبط صدا")
            .setContentText("برای توقف ضبط روی دکمه کلیک کنید")
            .setSmallIcon(com.yandex.divkit.demo.R.drawable.baseline_mic_24)
            .addAction(
                com.yandex.divkit.demo.R.drawable.baseline_stop_24,
                "توقف",
                stopPendingIntent
            )
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startRecording() {
        val outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        outputFile =
            File(outputDir, "recorded_audio_${System.currentTimeMillis()}.3gp").absolutePath
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {

            } catch (e: IOException) {
                e.printStackTrace()
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder?.apply {
            try {
                stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            release()
        }
        recorder = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}