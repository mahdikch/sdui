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
    private var isRecording = false
    private var recordingId: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("AudioRecordingService", "=== onStartCommand called ===")
        android.util.Log.d("AudioRecordingService", "Intent action: ${intent?.action}")
        android.util.Log.d("AudioRecordingService", "Flags: $flags, StartId: $startId")
        
        when (intent?.action) {
            ACTION_STOP -> {
                android.util.Log.d("AudioRecordingService", "ACTION_STOP received, stopping recording")
                stopRecording()
                android.util.Log.d("AudioRecordingService", "Recording stopped, stopping service")
                stopSelf()
                android.util.Log.d("AudioRecordingService", "Service stopped")
                return START_NOT_STICKY
            }

            else -> {
                if (isRecording) {
                    android.util.Log.d("AudioRecordingService", "Already recording, ignoring duplicate start request")
                    return START_STICKY
                }
                // Extract recording ID from intent
                recordingId = intent?.getStringExtra("RECORDING_ID")
                android.util.Log.d("AudioRecordingService", "Recording ID from intent: $recordingId")
                android.util.Log.d("AudioRecordingService", "Starting recording service")
                startForgroundServiceWithNotification()
                startRecording()
                android.util.Log.d("AudioRecordingService", "Recording service started")
                return START_STICKY
            }
        }
    }

    private fun startForgroundServiceWithNotification() {
        android.util.Log.d("AudioRecordingService", "=== startForgroundServiceWithNotification() called ===")
        createNotificationChannel()
        android.util.Log.d("AudioRecordingService", "Notification channel created")
        
        val stopIntent = Intent(this, AudioRecordingService::class.java).apply {
            action = ACTION_STOP
        }
        android.util.Log.d("AudioRecordingService", "Created stop intent with action: $ACTION_STOP")
        
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        android.util.Log.d("AudioRecordingService", "Created stop pending intent")

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
        android.util.Log.d("AudioRecordingService", "Created notification")

        startForeground(1, notification)
        android.util.Log.d("AudioRecordingService", "Started foreground service with notification")
        android.util.Log.d("AudioRecordingService", "=== startForgroundServiceWithNotification() completed ===")
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
        android.util.Log.d("AudioRecordingService", "=== startRecording() called ===")
        val outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        
        // Create filename with ID if provided
        val fileName = if (recordingId != null) {
            "recorded_audio_${recordingId}_${System.currentTimeMillis()}.3gp"
        } else {
            "recorded_audio_${System.currentTimeMillis()}.3gp"
        }
        
        outputFile = File(outputDir, fileName).absolutePath
        android.util.Log.d("AudioRecordingService", "Recording ID: $recordingId")
        android.util.Log.d("AudioRecordingService", "Output file: $outputFile")
        
        recorder = MediaRecorder().apply {
            android.util.Log.d("AudioRecordingService", "Setting up MediaRecorder")
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                android.util.Log.d("AudioRecordingService", "Preparing MediaRecorder")
                prepare()
                android.util.Log.d("AudioRecordingService", "Starting MediaRecorder")
                start()
                isRecording = true
                android.util.Log.d("AudioRecordingService", "MediaRecorder started successfully")
            } catch (e: IOException) {
                android.util.Log.e("AudioRecordingService", "Error starting MediaRecorder: ${e.message}", e)
                e.printStackTrace()
                stopSelf()
            }
        }
        android.util.Log.d("AudioRecordingService", "=== startRecording() completed ===")
    }

    private fun stopRecording() {
        android.util.Log.d("AudioRecordingService", "=== stopRecording() called ===")
        if (!isRecording) {
            android.util.Log.d("AudioRecordingService", "Not currently recording, nothing to stop")
            return
        }
        
        recorder?.apply {
            try {
                android.util.Log.d("AudioRecordingService", "Stopping MediaRecorder")
                stop()
                android.util.Log.d("AudioRecordingService", "MediaRecorder stopped")
            } catch (e: Exception) {
                android.util.Log.e("AudioRecordingService", "Error stopping MediaRecorder: ${e.message}", e)
                e.printStackTrace()
            }
            android.util.Log.d("AudioRecordingService", "Releasing MediaRecorder")
            release()
        }
        recorder = null
        isRecording = false
        android.util.Log.d("AudioRecordingService", "=== stopRecording() completed ===")
    }

    override fun onDestroy() {
        android.util.Log.d("AudioRecordingService", "=== onDestroy() called ===")
        super.onDestroy()
        stopRecording()
        android.util.Log.d("AudioRecordingService", "=== onDestroy() completed ===")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}