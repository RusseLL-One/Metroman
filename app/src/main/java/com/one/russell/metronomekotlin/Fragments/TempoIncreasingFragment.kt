package com.one.russell.metronomekotlin.Fragments

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.one.russell.metronomekotlin.MAX_BPM
import com.one.russell.metronomekotlin.MIN_BPM
import com.one.russell.metronomekotlin.R
import kotlinx.android.synthetic.main.fragment_training_tempo_increasing.*

class TempoIncreasingFragment : Fragment() {

    private val clickListener = View.OnClickListener {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_training_tempo_increasing, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tempoIncTabLayout.addTab(tempoIncTabLayout.newTab().setText("By bars"))
        tempoIncTabLayout.addTab(tempoIncTabLayout.newTab().setText("By time"))

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
        startValue.value = 90

        endValue.minValue = startValue.value
        endValue.maxValue = MAX_BPM
        endValue.value = 160

        endValue.minValue = startValue.value
        endValue.maxValue = MAX_BPM
        endValue.value = 160

        endValue.minValue = startValue.value
        endValue.maxValue = MAX_BPM
        endValue.value = 160

        barsValue.minValue = 1
        barsValue.maxValue = 16
        barsValue.value = 1

        increaseValue.minValue = 1
        increaseValue.maxValue = MAX_BPM
        increaseValue.value = 5

        startValue.setOnValueChangedListener { _, _, value ->
            endValue.minValue = value
        }

        endValue.setOnValueChangedListener { _, _, value ->
            startValue.maxValue = value
        }
    }
}
