package com.example.translatorkotlin.adapters

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.translatorkotlin.R
import com.example.translatorkotlin.fragments.media.VideoFullFragment
import com.example.translatorkotlin.mediaViews.AudioPlayerView
import com.example.translatorkotlin.mediaViews.MediaFiles
import com.example.translatorkotlin.mediaViews.VideoPlayerView
import java.util.*

class MediaAdapter : RecyclerView.Adapter<MediaAdapter.ViewHolder>(), Filterable {
    private val currentMediaList = mutableListOf<MediaFiles>()
    private val mediaFilesListAll = mutableListOf<MediaFiles>()
    private lateinit var context: Context
    private lateinit var activity: FragmentActivity

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var videoPlayerView: VideoPlayerView? = itemView.findViewById(R.id.videoPlayerView)
        var audioPlayerView: AudioPlayerView? = itemView.findViewById(R.id.audioPlayerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))

    override fun getItemViewType(position: Int): Int {
        return if (currentMediaList[position].isVideo) {
            R.layout.video_player_container
        } else R.layout.audio_player_container
    }

    override fun getItemCount(): Int = currentMediaList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.videoPlayerView != null) {
            holder.videoPlayerView?.setOnClickListener {
                activity.supportFragmentManager.beginTransaction().add(
                    R.id.fragment_container,
                    VideoFullFragment.newInstance(
                        currentMediaList[position].uRL,
                        currentMediaList[position].text, currentMediaList[position].description
                    )
                ).addToBackStack("fullVideo").commit()
            }
            holder.videoPlayerView?.setVideoURL(currentMediaList[position].uRL)
            if (currentMediaList[position].spannable != null) {
                holder.videoPlayerView?.setSpannable(currentMediaList[position].spannable)
            } else {
                holder.videoPlayerView?.setTitle(currentMediaList[position].text)
            }
        } else {
            holder.audioPlayerView?.setAudioURL(currentMediaList[position].uRL)
            if (currentMediaList[position].spannable != null) {
                holder.audioPlayerView?.setSpannable(currentMediaList[position].spannable)
            } else {
                holder.audioPlayerView?.setTitle(currentMediaList[position].text)
            }
        }
    }

    fun setActivity(activity: AppCompatActivity) {
        this.activity = activity
    }

    fun setItems(media: List<MediaFiles>, context: Context?) {
        if (context != null) {
            this.context = context
        }
        mediaFilesListAll.clear()
        mediaFilesListAll.addAll(media)
        currentMediaList.clear()
        currentMediaList.addAll(media)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = myFilter

    private val myFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = mutableListOf<MediaFiles>()
            if (constraint.toString().isEmpty()) {
                for (media in mediaFilesListAll) {
                    media.spannable = null
                }
                filteredList.addAll(mediaFilesListAll)
            } else {
                findMediaByQuery(filteredList, constraint.toString())
            }
            val filteredResult = FilterResults()
            filteredResult.values = filteredList
            return filteredResult
        }

        private fun findMediaByQuery(resList: MutableList<MediaFiles>, query: String) {
            for (media in mediaFilesListAll) {
                if (media.text.toLowerCase(Locale.getDefault())
                        .contains(query.toLowerCase(Locale.getDefault()))
                ) {
                    val spannable = SpannableString(media.text)
                    val startIndex = media.text
                        .toLowerCase(Locale.getDefault())
                        .indexOf(query.toLowerCase(Locale.getDefault()))
                    spannable.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)),
                        startIndex,
                        startIndex + query.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    media.spannable = spannable
                    resList.add(media)
                }
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            currentMediaList.clear()
            if (results?.values != null && results.values as MutableList<MediaFiles> != null) {
                currentMediaList.addAll(results.values as MutableList<MediaFiles>)
            }
            notifyDataSetChanged()
        }
    }
}