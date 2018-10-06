package com.one.russell.metronomekotlin

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlin.properties.Delegates
import kotlinx.android.synthetic.main.fragment_training_beat_drop.*
import kotlinx.android.synthetic.main.item_settings_param.view.*

class BeatDropFragment : Fragment() {

    private var model: MainViewModel by Delegates.notNull()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity
        if(activity != null) {
            model = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        }
        return inflater.inflate(R.layout.fragment_training_beat_drop, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        chanceValue.tvValue.setText("50")
        //endValue.tvValue.text = "160"

        chanceValue.ivIncrease.setOnClickListener {
            try {
                var chance = chanceValue.tvValue.text.toString().toInt()
                chance++

                if (chance <= 100) {
                    chanceValue.tvValue.setText(chance.toString())
                }
            } catch (e: NumberFormatException) {
            }
        }

        chanceValue.ivDecrease.setOnClickListener {
            try {
                var chance = chanceValue.tvValue.text.toString().toInt()
                chance--

                if (chance >= 0) {
                    chanceValue.tvValue.setText(chance.toString())
                }
            } catch (e: NumberFormatException) {
            }
        }

        btStart.setOnClickListener {
            try {
                val trainingType = TrainingType.BEAT_DROPPING
                val chance = chanceValue.tvValue.text.toString().toInt()

                val params = Bundle()
                params.putString("trainingType", trainingType.name)
                params.putInt("chance", chance)

                model.startTraining(params)
                activity?.supportFragmentManager?.popBackStack()
            } catch (e: NumberFormatException) {
            }
        }
    }
}