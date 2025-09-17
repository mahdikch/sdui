package com.yandex.divkit.demo.div.audioRecorderView

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.yandex.divkit.demo.ui.LoadScreenListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val recordButton: ImageButton
    private val playButton: ImageButton
    private val deleteButton: ImageButton
    private val statusText: TextView
    private val durationText: TextView

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private var recordingStartTime: Long = 0
    private var recordingDuration: Long = 0
    private var loadScreenListener: LoadScreenListener? = null

    init {
        LayoutInflater.from(context).inflate(com.yandex.divkit.demo.R.layout.view_audio_recorder, this, true)
        
        recordButton = findViewById(com.yandex.divkit.demo.R.id.btn_record)
        playButton = findViewById(com.yandex.divkit.demo.R.id.btn_play)
        deleteButton = findViewById(com.yandex.divkit.demo.R.id.btn_delete)
        statusText = findViewById(com.yandex.divkit.demo.R.id.tv_status)
        durationText = findViewById(com.yandex.divkit.demo.R.id.tv_duration)

        setupButtons()
        updateUI()
    }

    private fun setupButtons() {
        recordButton.setOnClickListener {
            if (checkPermission()) {
                if (isRecording) {
                    stopRecording()
                } else {
                    startRecording()
                }
            }
        }

        playButton.setOnClickListener {
            if (isPlaying) {
                stopPlayback()
            } else {
                startPlayback()
            }
        }

        deleteButton.setOnClickListener {
            deleteRecording()
        }
    }

    private fun checkPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission through LoadScreenListener
            android.util.Log.d("AudioRecorderView", "Permission not granted, requesting...")
            android.util.Log.d("AudioRecorderView", "loadScreenListener is null: ${loadScreenListener == null}")
            loadScreenListener?.startRecording()
            Toast.makeText(context, "درخواست مجوز ضبط صدا...", Toast.LENGTH_LONG).show()
            false
        } else {
            android.util.Log.d("AudioRecorderView", "Permission already granted")
            true
        }
    }

    private fun startRecording() {
        try {
            // Create audio file
            audioFile = createAudioFile()
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            updateUI()
            startDurationTimer()

        } catch (e: IOException) {
            Toast.makeText(context, "خطا در شروع ضبط: ${e.message}", Toast.LENGTH_SHORT).show()
            resetRecording()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            recordingDuration = System.currentTimeMillis() - recordingStartTime
            updateUI()
            stopDurationTimer()

            Toast.makeText(context, "ضبط صدا با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در توقف ضبط: ${e.message}", Toast.LENGTH_SHORT).show()
            resetRecording()
        }
    }

    private fun startPlayback() {
        if (audioFile == null || !audioFile!!.exists()) {
            Toast.makeText(context, "فایل صوتی موجود نیست", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile!!.absolutePath)
                prepare()
                setOnCompletionListener {
                    stopPlayback()
                }
                start()
            }

            isPlaying = true
            updateUI()

        } catch (e: IOException) {
            Toast.makeText(context, "خطا در پخش صدا: ${e.message}", Toast.LENGTH_SHORT).show()
            resetPlayback()
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        isPlaying = false
        updateUI()
    }

    private fun deleteRecording() {
        audioFile?.let { file ->
            if (file.exists()) {
                file.delete()
            }
        }
        audioFile = null
        recordingDuration = 0
        resetPlayback()
        updateUI()
        Toast.makeText(context, "فایل صوتی حذف شد", Toast.LENGTH_SHORT).show()
    }

    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "AUDIO_$timeStamp.3gp"
        val storageDir = File(context.getExternalFilesDir(null), "AudioRecordings")
        
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        return File(storageDir, fileName)
    }

    private fun startDurationTimer() {
        handler.post(object : Runnable {
            override fun run() {
                if (isRecording) {
                    val currentDuration = System.currentTimeMillis() - recordingStartTime
                    durationText.text = formatDuration(currentDuration)
                    handler.postDelayed(this, 100)
                }
            }
        })
    }

    private fun stopDurationTimer() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun formatDuration(ms: Long): String {
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }

    private fun updateUI() {
        when {
            isRecording -> {
                recordButton.setImageResource(android.R.drawable.ic_media_pause)
                statusText.text = "در حال ضبط..."
                playButton.isEnabled = false
                deleteButton.isEnabled = false
            }
            audioFile != null && audioFile!!.exists() -> {
                recordButton.setImageResource(android.R.drawable.ic_btn_speak_now) // Microphone icon
                statusText.text = "آماده برای پخش"
                playButton.isEnabled = true
                deleteButton.isEnabled = true
                durationText.text = formatDuration(recordingDuration)
            }
            else -> {
                recordButton.setImageResource(android.R.drawable.ic_btn_speak_now) // Microphone icon
                statusText.text = "آماده برای ضبط"
                playButton.isEnabled = false
                deleteButton.isEnabled = false
                durationText.text = "00:00"
            }
        }

        playButton.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause 
            else android.R.drawable.ic_media_play
        )
    }

    private fun resetRecording() {
        isRecording = false
        recordingDuration = 0
        updateUI()
        stopDurationTimer()
    }

    private fun resetPlayback() {
        isPlaying = false
        updateUI()
    }

    fun getAudioFilePath(): String? {
        return audioFile?.absolutePath
    }

    fun hasRecording(): Boolean {
        return audioFile != null && audioFile!!.exists()
    }

    fun setLoadScreenListener(listener: LoadScreenListener?) {
        this.loadScreenListener = listener
    }

    fun onPermissionGranted() {
        // Called when permission is granted, can start recording if user was trying to record
        if (!isRecording && !isPlaying) {
            // User can now start recording
            updateUI()
        }
    }

    fun release() {
        stopRecording()
        stopPlayback()
        handler.removeCallbacksAndMessages(null)
    }
}
