package com.example.translatorkotlin.mediaViews

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.text.Spannable
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import com.example.translatorkotlin.R
import kotlinx.android.synthetic.main.view_audio_player.view.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class AudioPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager =
        getContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private lateinit var durationString: String
    private var runnable: Runnable? = null
    private var myHandler = Handler()
    private var started = false
    private var paused = false
    private var url = ""

    private val mAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {
        if (it == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
            it == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
        ) {
            mediaPlayer?.pause()
            mediaPlayer?.seekTo(0)
        } else if (it == AudioManager.AUDIOFOCUS_GAIN) {
            mediaPlayer?.start()
        } else if (it == AudioManager.AUDIOFOCUS_LOSS) {
            stop()
        }
    }

    init {
        inflate(context, R.layout.view_audio_player, this)
        setupListeners()
    }

    private fun setupListeners() {
        playFABAudio.setOnClickListener {
            if (!started) {
                prepareAndPlayAudio()
            } else {
                handlePlayPauseStates()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer?.seekTo(seekBar?.progress ?: 0)
            }
        })
    }

    private fun handlePlayPauseStates() {
        if (!paused) {
            playFABAudio.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp))
            mediaPlayer?.pause()
            paused = true
        } else {
            playFABAudio.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_black_30dp))
            mediaPlayer?.start()
            paused = false
        }
    }

    private fun prepareAndPlayAudio() {
        val requestResult = audioManager.requestAudioFocus(
            mAudioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
        if (requestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            playFABAudio.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_black_30dp))
            mediaPlayer = MediaPlayer()
            try {
                setupMediaPlayer()
                setupMediaListeners()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            started = true
        }
    }

    private fun setupMediaListeners() {
        mediaPlayer?.setOnPreparedListener {
            mediaPlayer?.start()
            seekBar.max = it.duration
            durationString = formatStringToTime("%02d:%02d", it.duration.toLong())
            runSeekbar()
        }

        mediaPlayer?.setOnCompletionListener {
            it.stop()
            stopRun()
            playFABAudio.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp))
            stop()
            started = false
        }
    }

    private fun formatStringToTime(format: String, position: Long): String {
        return String.format(
            format,
            TimeUnit.MILLISECONDS.toMinutes(position),
            TimeUnit.MILLISECONDS.toSeconds(position) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position))
        )
    }

    private fun setupMediaPlayer() {
        mediaPlayer?.apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setDataSource(url)
            prepareAsync()
            Toast.makeText(context, "Loading, wait...", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun runSeekbar() {
        runnable = Runnable {
            mediaPlayer?.let {
                timeAudio.text =
                    formatStringToTime("%02d:%02d - $durationString", it.currentPosition.toLong())
                seekBar.progress = it.currentPosition
            }
            myHandler.postDelayed(runnable!!, 100)
        }
        myHandler.postDelayed(runnable!!, 100)
    }

    private fun stopRun() {
        runnable?.let {
            myHandler.removeCallbacks(it)
        }
    }

    fun setAudioURL(urlStr: String) {
        url = urlStr
    }

    fun setSpannable(spannable: Spannable?) {
        titleAudio.text = spannable
    }

    fun setTitle(title: String) {
        titleAudio.text = title
    }

    private fun stop() {
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
            audioManager.abandonAudioFocus(mAudioFocusChangeListener)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRun()
        stop()
    }
}
