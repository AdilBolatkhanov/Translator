package com.example.translatorkotlin.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.translatorkotlin.fragments.GeneralStatisticsFragment
import com.example.translatorkotlin.fragments.MyStatisticsFragment

class StatisticsViewPagerAdapter(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return MyStatisticsFragment()
            1 -> return GeneralStatisticsFragment()
        }
        return Fragment()
    }

    fun getTitle(pos: Int): String {
        return if (pos == 0) {
            "Individual Statistics"
        } else {
            "General Statistics"
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}