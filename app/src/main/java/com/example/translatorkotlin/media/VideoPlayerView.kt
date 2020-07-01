package com.example.translatorkotlin.media

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.text.Spannable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.SeekBar
import com.example.translatorkotlin.R
import kotlinx.android.synthetic.main.view_video_player.view.*
import java.util.concurrent.TimeUnit

class VideoPlayerView
@JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr) {

    private var started = false
    private var paused = false
    private var iconsVisible = false
    private var url = ""
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var durationString: String
    private var runnable: Runnable? = null
    private var myHandler = Handler()

    init {
        inflate(context, R.layout.view_video_player, this)

        seekBar.visibility = View.GONE
        time.visibility = View.GONE
        titleTopTV.visibility = View.GONE
        videoView.visibility = View.GONE

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.VideoPlayerView,
                defStyleAttr,
                0
            )

            val name = typedArray.getString(R.styleable.VideoPlayerView_nameOfVideo)
            if (name != null && name.isNotEmpty()) {
                titleTopTV.text = name
                titleBottomTV.text = name
            }

            typedArray.recycle()
        }

        playFAB.setOnClickListener {
            if (!started) {
                playFAB.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_black_30dp))
                playFAB.visibility = View.GONE
                titleBottomTV.visibility = View.GONE
                placeholderImage.visibility = View.GONE
                videoView.visibility = View.VISIBLE
                videoView.setVideoPath(url)
                videoView.start()
                videoView.setOnPreparedListener {
                    mediaPlayer = it
                    seekBar.max = it.duration
                    durationString = String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(it.duration.toLong()),
                        TimeUnit.MILLISECONDS.toSeconds(it.duration.toLong()) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(it.duration.toLong())
                                )
                    )
                    runSeekbar()
                }

                videoView.setOnCompletionListener {
                    videoView.stopPlayback()
                    release()
                    stopRun()
                    videoView.visibility = View.GONE
                    playFAB.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                    seekBar.visibility = GONE
                    titleTopTV.visibility = View.GONE
                    time.visibility = GONE
                    titleBottomTV.visibility = View.VISIBLE
                    placeholderImage.visibility = View.VISIBLE
                    started = false
                }

                started = true
                iconsVisible = false
            } else {
                if (!paused) {
                    playFAB.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                    mediaPlayer?.pause()
                    paused = true
                } else {
                    playFAB.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_black_30dp))
                    mediaPlayer?.start()
                    paused = false
                }
            }
        }

        videoView.setOnClickListener {
            if (!iconsVisible) {
                playFAB.visibility = View.VISIBLE
                seekBar.visibility = View.VISIBLE
                titleTopTV.visibility = View.VISIBLE
                time.visibility = View.VISIBLE
                iconsVisible = true
            } else {
                playFAB.visibility = GONE
                seekBar.visibility = GONE
                titleTopTV.visibility = GONE
                time.visibility = GONE
                iconsVisible = false
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                videoView.seekTo(seekBar?.progress ?: 0)
            }
        })
    }

    private fun runSeekbar() {
        runnable = Runnable {
            val currentPos = mediaPlayer?.currentPosition
            if (currentPos != null) {
                time.text = String.format(
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

    private fun release() {
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    private fun stopRun() {
        myHandler.removeCallbacks(runnable)
    }

    fun setVideoURL(urlStr: String) {
        url = urlStr
    }

    fun play() {
        playFAB.performClick()
    }

    fun setTitle(title: String) {
        titleBottomTV.text = title
        titleTopTV.text = title
    }

    fun setSpannable(title: Spannable?) {
        titleBottomTV.text = title
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRun()
        release()
    }
}
