package com.one.russell.metronomekotlin

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlin.properties.Delegates
import kotlinx.android.synthetic.main.fragment_training_bar_drop.*
import kotlinx.android.synthetic.main.item_settings_param.view.*

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

        chanceValue.tvValue.setText("50")
        //endValue.tvValue.text = "160"

        chanceValue.ivIncrease.setOnClickListener {
            try {
                var chance = chanceValue.tvValue.text.toString().toInt()
                chance++

                if(chance <= 100) {
                    chanceValue.tvValue.setText(chance.toString())
                }
            } catch (e: NumberFormatException) {
            }
        }

        chanceValue.ivDecrease.setOnClickListener {
            try {
                var chance = chanceValue.tvValue.text.toString().toInt()
                chance--

                if(chance >= 0) {
                    chanceValue.tvValue.setText(chance.toString())
                }
            } catch (e: NumberFormatException) {
            }
        }

        normalValue.ivIncrease.setOnClickListener {
            try {
                var normal = normalValue.tvValue.text.toString().toInt()
                normal++

                if(normal <= 50) {
                    normalValue.tvValue.setText(normal.toString())
                }
            } catch (e: NumberFormatException) {
            }
        }

        normalValue.ivDecrease.setOnClickListener {
            try {
                var normal = normalValue.tvValue.text.toString().toInt()
                normal--

                if (normal >= 1) {
                    normalValue.tvValue.setText(normal.toString())
                }
            } catch (e: NumberFormatException) {
            }
        }

        mutedValue.ivIncrease.setOnClickListener {
            try {
                var muted = mutedValue.tvValue.text.toString().toInt()
                muted++

                if(muted <= 100) {
                    mutedValue.tvValue.setText(muted.toString())
                }
            } catch (e: NumberFormatException) {
            }
        }

        mutedValue.ivDecrease.setOnClickListener {
            try {
                var muted = mutedValue.tvValue.text.toString().toInt()
                muted--

                if (muted >= 1) {
                    mutedValue.tvValue.setText(muted.toString())
                }
            } catch (e: NumberFormatException) {
            }
        }

        btStart.setOnClickListener {
            try {
                val trainingType = TrainingType.BAR_DROPPING
                val chance = chanceValue.tvValue.text.toString().toInt()
                val normalBars = normalValue.tvValue.text.toString().toInt()
                val mutedBars = mutedValue.tvValue.text.toString().toInt()

                val params = Bundle()
                params.putString("trainingType", trainingType.name)
                params.putInt("chance", chance)
                params.putInt("normalBars", normalBars)
                params.putInt("mutedBars", mutedBars)

                model.startTraining(params)
                activity?.supportFragmentManager?.popBackStack();
            } catch (e: NumberFormatException) {
            }
        }
    }
}