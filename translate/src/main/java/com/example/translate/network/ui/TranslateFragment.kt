package com.example.translate.network.ui

import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
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
import com.example.translate.network.ui.adapter.TranslateAdapter
import com.example.translate.network.ui.adapter.TranslateItemClickListener
import kotlinx.android.synthetic.main.fragment_translate.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val RUS_ENG = "ru-en"
private const val ENG_RUS = "en-ru"
private const val THEMEPREF = "THEMEPREF"
private const val DarkTHEME = "DarkTheme"
private const val USER_INPUT = "USER_INPUT"
private const val AUTH = "auth"
private const val EMAIL = "email"
private const val USER_ID = "uid"
private const val NAME = "name"
private const val URL_ACC = "url"
private const val TIME = "time"
private const val QUANTITY = "quantity"

class TranslateFragment :
    BaseFragment<TranslateContract.View, TranslateContract.Presenter>(),
    TranslateContract.View, TranslateItemClickListener {

    override val presenter: TranslatePresenter by viewModel()
    private var chosenLanguage = RUS_ENG
    private var responseMessageList = mutableListOf<TranslatedResponse>()
    private var themePref: SharedPreferences? = null
    private lateinit var pref: SharedPreferences
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var messageAdapter: TranslateAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_translate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialSetup()

        setupRecycler()

        scrollFAB.hide()
        presenter.getAllDataFromDB()

        setupClickListeners()

        setupToolbarActions()
    }

    private fun setupClickListeners() {
        scrollFAB.setOnClickListener {
            scrollToBottom()
        }

        sendMessageImage.setOnClickListener {
            val userInputStr = userInput.text.toString()
            if (userInputStr.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val responseMessage = TranslatedResponse(userInputStr, true)
                    incrementQuanAndSave(userInputStr)
                    responseMessageList.add(responseMessage)
                    withContext(Dispatchers.Main) {
                        presenter.insertAllDataToDB(listOf(responseMessage))
                        presenter.loadFromNetwork(userInputStr, chosenLanguage)
                    }
                }
            }
        }
    }

    private fun incrementQuanAndSave(userInputStr: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
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
    }

    private fun setupRecycler() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        conversation.layoutManager = layoutManager
        conversation.addItemDecoration(ItemDecoration(3, 3))
        messageAdapter = TranslateAdapter(this)
        conversation.adapter = messageAdapter
    }

    override fun showTranslatedWord(translatedWord: TranslatedWord) {
        val res = translatedWord.text[0]
        val responseMessage2 = TranslatedResponse(res, false)
        responseMessageList.add(responseMessage2)
        presenter.insertAllDataToDB(listOf(responseMessage2))
        messageAdapter.setItems(responseMessageList)
        clearInput()
    }

    private fun clearInput() {
        userInput.setText("")
        if (!isLastVisible())
            conversation.smoothScrollToPosition(messageAdapter.itemCount - 1)
        userInput.setSelection(userInput.length())
    }

    override fun showAllDataFromDB(list: List<TranslatedResponse>) {
        if (list.isEmpty()) {
            responseMessageList = mutableListOf()
        } else {
            responseMessageList.clear()
            responseMessageList.addAll(list)
            scrollFAB.show()
        }
        messageAdapter.setItems(responseMessageList)
    }

    private fun initialSetup() {
        themePref = activity?.getSharedPreferences(THEMEPREF, Context.MODE_PRIVATE)
        if (themePref?.getBoolean(DarkTHEME, false) == true) {
            containerRel.background = resources.getDrawable(R.drawable.nightmode)
        } else {
            containerRel.background = resources.getDrawable(R.drawable.background)
        }
        (activity as AppCompatActivity).findViewById<TextView>(R.id.toolbarText).text = "Translate"
    }

    private fun setupToolbarActions() {
        val textView = (activity as AppCompatActivity).findViewById<TextView>(R.id.exchangeTextView)
        val toolbar = (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)
        toolbar.removeView(textView)
        val overflowBtn =
            (activity as AppCompatActivity).findViewById<ImageView>(R.id.overflowToolbar)
        overflowBtn.setImageResource(R.drawable.ic_exchange)
        val options = createOptionTextView()
        toolbar.apply {
            removeView(overflowBtn)
            addView(options)
            addView(overflowBtn)
        }

        overflowBtn.setOnClickListener {
            if (chosenLanguage == RUS_ENG) {
                chosenLanguage = ENG_RUS
                options.text = "Eng - Рус"
                Toast.makeText(context, "Eng-Rus", Toast.LENGTH_SHORT).show()
            } else {
                chosenLanguage = RUS_ENG
                options.text = "Рус - Eng"
                Toast.makeText(context, "Rus-Eng", Toast.LENGTH_SHORT).show()
            }
        }

        val dialog = createLanguageChooserDialog(options)
        options.setOnClickListener { dialog.show() }
    }

    private fun createLanguageChooserDialog(options: TextView): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.exchange_popup)
        dialog.window?.let {
            it.setGravity(Gravity.TOP or Gravity.END)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

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
        return dialog
    }

    private fun createOptionTextView(): TextView {
        val options = TextView(context)
        val layoutParams = Toolbar.LayoutParams(
            Toolbar.LayoutParams.WRAP_CONTENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        layoutParams.marginEnd = 50
        layoutParams.rightMargin = 50
        layoutParams.gravity = Gravity.END
        options.layoutParams = layoutParams
        options.setText(R.string.rus_eng)
        options.id = R.id.exchangeTextView
        options.setTextAppearance(activity, R.style.ToolbarTextViewExchange)
        return options
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

    private fun deleteFromDb(index1: Int, index2: Int) {
        val removed1 = responseMessageList.removeAt(index1)
        val removed2 = responseMessageList.removeAt(index2)
        messageAdapter.setItems(responseMessageList)
        presenter.deleteWordsFromDb(removed1, removed2)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val userEnteredText = userInput.text.toString()
        pref = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        pref.edit().putString(USER_INPUT, userEnteredText).putBoolean("Reverse", true).apply()
    }

    override fun onResume() {
        super.onResume()
        setSavedStatePref()
        conversation.addOnScrollListener(scrollListener)
    }

    private fun setSavedStatePref() {
        pref = requireActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        if (pref.getString(USER_INPUT, "")!!.isNotEmpty()
            && pref.getBoolean("Reverse", false)
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

    override fun copyToClipboard(positionOfItem: Int) {
        val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
        val chosenIntent =
            Intent.createChooser(intent, "Send message via...")
        activity?.startActivity(chosenIntent)
    }

    override fun deleteResponseItems(positionOfItem: Int) {
        if (responseMessageList[positionOfItem].isMe) {
            deleteFromDb(positionOfItem + 1, positionOfItem)
        } else {
            deleteFromDb(positionOfItem, positionOfItem - 1)
        }
    }

    override fun addToFavorites(positionOfItem: Int) {
        var first = ""
        var second = ""
        if (responseMessageList[positionOfItem].isMe) {
            first = responseMessageList[positionOfItem].text
            second = responseMessageList[positionOfItem + 1].text
        } else {
            first = responseMessageList[positionOfItem - 1].text
            second = responseMessageList[positionOfItem].text
        }
        presenter.insertFavoriteRecordToDB(listOf(Favorites(first, second)))
    }

}