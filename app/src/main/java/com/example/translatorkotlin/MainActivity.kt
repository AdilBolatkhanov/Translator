package com.example.translatorkotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.common.data.User
import com.example.cyrillic.ui.KirillizaFragment
import com.example.favorites.ui.FavoriteFragment
import com.example.translate.network.ui.TranslateFragment
import com.example.translatorkotlin.fragments.account.*
import com.example.translatorkotlin.fragments.media.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_translator.*
import java.util.concurrent.TimeUnit

private const val THEMEPREF = "THEMEPREF"
private const val DarkTHEME = "DarkTheme"
private const val LASTFRAGMENT = "FRAGMENT"
private const val RESTART = "Restart"

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var myFragment: Fragment
    private lateinit var themePref: SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    private val database = Firebase.database

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translator)

        setupToolbar()
        setupIcon()

        setListeners()
        checkSavedState(savedInstanceState)
        setFragment()
        setPopupMenu()
    }

    private fun setFragment() {
        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            myFragment, "fragmentTag"
        ).commit()
    }

    private fun checkSavedState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            setCorrectFragment(savedInstanceState.getString(LASTFRAGMENT))
        } else {
            if (themePref.getBoolean(RESTART, false)) {
                setCorrectFragment(themePref.getString(LASTFRAGMENT, ""))
                themePref.edit().putBoolean(RESTART, false).apply()
            } else {
                myFragment = KirillizaFragment()
                nav_view.selectedItemId = R.id.kirillizaFragment
            }
        }
    }

    private fun setListeners() {
        day_night.setOnClickListener {
            if (!themePref.getBoolean(DarkTHEME, false)) {
                with(themePref.edit()) {
                    putBoolean(DarkTHEME, true)
                    apply()
                }
                restartApp()
            } else {
                with(themePref.edit()) {
                    putBoolean(DarkTHEME, false)
                    apply()
                }
                restartApp()
            }
        }
        nav_view.setOnNavigationItemSelectedListener(this)
    }

    private fun setupIcon() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (!themePref.contains(DarkTHEME)) {
            with(themePref.edit()) {
                putBoolean(DarkTHEME, false)
                apply()
            }
        }
        if (themePref.getBoolean(DarkTHEME, false)) {
            day_night.setImageResource(R.drawable.ic_sun)
        } else day_night.setImageResource(R.drawable.ic_moon)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
    }

    private fun setTheme() {
        themePref = applicationContext.getSharedPreferences(THEMEPREF, MODE_PRIVATE)
        if (themePref.getBoolean(DarkTHEME, false)) {
            setTheme(R.style.AppThemeDark)
        } else {
            setTheme(R.style.AppTheme)
        }
    }

    @SuppressLint("RestrictedApi")
    fun setPopupMenu() {
        val wrapper = ContextThemeWrapper(this, R.style.BasePopupMenu)
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
        overflowToolbar.setOnClickListener {
            menuPopupHelper.show()
        }
    }

    private fun sendMail() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("kwenten@mail.ru"))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun setCorrectFragment(fragment: String?) {
        when (fragment) {
            "AccountFragment" -> {
                myFragment = AccountFragment()
                nav_view.selectedItemId = R.id.accountFragment
            }
            "FavoriteFragment" -> {
                myFragment = FavoriteFragment()
                nav_view.selectedItemId = R.id.favoriteFragment
            }
            "SearchFragment" -> {
                myFragment = SearchFragment()
                nav_view.selectedItemId = R.id.searchFragment
            }
            "TranslateFragment" -> {
                myFragment = TranslateFragment()
                nav_view.selectedItemId = R.id.translateFragment
            }
            else -> {
                myFragment = KirillizaFragment()
                nav_view.selectedItemId = R.id.kirillizaFragment
            }
        }
    }

    private fun restartApp() {
        with(themePref.edit()) {
            putString(LASTFRAGMENT, myFragment.javaClass.simpleName)
            putBoolean(RESTART, true)
            apply()
        }
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            LASTFRAGMENT,
            myFragment.javaClass.simpleName
        )
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var selectedFragment: Fragment? = null
        when (item.itemId) {
            R.id.translateFragment -> selectedFragment = TranslateFragment()
            R.id.favoriteFragment -> selectedFragment = FavoriteFragment()
            R.id.kirillizaFragment -> selectedFragment = KirillizaFragment()
            R.id.searchFragment -> selectedFragment = SearchFragment()
            R.id.accountFragment -> selectedFragment = AccountFragment()
        }
        if (selectedFragment != null) {
            myFragment = selectedFragment
        }
        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            myFragment, "fragmentTag"
        ).commit()
        return true
    }

    fun writeToFBDB(
        id: String,
        name: String,
        email: String,
        url: String,
        countWord: String,
        time: String
    ) {
        val myRef: DatabaseReference = database.getReference("users/$id")
        val user = User(url, name, email, countWord, time)
        myRef.setValue(user)
    }

    override fun onStart() {
        super.onStart()
        if (sharedPreferences.getBoolean(AUTH, false)) {
            val start = System.currentTimeMillis()
            sharedPreferences.edit().putString(STARTTIME, "$start").apply()
        }
    }

    override fun onStop() {
        super.onStop()
        if (sharedPreferences.getBoolean(AUTH, false)) {
            val start = sharedPreferences.getString(STARTTIME, "0")!!.toLong()
            val end = System.currentTimeMillis()
            val total = end - start
            val minutes = TimeUnit.MILLISECONDS.toMinutes(total)
            val totalTime = sharedPreferences.getString(TIME, "0")!!.toLong() + minutes
            writeToFBDB(
                sharedPreferences.getString(USER_ID, "")!!,
                sharedPreferences.getString(NAME, "")!!,
                sharedPreferences.getString(EMAIL, "")!!,
                sharedPreferences.getString(URL_ACC, "")!!,
                "${sharedPreferences.getInt(QUANTITY, 0)}",
                "$totalTime"
            )
            sharedPreferences.edit().putString(TIME, "$totalTime").apply()
        }
    }
}
