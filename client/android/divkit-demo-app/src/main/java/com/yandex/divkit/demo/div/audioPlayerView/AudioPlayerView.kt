package com.yandex.divkit.demo.div.audioPlayerView


import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.IOException

class AudioPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val playPauseButton: ImageButton
    private val seekBar: SeekBar
    private val currentTimeText: TextView
    private val totalTimeText: TextView

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler()
    private var isPrepared = false

    init {
        LayoutInflater.from(context).inflate(com.yandex.divkit.demo.R.layout.view_audio_player, this, true)
        playPauseButton = findViewById(com.yandex.divkit.demo.R.id.btn_play_pause)
        seekBar = findViewById(com.yandex.divkit.demo.R.id.audio_seekbar)
        currentTimeText = findViewById(com.yandex.divkit.demo.R.id.tv_current_time)
        totalTimeText = findViewById(com.yandex.divkit.demo.R.id.tv_total_time)

        playPauseButton.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            } else {
                mediaPlayer?.start()
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                updateSeekBar()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isPrepared) {
                    mediaPlayer?.seekTo(progress)
                    updateTimeTexts()
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    fun setAudioUrl(url: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer?.apply {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener {
                    isPrepared = true
                    seekBar.max = duration
                    totalTimeText.text = formatTime(duration)
                }
                setOnCompletionListener {
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                    seekBar.progress = 0
                    currentTimeText.text = formatTime(0)
                }
            }
        } catch (e: IOException) {
            Toast.makeText(context, "خطا در بارگذاری صدا", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (mediaPlayer != null && isPrepared && mediaPlayer!!.isPlaying) {
                    seekBar.progress = mediaPlayer!!.currentPosition
                    updateTimeTexts()
                    handler.postDelayed(this, 500)
                }
            }
        }, 0)
    }

    private fun updateTimeTexts() {
        currentTimeText.text = formatTime(mediaPlayer?.currentPosition ?: 0)
    }

    private fun formatTime(ms: Int): String {
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }

    fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}
