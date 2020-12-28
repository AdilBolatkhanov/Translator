package com.example.favorites.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.common.data.Favorites
import com.example.common.utility.ItemDecoration
import com.example.favorites.R
import com.example.favorites.ui.adapter.FavoriteAdapter
import com.example.favorites.ui.adapter.FavoriteClickListener
import com.example.favorites.viewmodel.FavoriteViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_favorite.*
import org.koin.androidx.viewmodel.ext.android.viewModel

const val THEMEPREF = "THEMEPREF"
const val DarkTHEME = "DarkTheme"

class FavoriteFragment : Fragment(R.layout.fragment_favorite), FavoriteClickListener {

    private val viewModel: FavoriteViewModel by viewModel()
    private var themePref: SharedPreferences? = null
    private var favoritesList = mutableListOf<Favorites>()
    private lateinit var favoriteAdapter: FavoriteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialSetup()

        recyclerSetup()

        observeData()
    }

    private fun observeData() {
        viewModel.response.observe(viewLifecycleOwner, Observer { list ->
            favoritesList = list as MutableList<Favorites>
            if (favoritesList.size == 0) {
                favoritesList = mutableListOf()
            }
            favoriteAdapter.setItems(favoritesList)
            if (favoritesList.size > 0 && !isLastVisible()) {
                recycler.smoothScrollToPosition(favoriteAdapter.itemCount - 1)
            }
        })
    }

    private fun recyclerSetup() {
        recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler.addItemDecoration(ItemDecoration(3, 3))
        favoriteAdapter = FavoriteAdapter(this)
        recycler.adapter = favoriteAdapter
        val itemTouch = ItemTouchHelper(favoriteAdapter.simpleCallback)
        itemTouch.attachToRecyclerView(recycler)
    }

    private fun initialSetup() {
        themePref = activity?.getSharedPreferences(THEMEPREF, Context.MODE_PRIVATE)
        (activity as AppCompatActivity).findViewById<TextView>(R.id.toolbarText).text = "Favorites"
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
                    sendMail()
                    true
                }
                R.id.bolisyQos -> true
                R.id.qosymsha -> true
                else -> false
            }
        }

        val menuPopupHelper = MenuPopupHelper(wrapper, popupMenu.menu as MenuBuilder, toolbar)
        menuPopupHelper.gravity = Gravity.END
        menuPopupHelper.setForceShowIcon(true)
        (activity as AppCompatActivity).findViewById<ImageView>(R.id.overflowToolbar)
            .setOnClickListener {
                menuPopupHelper.show()
            }
    }

    private fun sendMail() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("kwenten@mail.ru"))
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun isLastVisible(): Boolean {
        val layoutManager =
            recycler.layoutManager as LinearLayoutManager
        val pos = layoutManager.findLastCompletelyVisibleItemPosition()
        val numItems: Int = recycler.adapter?.itemCount ?: 0
        return pos >= numItems
    }

    override fun deleteClickListener(position: Int) {
        val text: CharSequence = "Deleted: " + favoritesList[position].first
        val snackbar = Snackbar.make(containerFav, text, Snackbar.LENGTH_LONG)
        val removedItem = favoritesList[position]
        snackbar.setAction("UNDO") {
            viewModel.insertAll(listOf(removedItem))
        }
        setupSnackbarUI(snackbar)

        viewModel.deleteFavorite(removedItem)
        favoriteAdapter.notifyItemRemoved(position)
        snackbar.show()
    }

    private fun setupSnackbarUI(snackbar: Snackbar) {
        if (themePref!!.getBoolean(DarkTHEME, false)) {
            snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
        } else snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.main))

        snackbar.apply {
            setBackgroundTint(
                ContextCompat.getColor(context, R.color.snack_bg)
            )
            setTextColor(ContextCompat.getColor(context, R.color.fav_text))
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
    }
}
