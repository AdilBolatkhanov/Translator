package com.example.translate.network.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.common.data.Favorites
import com.example.common.data.TranslatedResponse
import com.example.common.mvp.BaseFragment
import com.example.common.utility.ItemDecoration
import com.example.common.utility.Utility
import com.example.translate.R
import com.example.translate.network.model.TranslatedWord
import kotlinx.android.synthetic.main.fragment_translate.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val RUS_ENG = "ru-en"
const val ENG_RUS = "en-ru"
private const val THEMEPREF = "THEMEPREF"
private const val DarkTHEME = "DarkTheme"
const val USER_INPUT = "USER_INPUT"
const val AUTH = "auth"
const val EMAIL = "email"
const val USER_ID = "uid"
const val NAME = "name"
const val URL_ACC = "url"
const val TIME = "time"
const val QUANTITY = "quantity"
const val STARTTIME = "start"

class TranslateFragment : BaseFragment<TranslateContract.View, TranslateContract.Presenter>(),
    TranslateContract.View {

    override val presenter: TranslatePresenter by viewModel()
    private var chosenLanguage = RUS_ENG
    private var messageId = 1
    private var responseMessageList = mutableListOf<TranslatedResponse>()
    private var themePref: SharedPreferences? = null
    private lateinit var pref: SharedPreferences
    private var executorService: ExecutorService = Executors.newSingleThreadExecutor()
    lateinit var layoutManager: LinearLayoutManager
    private lateinit var messageAdapter: TranslateAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_translate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialSetup()

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        conversation.layoutManager = layoutManager
        conversation.addItemDecoration(ItemDecoration(3, 3))

        scrollFAB.hide()
        presenter.getAllDataFromDB()

        scrollFAB.setOnClickListener {
            scrollToBottom()
        }

        sendMessageImage.setOnClickListener {
            var userInputStr = userInput.text.toString()
            if (userInputStr.isNotEmpty()) {
                val responseMessage = TranslatedResponse(
                    messageId++,
                    userInputStr,
                    true
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
                responseMessageList.add(responseMessage)
                presenter.loadFromNetwork(userInputStr, chosenLanguage)
            }
        }

        setupToolbarActions()
    }

    override fun showTranslatedWord(translatedWord: TranslatedWord) {
        val res = translatedWord.text[0]
        val responseMessage2 =
            TranslatedResponse(messageId++, res, false)
        responseMessageList.add(responseMessage2)
        messageAdapter.setItems(responseMessageList)
        userInput.setText("")
        if (!isLastVisible())
            conversation.smoothScrollToPosition(messageAdapter.itemCount - 1)
        userInput.setSelection(userInput.length())
    }


    override fun showAllDataFromDB(responseList: List<TranslatedResponse>) {
        Log.d("TranslateFragment", "${responseList.size}")
        if (responseList == null || responseList.isEmpty()) {
            responseMessageList = mutableListOf()
            messageId = 1
        } else {
            responseMessageList.clear()
            responseMessageList.addAll(responseList)
            scrollFAB.show()
            messageId = responseMessageList[responseMessageList.size - 1].responseId + 1
        }
        Log.d("TranslateFragment", "${responseMessageList.size}")
        messageAdapter =
            TranslateAdapter()
        messageAdapter.setActivity(activity as AppCompatActivity, context)
        messageAdapter.setItems(responseMessageList)
        conversation.adapter = messageAdapter
    }

    private fun initialSetup(){
        themePref = activity?.getSharedPreferences(THEMEPREF, Context.MODE_PRIVATE)
        if (themePref?.getBoolean(DarkTHEME, false) == true) {
            containerRel.background = resources.getDrawable(R.drawable.nightmode)
        } else {
            containerRel.background = resources.getDrawable(R.drawable.background)
        }
        (activity as AppCompatActivity).findViewById<TextView>(R.id.toolbarText).text = "Translate"
    }

    private fun setupToolbarActions(){
        val textView = (activity as AppCompatActivity).findViewById<TextView>(R.id.exchangeTextView)
        val toolbar = (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)
        toolbar.removeView(textView)
        val overflowBtn =
            (activity as AppCompatActivity).findViewById<ImageView>(R.id.overflowToolbar)
        overflowBtn.setImageResource(R.drawable.ic_exchange)
        val options = TextView(context)
        val layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.WRAP_CONTENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layoutParams.marginEnd = 50
        layoutParams.rightMargin = 50
        layoutParams.gravity = Gravity.RIGHT
        options.layoutParams = layoutParams
        options.setText(R.string.rus_eng)
        options.id = R.id.exchangeTextView
        options.setTextAppearance(activity, R.style.ToolbarTextViewExchange)
        toolbar.apply {
            removeView(overflowBtn)
            addView(options)
            addView(overflowBtn)
        }

        overflowBtn.setOnClickListener {
            if (chosenLanguage == RUS_ENG) {
                chosenLanguage = ENG_RUS
                options.text = "Eng - Рус"
                Toast.makeText(context, "Eng-Rus", Toast.LENGTH_SHORT).show();
            } else {
                chosenLanguage = RUS_ENG
                options.text = "Рус - Eng"
                Toast.makeText(context, "Rus-Eng", Toast.LENGTH_SHORT).show();
            }
        }

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.exchange_popup)
        val window = dialog.window
        window!!.setGravity(Gravity.TOP or Gravity.END)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val rusEng = dialog.findViewById<TextView>(R.id.ruEngTV)
        rusEng.setOnClickListener {
            chosenLanguage = RUS_ENG
            options.text = requireContext().resources.getString(R.string.rus_eng)
            dialog.dismiss()
            Toast.makeText(context, "Rus-Eng", Toast.LENGTH_SHORT).show()
        }
        val engRus = dialog.findViewById<TextView>(R.id.engRusTx)
        engRus.setOnClickListener {
            chosenLanguage = ENG_RUS
            options.text = requireContext().resources.getString(R.string.eng_rus)
            dialog.dismiss()
            Toast.makeText(context, "Eng-Rus", Toast.LENGTH_SHORT).show()
        }
        options.setOnClickListener { dialog.show() }
    }


    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            var lastPosition = layoutManager.findLastVisibleItemPosition()
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

    class TranslateAdapter : RecyclerView.Adapter<TranslateAdapter.ViewHolder>() {
        private val responseMessages = mutableListOf<TranslatedResponse>()
        private lateinit var context: Context
        private lateinit var activity: FragmentActivity
        private var positionOfItem = 0

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var textView: TextView = itemView.findViewById(R.id.textMessage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
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
                            (fragment as TranslateFragment).deleteFromDb(
                                positionOfItem + 1,
                                positionOfItem
                            )
                        } else {
                            (fragment as TranslateFragment).deleteFromDb(
                                positionOfItem,
                                positionOfItem - 1
                            )
                        }
                        true
                    }
                    R.id.unagan -> {
                        val fragment: Fragment =
                            activity.supportFragmentManager.findFragmentByTag("fragmentTag")!!
                        val executor = Executors.newSingleThreadExecutor()
                        executor.execute {
                            var first = ""
                            var second = ""
                            (fragment as TranslateFragment).presenter.getAllFavoriteDataFromDB()
                            if (responseMessages[positionOfItem].isMe) {
                                first = responseMessages[positionOfItem].text
                                second = responseMessages[positionOfItem + 1].text
                            } else {
                                first = responseMessages[positionOfItem - 1].text
                                second = responseMessages[positionOfItem].text
                            }
                            fragment.presenter.insertFavoriteRecordToDB(
                                listOf(
                                    Favorites(
                                        first,
                                        second
                                    )
                                )
                            )
                        }
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

        fun setItems(list: List<TranslatedResponse>) {
            responseMessages.clear()
            responseMessages.addAll(list)
            notifyDataSetChanged()
        }
    }

    fun deleteFromDb(index1: Int, index2: Int) {
        Log.d("TranslateFragment", "size: ${responseMessageList.size}")
        val removed1 = responseMessageList.removeAt(index1)
        val removed2 = responseMessageList.removeAt(index2)
        messageAdapter.setItems(responseMessageList)
        presenter.deleteWordsFromDb(removed1, removed2)
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
        if (pref.getString(USER_INPUT, "")!!.isNotEmpty()
            && pref.getBoolean("Reverse", false)
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
        presenter.insertAllDataToDB(responseMessageList)
    }



}