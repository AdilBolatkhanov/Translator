package com.example.translatorkotlin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.translatorkotlin.R
import kotlinx.android.synthetic.main.video_full_fragment.*

private const val URL = "param1"
private const val TITLE = "param2"
private const val DESCRIPTION = "param3"

class VideoFullFragment : Fragment() {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.video_full_fragment, container, false)
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
        fun newInstance(param1: String, param2: String, param3: String) =
            VideoFullFragment().apply {
                arguments = Bundle().apply {
                    putString(URL, param1)
                    putString(TITLE, param2)
                    putString(DESCRIPTION, param3)
                }
            }
    }
}
