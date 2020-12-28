package com.example.cyrillic.ui

import android.annotation.SuppressLint
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.common.data.ResponseClass
import com.example.common.utility.ItemDecoration
import com.example.common.utility.Utility
import com.example.cyrillic.R
import com.example.cyrillic.ui.adapter.MessageAdapter
import com.example.cyrillic.ui.adapter.ResponseClickListener
import com.example.cyrillic.ui.viewmodel.CyrillicViewModel
import com.example.cyrillic.util.CyrillicToLatinMapping
import kotlinx.android.synthetic.main.fragment_kirilliza.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

const val THEMEPREF = "THEMEPREF"
const val DarkTHEME = "DarkTheme"
const val USER_INPUT = "USER_INPUT"
const val AUTH = "auth"
const val EMAIL = "email"
const val USER_ID = "uid"
const val NAME = "name"
const val URL_ACC = "url"
const val TIME = "time"
const val QUANTITY = "quantity"

class KirillizaFragment : Fragment(R.layout.fragment_kirilliza), ResponseClickListener {
    private val kirToLat: HashMap<String, String> =
        CyrillicToLatinMapping.getCyrillicToLatinMapping()

    private val viewModel: CyrillicViewModel by viewModel()

    private var responseMessageList = mutableListOf<ResponseClass>()
    private var themePref: SharedPreferences? = null
    private lateinit var pref: SharedPreferences
    lateinit var layoutManager: LinearLayoutManager
    private lateinit var messageAdapter: MessageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialSetup()
        setPopupMenu()

        setupRecycler()

        initViewModel()

        scrollFAB.hide()
        setupListeners()
    }

    private fun setupListeners() {
        scrollFAB.setOnClickListener {
            scrollToBottom()
        }

        sendMessageImage.setOnClickListener {
            val userInputStr = userInput.text.toString()
            if (userInputStr.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseMessage = ResponseClass(
                        userInputStr,
                        true
                    )
                    val lat = translateToLat(userInputStr)
                    val responseMessage2 = ResponseClass(
                        lat,
                        false
                    )
                    viewModel.insertAll(listOf(responseMessage, responseMessage2))
                    incrementQuantityAndSave(userInputStr)
                    withContext(Dispatchers.Main) {
                        userInput.setText("")
                        userInput.setSelection(userInput.length())
                    }
                }
            }
        }
    }

    private fun incrementQuantityAndSave(userInputStr: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (preferences.getBoolean(AUTH, false)) {
            val splitted = userInputStr.split(" ")
            Utility.writeToFBDB(
                preferences.getString(USER_ID, "")!!,
                preferences.getString(NAME, "")!!,
                preferences.getString(EMAIL, "")!!,
                preferences.getString(URL_ACC, "")!!,
                "${(preferences.getInt(QUANTITY, 0) + splitted.size)}",
                preferences.getString(TIME, "")!!
            )
            preferences.edit()
                .putInt(QUANTITY, preferences.getInt(QUANTITY, 0) + splitted.size)
                .apply()
        }
    }

    private fun setupRecycler() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        conversation.layoutManager = layoutManager
        conversation.addItemDecoration(ItemDecoration(3, 3))
        messageAdapter = MessageAdapter(this)
        conversation.adapter = messageAdapter
    }

    private fun initViewModel() {
        viewModel.showError.observe(viewLifecycleOwner, Observer { showError ->
            Toast.makeText(context, showError, Toast.LENGTH_SHORT).show()
        })

        viewModel.response.observe(viewLifecycleOwner, Observer { responseList ->
            if (responseList == null || responseList.isEmpty()) {
                responseMessageList = mutableListOf()
            } else {
                responseMessageList.clear()
                responseMessageList.addAll(responseList)
                scrollFAB.show()
            }
            messageAdapter.setItemsWithDiff(responseMessageList)
            if (!isLastVisible() && responseMessageList.isNotEmpty())
                conversation.smoothScrollToPosition(messageAdapter.itemCount - 1)
        })
    }

    private fun initialSetup() {
        setBackground()
        (activity as AppCompatActivity).findViewById<TextView>(R.id.toolbarText).text =
            "Cyrillic - Latin"
        (activity as AppCompatActivity).findViewById<ImageView>(R.id.overflowToolbar)
            .setImageResource(R.drawable.ic_overflow)
        val textView = (activity as AppCompatActivity).findViewById<TextView>(R.id.exchangeTextView)
        (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar).removeView(textView)
    }

    private fun setBackground() {
        themePref = activity?.getSharedPreferences(THEMEPREF, Context.MODE_PRIVATE)
        if (themePref?.getBoolean(DarkTHEME, false) == true) {
            containerRel.background = resources.getDrawable(R.drawable.nightmode)
        } else {
            containerRel.background = resources.getDrawable(R.drawable.background)
        }
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

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val lastPosition = layoutManager.findLastVisibleItemPosition()
            if (lastPosition < responseMessageList.size - 1 && responseMessageList.size != 0) {
                scrollFAB.show()
            } else {
                scrollFAB.hide()
            }
        }
    }

    private fun scrollToBottom() {
        val smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int =
                SNAP_TO_START
        }
        smoothScroller.targetPosition = responseMessageList.size - 1
        layoutManager.startSmoothScroll(smoothScroller)
        scrollFAB.hide()
    }

    private fun isLastVisible(): Boolean {
        val pos = layoutManager.findLastCompletelyVisibleItemPosition()
        val numItems: Int = conversation.adapter?.itemCount ?: 0
        return pos >= numItems
    }

    private fun translateToLat(input: String): String {
        var s = input
        for (i in 0 until s.length) {
            val x = "${s[i]}"
            if (kirToLat.containsKey(x)) {
                s = s.replace(x, kirToLat[x]!!)
            }
        }
        return s
    }

    override fun copyToClipboard(positionOfItem: Int) {
        val clipboard =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Text", responseMessageList[positionOfItem].text)
        clipboard.setPrimaryClip(clipData)
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    override fun shareContent(positionOfItem: Int) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_TEXT,
            responseMessageList[positionOfItem].text
        )
        val chosenIntent = Intent.createChooser(intent, "Send message via...")
        activity?.startActivity(chosenIntent)
    }

    override fun deleteResponseItems(positionOfItem: Int) {
        if (responseMessageList[positionOfItem].isMe) {
            viewModel.deleteResponse(positionOfItem + 1, positionOfItem)
        } else {
            viewModel.deleteResponse(positionOfItem, positionOfItem - 1)
        }
    }

    override fun addToFavorites(positionOfItem: Int) {
        viewModel.insertAllFavorites(positionOfItem)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val userEnteredText = userInput.text.toString()
        pref = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        pref.edit().putString(USER_INPUT, userEnteredText).putBoolean("Reverse", true).apply()
    }

    override fun onResume() {
        super.onResume()
        setSavedStateValues()
        conversation.addOnScrollListener(scrollListener)
    }

    private fun setSavedStateValues() {
        pref = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        if (pref.getString(USER_INPUT, "")!!.isNotEmpty() &&
            pref.getBoolean("Reverse", false)
        ) {
            userInput.setText(pref.getString(USER_INPUT, ""))
            userInput.setSelection(userInput.text.toString().length - 1)
            pref.edit().putString(USER_INPUT, "").putBoolean("Reverse", false).apply()
        }
    }

    override fun onPause() {
        super.onPause()
        conversation.removeOnScrollListener(scrollListener)
    }
}
