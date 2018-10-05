package com.one.russell.metronomekotlin

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class TrainingPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val fragmentsCount = 3

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> TempoIncreasingFragment()
            1 -> BeatDropFragment()
            2 -> BarDropFragment()
            else -> Fragment()
        }
    }

    override fun getCount(): Int {
        return fragmentsCount
    }

}