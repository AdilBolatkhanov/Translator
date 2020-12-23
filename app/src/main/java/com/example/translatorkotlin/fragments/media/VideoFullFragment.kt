package com.example.translatorkotlin.fragments.media

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.translatorkotlin.R
import kotlinx.android.synthetic.main.video_full_fragment.*

private const val URL = "URL"
private const val TITLE = "TITLE"
private const val DESCRIPTION = "DESCRIPTION"

class VideoFullFragment : Fragment(R.layout.video_full_fragment) {
    private var url: String? = null
    private var title: String? = null
    private var description: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(URL)
            title = it.getString(TITLE)
            description = it.getString(DESCRIPTION)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoTitle.text = title
        videoDescription.text = description
        url?.let {
            videoPlayerViewFull.setVideoURL(it)
            videoPlayerViewFull.play()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String, title: String, description: String) =
            VideoFullFragment().apply {
                arguments = Bundle().apply {
                    putString(URL, url)
                    putString(TITLE, title)
                    putString(DESCRIPTION, description)
                }
            }
    }
}
