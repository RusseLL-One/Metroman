package com.one.russell.metronome_kotlin.fragments

import androidx.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.one.russell.metronome_kotlin.MainViewModel
import com.one.russell.metronome_kotlin.R
import com.one.russell.metronome_kotlin.TrainingPagerAdapter
import com.one.russell.metronome_kotlin.TrainingType
import kotlinx.android.synthetic.main.fragment_training.*
import kotlinx.android.synthetic.main.fragment_training_bar_drop.*
import kotlinx.android.synthetic.main.fragment_training_beat_drop.*
import kotlinx.android.synthetic.main.fragment_training_tempo_increasing.*
import kotlin.properties.Delegates

class TrainingFragment : Fragment() {

    private var model: MainViewModel by Delegates.notNull()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity
        if (activity != null) {
            model = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        }
        return inflater.inflate(R.layout.fragment_training, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Glide.with(this)
                    .load(R.drawable.background_land)
                    .into(background)
        } else {
            Glide.with(this)
                    .load(R.drawable.background)
                    .into(background)
        }

        vpTraining.adapter = TrainingPagerAdapter(childFragmentManager)
        categoriesTabLayout.setupWithViewPager(vpTraining)

        btStart.setOnClickListener {
            val params = Bundle()
            val selectedTab = categoriesTabLayout.selectedTabPosition
            when (selectedTab) {
                0 -> {
                    params.putInt("startBpm", startValue.value)
                    params.putInt("endBpm", endValue.value)

                    if(tempoIncTabLayout.selectedTabPosition == 1) {
                        val trainingType = TrainingType.TEMPO_INCREASING_BY_TIME
                        params.putString("trainingType", trainingType.name)
                        params.putInt("minutes", timeValue.value)
                    } else {
                        val trainingType = TrainingType.TEMPO_INCREASING_BY_BARS
                        params.putString("trainingType", trainingType.name)
                        params.putInt("bars", barsValue.value)
                        params.putInt("increment", increaseValue.value)
                    }
                }
                1 -> {
                    if(barDropTabLayout.selectedTabPosition == 1) {
                        val trainingType = TrainingType.BAR_DROPPING_BY_COUNT
                        params.putString("trainingType", trainingType.name)
                        params.putInt("normalBars", normalValue.value)
                        params.putInt("mutedBars", mutedValue.value)
                    } else {
                        val trainingType = TrainingType.BAR_DROPPING_RANDOM
                        params.putString("trainingType", trainingType.name)
                        params.putInt("barChance", barMuteChanceValue.value)
                    }
                }
                2 -> {
                    val trainingType = TrainingType.BEAT_DROPPING
                    params.putString("trainingType", trainingType.name)
                    params.putInt("beatChance", beatMuteChanceValue.value)
                }
            }
            model.startTraining(params)
            activity?.supportFragmentManager?.popBackStack()
        }
    }
}
