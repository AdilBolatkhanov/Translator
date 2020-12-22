package com.example.translatorkotlin.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.translatorkotlin.R
import kotlinx.android.synthetic.main.fragment_my_statistics.*

class MyStatisticsFragment : Fragment(R.layout.fragment_my_statistics) {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setValuesFromPreferences()
    }

    private fun setValuesFromPreferences() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        quatntityText.text = "${sharedPref.getInt(QUANTITY, 0)}"
        timeText.text = "${sharedPref.getString(TIME, "0")} мин"
    }
}
