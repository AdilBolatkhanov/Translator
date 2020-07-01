package com.example.translatorkotlin.media

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

class AudioPlayerView
@JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr) {

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
        playFABAudio.setOnClickListener {
            if (!started) {
                val result = audioManager.requestAudioFocus(
                    mAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    playFABAudio.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_black_30dp))
                    mediaPlayer = MediaPlayer()
                    try {
                        mediaPlayer?.apply {
                            setAudioStreamType(AudioManager.STREAM_MUSIC)
                            setDataSource(url)
                            prepareAsync()
                            Toast.makeText(getContext(), "Loading, wait...", Toast.LENGTH_LONG)
                                .show()
                        }
                        mediaPlayer?.setOnPreparedListener {
                            mediaPlayer?.start()
                            Toast.makeText(getContext(), "Playing", Toast.LENGTH_LONG).show()
                            seekBar.max = it.duration
                            durationString = String.format(
                                "%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(it.duration.toLong()),
                                TimeUnit.MILLISECONDS.toSeconds(it.duration.toLong()) -
                                        TimeUnit.MINUTES.toSeconds(
                                            TimeUnit.MILLISECONDS.toMinutes(
                                                it.duration.toLong()
                                            )
                                        )
                            )
                            runSeekbar()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    mediaPlayer?.setOnCompletionListener {
                        it.stop()
                        stopRun()
                        playFABAudio.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                        stop()
                        started = false
                    }
                    started = true
                }
            } else {
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
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer?.seekTo(seekBar?.progress ?: 0)
            }
        })
    }

    private fun runSeekbar() {
        runnable = Runnable {
            val currentPos = mediaPlayer?.currentPosition
            if (currentPos != null) {
                timeAudio.text = String.format(
                    "%02d:%02d - $durationString",
                    TimeUnit.MILLISECONDS.toMinutes(currentPos.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(currentPos.toLong()) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    currentPos.toLong()
                                )
                            )
                )
            }
            if (currentPos != null) {
                seekBar.progress = currentPos
            }
            myHandler.postDelayed(runnable, 100)
        }
        myHandler.postDelayed(runnable, 100)
    }

    private fun stopRun() {
        myHandler.removeCallbacks(runnable)
    }

    fun setVideoURL(urlStr: String) {
        url = urlStr
    }

    fun setSpannable(spannable: Spannable?) {
        titleAudio.text = spannable
    }

    fun setTitle(title: String) {
        titleAudio.text = title
    }

    fun stop() {
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
