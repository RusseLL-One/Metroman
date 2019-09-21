package com.one.russell.metronome_kotlin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.one.russell.metronome_kotlin.fragments.BarDropFragment
import com.one.russell.metronome_kotlin.fragments.BeatDropFragment
import com.one.russell.metronome_kotlin.fragments.TempoIncreasingFragment

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
            0 -> App.getAppInstance().resources.getString(R.string.tempo_increasing)
            1 -> App.getAppInstance().resources.getString(R.string.bar_drop)
            2 -> App.getAppInstance().resources.getString(R.string.beat_drop)
            else -> null
        }
    }

    override fun getCount(): Int {
        return fragmentsCount
    }

}