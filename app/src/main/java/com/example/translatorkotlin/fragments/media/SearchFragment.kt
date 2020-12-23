package com.example.translatorkotlin.fragments.media

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.translatorkotlin.MainActivity
import com.example.translatorkotlin.R
import com.example.translatorkotlin.adapters.MediaAdapter
import com.example.translatorkotlin.mediaViews.MediaFiles
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var mediaAdapter: MediaAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialSetup()
        setupMediaRecycler()
        setupListeners()
    }

    private fun setupMediaRecycler() {
        val mediaFiles = listOf(
            MediaFiles(
                "Komedia Akim 2020",
                "http://videocdn.bodybuilding.com/video/mp4/62000/62792m.mp4",
                true,
                "Молодого мажора по имени Азамат, привыкшего к комфортной городской жизни, " +
                        "по стечению непредвиденных обстоятельств, отправляют работать Акимом в отдаленный посёлок -" +
                        " Таза Булак. ... С первого дня пребывания в ауле, новоиспечённый Аким мечтает о том, чтобы вернуться в город."
            ),
            MediaFiles(
                "Imagine dragons - Dream 2018",
                "https://drivemusic.club/dl/Y5mByP9yLveZ1HfIRe2cBQ/1590689044/download_music/2013/12/imagine-dragons-demons.mp3",
                false
            ),
            MediaFiles(
                "Fast and furious 2022",
                "http://videocdn.bodybuilding.com/video/mp4/62000/62792m.mp4",
                true,
                "Молодого мажора по имени Азамат, привыкшего к комфортной городской жизни, " +
                        "по стечению непредвиденных обстоятельств, отправляют работать Акимом в отдаленный посёлок -" +
                        " Таза Булак. ... С первого дня пребывания в ауле, новоиспечённый Аким мечтает о том, чтобы вернуться в город."
            )
        )
        mediaAdapter = MediaAdapter()
        mediaAdapter.setActivity(activity as AppCompatActivity)
        mediaAdapter.setItems(mediaFiles, context)
        videoAndAudioRecycler.adapter = mediaAdapter
    }

    private fun setupListeners() {
        searchView.addTextChangedListener {
            mediaAdapter.filter.filter(it.toString())
        }
    }

    private fun initialSetup(){
        (activity as AppCompatActivity).findViewById<TextView>(R.id.toolbarText).text = "Search"
        (activity as AppCompatActivity).findViewById<ImageView>(R.id.overflowToolbar)
            .setImageResource(R.drawable.ic_overflow)
        val textView = (activity as AppCompatActivity).findViewById<TextView>(R.id.exchangeTextView)
        (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar).removeView(textView)
        (activity as MainActivity).setPopupMenu()
    }
}
