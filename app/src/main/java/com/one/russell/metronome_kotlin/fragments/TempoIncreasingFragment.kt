package com.one.russell.metronome_kotlin.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.core.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.one.russell.metronome_kotlin.*
import kotlinx.android.synthetic.main.fragment_training_tempo_increasing.*
import kotlin.properties.Delegates

class TempoIncreasingFragment : Fragment() {

    private var model: MainViewModel by Delegates.notNull()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity
        if (activity != null) {
            model = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        }
        return inflater.inflate(R.layout.fragment_training_tempo_increasing, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val byBarsStr = App.getAppInstance().resources.getString(R.string.by_bars)
        val byTimeStr = App.getAppInstance().resources.getString(R.string.by_time)
        tempoIncTabLayout.addTab(tempoIncTabLayout.newTab().setText(byBarsStr ?: "By bars"))
        tempoIncTabLayout.addTab(tempoIncTabLayout.newTab().setText(byTimeStr ?: "By time"))

        tempoIncTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(p0: TabLayout.Tab?) {
                when (tempoIncTabLayout.selectedTabPosition) {
                    0 -> {
                        tvTime.visibility = View.INVISIBLE
                        timeValue.visibility = View.INVISIBLE
                        timeFrame.visibility = View.INVISIBLE

                        tvBars.visibility = View.VISIBLE
                        tvIncrease.visibility = View.VISIBLE
                        barsValue.visibility = View.VISIBLE
                        increaseValue.visibility = View.VISIBLE
                        barsFrame.visibility = View.VISIBLE
                    }
                    1 -> {
                        tvBars.visibility = View.INVISIBLE
                        tvIncrease.visibility = View.INVISIBLE
                        barsValue.visibility = View.INVISIBLE
                        increaseValue.visibility = View.INVISIBLE
                        barsFrame.visibility = View.INVISIBLE

                        tvTime.visibility = View.VISIBLE
                        timeValue.visibility = View.VISIBLE
                        timeFrame.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }
        })

        startValue.minValue = MIN_BPM
        startValue.maxValue = 160
        startValue.value = model.tempoIncStartValue
        startValue.wrapSelectorWheel = false

        endValue.minValue = startValue.value
        endValue.maxValue = MAX_BPM
        endValue.value = model.tempoIncEndValue
        endValue.wrapSelectorWheel = false

        barsValue.minValue = 1
        barsValue.maxValue = 16
        barsValue.value = model.tempoIncBarsValue
        barsValue.wrapSelectorWheel = false

        increaseValue.minValue = 1
        increaseValue.maxValue = MAX_BPM
        increaseValue.value = model.tempoIncIncreaseValue
        increaseValue.wrapSelectorWheel = false

        timeValue.minValue = 1
        timeValue.maxValue = 180
        timeValue.value = model.tempoIncTimeValue
        timeValue.wrapSelectorWheel = false

        val cont = context
        if(cont != null) {
            startValue.typeface = ResourcesCompat.getFont(cont, R.font.xolonium_regular)
            endValue.typeface = ResourcesCompat.getFont(cont, R.font.xolonium_regular)
            barsValue.typeface = ResourcesCompat.getFont(cont, R.font.xolonium_regular)
            increaseValue.typeface = ResourcesCompat.getFont(cont, R.font.xolonium_regular)
            timeValue.typeface = ResourcesCompat.getFont(cont, R.font.xolonium_regular)
        }

        startValue.setOnValueChangedListener { _, _, value ->
            endValue.minValue = value
            endValue.wrapSelectorWheel = false
        }

        endValue.setOnValueChangedListener { _, _, value ->
            startValue.maxValue = value
            startValue.wrapSelectorWheel = false
        }
    }
}
