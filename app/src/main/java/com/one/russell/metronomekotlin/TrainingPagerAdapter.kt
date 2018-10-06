package com.one.russell.metronomekotlin

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class TrainingPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val fragmentsCount = 3

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> TempoIncreasingFragment()
            1 -> BarDropFragment()
            2 -> BeatDropFragment()
            else -> Fragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Tempo increasing"
            1 -> "Bar drop"
            2 -> "Beat drop"
            else -> null
        }
    }

    override fun getCount(): Int {
        return fragmentsCount
    }

}