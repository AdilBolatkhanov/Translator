package com.example.cyrillic.ui

import android.annotation.SuppressLint
import android.content.*
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.common.data.Favorites
import com.example.common.data.ResponseClass
import com.example.common.utility.ItemDecoration
import com.example.common.utility.MyDiffUtil
import com.example.common.utility.Utility
import com.example.cyrillic.R
import com.example.cyrillic.util.CyrillicToLatinMapping
import com.example.cyrillic.viewmodel.CyrillicViewModel
import kotlinx.android.synthetic.main.fragment_kirilliza.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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

class KirillizaFragment : Fragment(R.layout.fragment_kirilliza) {
    private val kirToLat: HashMap<String, String> =
        CyrillicToLatinMapping.getCyrillicToLatinMapping()

    private val viewModel: CyrillicViewModel by viewModel()

    private var messageId = 1
    private var responseMessageList = mutableListOf<ResponseClass>()
    private var themePref: SharedPreferences? = null
    private lateinit var pref: SharedPreferences
    private var executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private var mainThreadExecutor = MainThreadExecutor()
    lateinit var layoutManager: LinearLayoutManager
    private lateinit var messageAdapter: MessageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialSetup()
        setPopupMenu()

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        conversation.layoutManager = layoutManager
        conversation.addItemDecoration(ItemDecoration(3, 3))

        scrollFAB.hide()
        initViewModel()

        scrollFAB.setOnClickListener {
            scrollToBottom()
        }

        sendMessageImage.setOnClickListener {
            val userInputStr = userInput.text.toString()
            if (userInputStr.isNotEmpty()) {
                executorService.execute {
                    val responseMessage = ResponseClass(
                        messageId++,
                        userInputStr,
                        true
                    )
                    responseMessageList.add(responseMessage)
                    val lat = translateToLat(userInputStr)
                    val responseMessage2 = ResponseClass(
                        messageId++,
                        lat,
                        false
                    )
                    if (preferences.getBoolean(AUTH, false)) {
                        val splitted = userInputStr.split(" ")
                        Utility.writeToFBDB(
                            preferences!!.getString(USER_ID, "")!!,
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
                    responseMessageList.add(responseMessage2)
                    mainThreadExecutor.execute {
                        messageAdapter.setItemsWithDiff(responseMessageList)
                        userInput.setText("")
                        if (!isLastVisible())
                            conversation.smoothScrollToPosition(messageAdapter.itemCount - 1)
                        userInput.setSelection(userInput.length())
                    }
                }
            }
        }
    }

    private fun initViewModel() {
        viewModel.showError.observe(viewLifecycleOwner, Observer { showError ->
            Toast.makeText(context, showError, Toast.LENGTH_SHORT).show()
        })

        viewModel.response.observe(viewLifecycleOwner, Observer { responseList ->
            Log.d("KirillizaFragment", "${responseList.size}")
            if (responseList == null || responseList.isEmpty()) {
                responseMessageList = mutableListOf()
                messageId = 1
            } else {
                responseMessageList.clear()
                responseMessageList.addAll(responseList)
                scrollFAB.show()
                messageId = responseMessageList[responseMessageList.size - 1].responseId + 1
            }
            Log.d("KirillizaFragment", "${responseMessageList.size}")
            messageAdapter =
                MessageAdapter()
            messageAdapter.setActivity(activity as AppCompatActivity, context)
            messageAdapter.setItemsWithDiff(responseMessageList)
            conversation.adapter = messageAdapter
        })
        viewModel.getAllResponses()
    }

    private fun initialSetup() {
        themePref = activity?.getSharedPreferences(THEMEPREF, Context.MODE_PRIVATE)
        if (themePref?.getBoolean(DarkTHEME, false) == true) {
            containerRel.background = resources.getDrawable(R.drawable.nightmode)
        } else {
            containerRel.background = resources.getDrawable(R.drawable.background)
        }
        (activity as AppCompatActivity).findViewById<TextView>(R.id.toolbarText).text =
            "Cyrillic - Latin"
        (activity as AppCompatActivity).findViewById<ImageView>(R.id.overflowToolbar)
            .setImageResource(R.drawable.ic_overflow)
        val textView = (activity as AppCompatActivity).findViewById<TextView>(R.id.exchangeTextView)
        (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar).removeView(textView)
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
        menuPopupHelper.gravity = Gravity.END
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
            val x = s[i].toString() + ""
            if (kirToLat.containsKey(x)) {
                s = s.replace(x, kirToLat[x]!!)
            }
        }
        return s
    }

