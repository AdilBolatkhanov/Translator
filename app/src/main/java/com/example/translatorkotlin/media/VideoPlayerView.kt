package com.example.translatorkotlin.media

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.text.Spannable
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.core.view.isVisible
import com.example.translatorkotlin.R
import kotlinx.android.synthetic.main.view_video_player.view.*
import java.util.concurrent.TimeUnit

class VideoPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
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

        showOrHidePlaceholders(true)

        getAndSetAttributes(attrs, context, defStyleAttr)

        setupListeners()
    }

    private fun setupListeners() {
        playFAB.setOnClickListener {
            if (!started) {
                prepareAndStartVideoPlayback()

                started = true
                iconsVisible = false
            } else {
                handlePlayPauseState()
            }
        }

        videoView.setOnClickListener {
            if (!iconsVisible) {
                iconsVisible = true
                showOrHideIcons(iconsVisible)
            } else {
                iconsVisible = false
                showOrHideIcons(iconsVisible)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) =
                Unit

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                videoView.seekTo(seekBar?.progress ?: 0)
            }
        })
    }

    private fun prepareAndStartVideoPlayback() {
        playFAB.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_black_30dp))
        playFAB.visibility = GONE
        showOrHidePlaceholders(false)

        videoView.setVideoPath(url)
        videoView.start()
        setupVideoViewListeners()
    }

    private fun handlePlayPauseState() {
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

    private fun setupVideoViewListeners() {
        videoView.setOnPreparedListener {
            mediaPlayer = it
            seekBar.max = it.duration
            durationString = formatStringToTime("%02d:%02d", it.duration.toLong())
            runSeekbar()
        }

        videoView.setOnCompletionListener {
            videoView.stopPlayback()
            release()
            stopRun()
            playFAB.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp))
            showOrHidePlaceholders(true)
            playFAB.visibility = VISIBLE
            started = false
        }
    }

    private fun showOrHideIcons(show: Boolean) {
        playFAB.isVisible = show
        seekBar.isVisible = show
        titleTopTV.isVisible = show
        time.isVisible = show
    }

    private fun showOrHidePlaceholders(show: Boolean) {
        videoView.isVisible = !show
        seekBar.isVisible = !show
        titleTopTV.isVisible = !show
        time.isVisible = !show
        titleBottomTV.isVisible = show
        placeholderImage.isVisible = show
    }

    private fun getAndSetAttributes(
        attrs: AttributeSet?,
        context: Context,
        defStyleAttr: Int
    ) {
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
    }

    private fun runSeekbar() {
        runnable = Runnable {
            mediaPlayer?.let {
                time.text =
                    formatStringToTime("%02d:%02d - $durationString", it.currentPosition.toLong())
                seekBar.progress = it.currentPosition
            }
            myHandler.postDelayed(runnable!!, 100)
        }
        myHandler.postDelayed(runnable!!, 100)
    }

    private fun formatStringToTime(format: String, position: Long): String {
        return String.format(
            format,
            TimeUnit.MILLISECONDS.toMinutes(position),
            TimeUnit.MILLISECONDS.toSeconds(position) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position))
        )
    }

    private fun release() {
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    private fun stopRun() {
        runnable?.let {
            myHandler.removeCallbacks(it)
        }
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
