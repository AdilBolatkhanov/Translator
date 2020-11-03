package com.example.translatorkotlin.fragments

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.translatorkotlin.R
import com.example.translatorkotlin.media.AudioPlayerView
import com.example.translatorkotlin.media.MediaFiles
import com.example.translatorkotlin.media.VideoPlayerView
import kotlinx.android.synthetic.main.fragment_search.*
import com.example.translatorkotlin.MainActivity
import java.util.Locale

class SearchFragment : Fragment() {

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? { return inflater.inflate(R.layout.fragment_search, container, false) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialSetup()

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
        val adapter = MediaAdapter()
        adapter.setActivity(activity as AppCompatActivity)
        adapter.setItems(mediaFiles, context)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        videoAndAudioRecycler.adapter = adapter
        videoAndAudioRecycler.layoutManager = layoutManager

        searchView.addTextChangedListener {
            adapter.filter.filter(it.toString())
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

    class MediaAdapter : RecyclerView.Adapter<MediaAdapter.ViewHolder>(), Filterable {
        private val mediaFilesList = mutableListOf<MediaFiles>()
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
            return if (mediaFilesList[position].isVideo) {
                R.layout.video_player_container
            } else R.layout.audio_player_container
        }

        override fun getItemCount(): Int = mediaFilesList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.videoPlayerView != null) {
                holder.videoPlayerView?.setOnClickListener {
                    activity.supportFragmentManager.beginTransaction().add(
                        R.id.fragment_container,
                        VideoFullFragment.newInstance(
                            mediaFilesList[position].uRL,
                            mediaFilesList[position].text, mediaFilesList[position].description
                        )
                    )
                        .addToBackStack("fullVideo").commit()
                }
                holder.videoPlayerView?.setVideoURL(mediaFilesList[position].uRL)
                if (mediaFilesList[position].spannable != null) {
                    holder.videoPlayerView?.setSpannable(mediaFilesList[position].spannable)
                } else {
                    holder.videoPlayerView?.setTitle(mediaFilesList[position].text)
                }
            } else {
                holder.audioPlayerView?.setVideoURL(mediaFilesList[position].uRL)
                if (mediaFilesList[position].spannable != null) {
                    holder.audioPlayerView?.setSpannable(mediaFilesList[position].spannable)
                } else {
                    holder.audioPlayerView?.setTitle(mediaFilesList[position].text)
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
            mediaFilesList.clear()
            mediaFilesList.addAll(media)
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
                    for (media in mediaFilesListAll) {
                        if (media.text.toLowerCase(Locale.getDefault())
                                .contains(constraint.toString().toLowerCase(Locale.getDefault()))
                        ) {
                            val spannable = SpannableString(media.text)
                            val startIndex = media.text.indexOf(constraint.toString())
                            spannable.setSpan(
                                ForegroundColorSpan(getColor(context, R.color.colorAccent)),
                                startIndex,
                                startIndex + constraint.toString().length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            media.spannable = spannable
                            filteredList.add(media)
                        }
                    }
                }
                val filteredResult = FilterResults()
                filteredResult.values = filteredList
                return filteredResult
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mediaFilesList.clear()
                if (results?.values != null && results?.values as MutableList<MediaFiles> != null) {
                    mediaFilesList.addAll(results?.values as MutableList<MediaFiles>)
                }
                notifyDataSetChanged()
            }
        }
    }
}
