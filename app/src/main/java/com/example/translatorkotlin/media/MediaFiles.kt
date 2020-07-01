package com.example.translatorkotlin.media

import android.text.Spannable

class MediaFiles {
    var isVideo: Boolean
    var text: String
    var uRL: String
    var description: String = ""
    var spannable: Spannable? = null

    constructor(text: String, urlStr: String, isVideo: Boolean) {
        this.isVideo = isVideo
        this.text = text
        uRL = urlStr
    }

    constructor(
        text: String,
        urlStr: String,
        isVideo: Boolean,
        description: String
    ) {
        this.isVideo = isVideo
        this.text = text
        uRL = urlStr
        this.description = description
    }
}
