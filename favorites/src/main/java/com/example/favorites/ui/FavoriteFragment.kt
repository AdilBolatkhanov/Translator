package com.example.favorites.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.favorites.R
import com.example.common.data.Favorites
import com.example.common.utility.ItemDecoration
import com.example.favorites.viewmodel.FavoriteViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_favorite.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Collections
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val THEMEPREF = "THEMEPREF"
const val DarkTHEME = "DarkTheme"

class FavoriteFragment : Fragment() {

    private val viewModel: FavoriteViewModel by viewModel()
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainThreadExecutor =
        MainThreadExecutor()
    private var themePref: SharedPreferences? = null
    private var favoritesList = mutableListOf<Favorites>()
    private lateinit var favoriteAdapter: FavoriteAdapter

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialSetup()

        recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler.addItemDecoration(ItemDecoration(3, 3))
        favoriteAdapter =
            FavoriteAdapter()
        favoriteAdapter.setActivity(activity as AppCompatActivity)
        recycler.adapter = favoriteAdapter
        val itemTouch = ItemTouchHelper(favoriteAdapter.simpleCallback)
        itemTouch.attachToRecyclerView(recycler)

        viewModel.response.observe(viewLifecycleOwner, Observer { list ->
            favoritesList = list as MutableList<Favorites>
            Log.d("ROOMDBFAV", favoritesList.size.toString() + "")
            if (favoritesList == null || favoritesList.size == 0) {
                favoritesList = mutableListOf()
            }
            favoriteAdapter.setItems(favoritesList)
            if (favoritesList.size > 0 && !isLastVisible()) {
                recycler.smoothScrollToPosition(favoriteAdapter.itemCount - 1)
            }
        })
        viewModel.getAllResponses()

    }

    private fun initialSetup() {
        themePref = activity?.getSharedPreferences(THEMEPREF, Context.MODE_PRIVATE)
        (activity as AppCompatActivity).findViewById<TextView>(R.id.toolbarText).text = "Unaǵandar tіzіmі"
        (activity as AppCompatActivity).findViewById<ImageView>(R.id.overflowToolbar)
            .setImageResource(R.drawable.ic_overflow)
        val textView = (activity as AppCompatActivity).findViewById<TextView>(R.id.exchangeTextView)
        (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar).removeView(textView)
        setPopupMenu()
    }

    @SuppressLint("RestrictedApi")
    fun setPopupMenu() {
        val toolbar = (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)
        val wrapper = ContextThemeWrapper(context, R.style.BasePopupMenu)
        val popupMenu = PopupMenu(wrapper, toolbar)
        popupMenu.inflate(R.menu.overflow_menu)

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.hat -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.data = Uri.parse("mailto:")
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("kwenten@mail.ru"))
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(intent)
                    }
                    true
                }
                R.id.bolisyQos -> true
                R.id.qosymsha -> true
                else -> false
            }
        }

        val menuPopupHelper = MenuPopupHelper(wrapper, popupMenu.menu as MenuBuilder, toolbar)
        menuPopupHelper.gravity = Gravity.RIGHT
        menuPopupHelper.setForceShowIcon(true)
        (activity as AppCompatActivity).findViewById<ImageView>(R.id.overflowToolbar)
            .setOnClickListener {
                menuPopupHelper.show()
            }
    }

    class MainThreadExecutor : Executor {
        private val myHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            myHandler.post(command)
        }
    }

    private fun isLastVisible(): Boolean {
        val layoutManager =
            recycler.layoutManager as LinearLayoutManager
        val pos = layoutManager.findLastCompletelyVisibleItemPosition()
        val numItems: Int = recycler.adapter?.itemCount ?: 0
        return pos >= numItems
    }

    class FavoriteAdapter : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {
        private var favorites = mutableListOf<Favorites>()
        private lateinit var activity: FragmentActivity
        private var removedPosition = 0
        lateinit var removedItem: Favorites

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var first: TextView = v.findViewById(R.id.first)
            var second: TextView = v.findViewById(R.id.second)
            var del: ImageView = v.findViewById(R.id.del_icon)
        }

        fun setActivity(activity: AppCompatActivity) {
            this.activity = activity
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.favorite_item, parent, false
                )
            )
        }

        override fun getItemCount(): Int = favorites.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val fragment: Fragment =
                activity.supportFragmentManager.findFragmentByTag("fragmentTag")!!
            holder.first.text = favorites[position].first
            holder.second.text = favorites[position].second
            holder.del.setOnClickListener {
                val text: CharSequence = "Oshirildi: " + holder.first.text
                val snackbar =
                    Snackbar.make(
                        (fragment as FavoriteFragment).getContainer(),
                        text,
                        Snackbar.LENGTH_LONG
                    )
                snackbar.setAction("Keri qaitaru") {
                    favorites.add(removedPosition, removedItem)
                    fragment.viewModel.insertAll(listOf(removedItem))
                    fragment.setMyFavorites(favorites)
                    notifyItemInserted(removedPosition)
                }
                if (fragment.themePref!!.getBoolean(DarkTHEME, false)) {
                    snackbar.setActionTextColor(fragment.resources.getColor(R.color.orange))
                } else snackbar.setActionTextColor(fragment.resources.getColor(R.color.main))

                snackbar.apply {
                    setBackgroundTint(
                        fragment.resources.getColor(R.color.snack_bg)
                    )
                    setTextColor(fragment.resources.getColor(R.color.fav_text))
                    animationMode = Snackbar.ANIMATION_MODE_SLIDE
                }
                val snackbarActionTextView = snackbar.view
                    .findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
                snackbarActionTextView.textSize = 14f
                val params = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
                params.setMargins(
                    params.leftMargin + 15,
                    params.topMargin + 15,
                    params.rightMargin + 15,
                    params.bottomMargin + +15
                )
                snackbar.view.layoutParams = params

                val snackbarTextView = snackbar.view
                    .findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                snackbarTextView.textSize = 16f
                removedPosition = holder.adapterPosition
                removedItem = favorites[removedPosition]
                favorites.removeAt(removedPosition)

                fragment.viewModel.deleteFavorite(removedItem)
                fragment.setMyFavorites(favorites)
                notifyItemRemoved(removedPosition)
                snackbar.show()
            }
        }

        var simpleCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPosition = viewHolder.adapterPosition
                    val toPosition = target.adapterPosition
                    if (fromPosition < toPosition) {
                        for (i in fromPosition until toPosition) {
                            Collections.swap(favorites, i, i + 1)
                            notifyItemMoved(i, i + 1)
                        }
                    } else {
                        for (i in fromPosition downTo toPosition + 1) {
                            Collections.swap(favorites, i, i - 1)
                            notifyItemMoved(i, i - 1)
                        }
                    }
                    return false
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                }
            }

        fun setItems(list: List<Favorites>) {
            favorites.clear()
            favorites.addAll(list)
            notifyDataSetChanged()
        }
    }

    fun getContainer(): CoordinatorLayout = containerFav

    fun setMyFavorites(list: List<Favorites>) {
        favoritesList.clear()
        favoritesList.addAll(list)
    }

    override fun onDestroy() {
        executorService.shutdown()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        viewModel.insertAll(favoritesList)
    }
}
