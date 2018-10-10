package com.one.russell.metronomekotlin.Fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.one.russell.metronomekotlin.App
import com.one.russell.metronomekotlin.MainViewModel
import com.one.russell.metronomekotlin.R
import kotlin.properties.Delegates
import kotlinx.android.synthetic.main.fragment_training_bar_drop.*

class BarDropFragment : Fragment() {

    private var model: MainViewModel by Delegates.notNull()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity
        if(activity != null) {
            model = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        }
        return inflater.inflate(R.layout.fragment_training_bar_drop, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        barMuteChanceValue.value = 50

        //endValue.tvValue.text = "160"

        val byRandomStr = App.getAppInstance().resources.getString(R.string.by_random)
        val byCountStr = App.getAppInstance().resources.getString(R.string.by_count)
        barDropTabLayout.addTab(barDropTabLayout.newTab().setText(byRandomStr ?: "By random"))
        barDropTabLayout.addTab(barDropTabLayout.newTab().setText(byCountStr ?: "By count"))

        barDropTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(p0: TabLayout.Tab?) {
                when (barDropTabLayout.selectedTabPosition) {
                    0 -> {
                        tvNormalBars.visibility = View.INVISIBLE
                        normalValue.visibility = View.INVISIBLE
                        tvMutedBars.visibility = View.INVISIBLE
                        mutedValue.visibility = View.INVISIBLE
                        barCountFrame.visibility = View.INVISIBLE

                        barMuteChanceValue.visibility = View.VISIBLE
                        tvMuteChance.visibility = View.VISIBLE
                        barMuteChanceFrame.visibility = View.VISIBLE
                    }
                    1 -> {
                        barMuteChanceValue.visibility = View.INVISIBLE
                        tvMuteChance.visibility = View.INVISIBLE
                        barMuteChanceFrame.visibility = View.INVISIBLE

                        tvNormalBars.visibility = View.VISIBLE
                        normalValue.visibility = View.VISIBLE
                        tvMutedBars.visibility = View.VISIBLE
                        mutedValue.visibility = View.VISIBLE
                        barCountFrame.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }
        })
    }
}