    class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
        private val responseMessages = mutableListOf<ResponseClass>()
        private lateinit var context: Context
        private lateinit var activity: FragmentActivity
        private val myDiffUtil = MyDiffUtil()
        private var positionOfItem = 0

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var textView: TextView = itemView.findViewById(R.id.textMessage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            )
        }

        fun setActivity(activity: AppCompatActivity, context: Context?) {
            if (context != null) {
                this.context = context
            }
            this.activity = activity
        }

        override fun getItemCount(): Int = responseMessages.size

        @SuppressLint("RestrictedApi")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = responseMessages[position].text
            val wrapper = ContextThemeWrapper(context, R.style.BasePopupMenu)
            val popupMenu = PopupMenu(wrapper, holder.textView)
            popupMenu.inflate(R.menu.item_menu)

            val menuPopupHelper =
                MenuPopupHelper(wrapper, popupMenu.menu as MenuBuilder, holder.textView)
            menuPopupHelper.setForceShowIcon(true)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.koshiry -> {
                        val clipboard =
                            activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData =
                            ClipData.newPlainText("Text", responseMessages[position].text)
                        clipboard.setPrimaryClip(clipData)
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.bolisy -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "text/plain"
                        intent.putExtra(
                            Intent.EXTRA_TEXT,
                            responseMessages[positionOfItem].text
                        )
                        val chosenIntent =
                            Intent.createChooser(intent, "Send message via...")
                        activity.startActivity(chosenIntent)
                        true
                    }
                    R.id.oshiry -> {
                        val fragment: Fragment =
                            activity.supportFragmentManager.findFragmentByTag("fragmentTag")!!
                        if (responseMessages[positionOfItem].isMe) {
                            (fragment as KirillizaFragment).deleteFromDb(
                                positionOfItem + 1,
                                positionOfItem
                            )
                        } else {
                            (fragment as KirillizaFragment).deleteFromDb(
                                positionOfItem,
                                positionOfItem - 1
                            )
                        }
                        true
                    }
                    R.id.unagan -> {
                        val fragment: Fragment =
                            activity.supportFragmentManager.findFragmentByTag("fragmentTag")!!
                        (fragment as KirillizaFragment).viewModel.favoritesResponse.observe(fragment.viewLifecycleOwner,
                            Observer { favorites ->
                                val first: String
                                val second: String

                                var id = 0
                                for (favorite in favorites) {
                                    if (favorite.favoritesID > id) {
                                        id = favorite.favoritesID
                                    }
                                }
                                id += 1

                                if (responseMessages[positionOfItem].isMe) {
                                    first = responseMessages[positionOfItem].text
                                    second = responseMessages[positionOfItem + 1].text
                                } else {
                                    first = responseMessages[positionOfItem - 1].text
                                    second = responseMessages[positionOfItem].text
                                }
                                fragment.viewModel.insertAllFavorites(
                                    listOf(
                                        Favorites(
                                            id,
                                            first,
                                            second
                                        )
                                    )
                                )
                            })
                        fragment.viewModel.getAllFavorites()
                        true
                    }
                    else -> false
                }
            }
            holder.textView.setOnClickListener {
                positionOfItem = position
                menuPopupHelper.show()
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (responseMessages[position].isMe) {
                R.layout.me_bubble
            } else R.layout.bot_buble
        }

        fun setItemsWithDiff(list: List<ResponseClass>) {
            myDiffUtil.setItems(responseMessages, list)
            val diffResult = DiffUtil.calculateDiff(myDiffUtil, false)
            responseMessages.clear()
            responseMessages.addAll(list)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    fun deleteFromDb(index1: Int, index2: Int) {
        Log.d("KirillizaFragment", "size: ${responseMessageList.size}")
        val removed1 = responseMessageList.removeAt(index1)
        val removed2 = responseMessageList.removeAt(index2)
        messageAdapter.setItemsWithDiff(responseMessageList)
        viewModel.deleteResponse(removed1)
        viewModel.deleteResponse(removed2)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val userEnteredText = userInput.text.toString()
        pref = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        pref.edit().putString(USER_INPUT, userEnteredText).putBoolean("Reverse", true)
            .apply()
    }

    override fun onDestroy() {
        executorService.shutdown()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        pref = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        if (pref.getString(USER_INPUT, "")!!.isNotEmpty() &&
            pref.getBoolean("Reverse", false)
        ) {
            userInput.setText(pref.getString(USER_INPUT, ""))
            userInput.setSelection(userInput.text.toString().length - 1)
            pref.edit().putString(USER_INPUT, "").putBoolean("Reverse", false).apply()
        }
        conversation.addOnScrollListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        conversation.removeOnScrollListener(scrollListener)
        viewModel.insertAll(responseMessageList)
    }

    override fun onStop() {
        super.onStop()
        viewModel.insertAll(responseMessageList)
    }
}
