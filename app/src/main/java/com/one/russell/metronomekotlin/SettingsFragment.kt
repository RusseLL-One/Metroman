package com.one.russell.metronomekotlin

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.item_settings_param.view.*
import kotlin.properties.Delegates

class SettingsFragment : Fragment() {

    private var model: MainViewModel by Delegates.notNull()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity
        if(activity != null) {
            model = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        }
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        accentValue.tvValue.text = model.accentSoundLiveData.value.toString()
        beatValue.tvValue.text = model.beatSoundLiveData.value.toString()

        accentValue.ivIncrease.setOnClickListener {
            try {
                var accentSoundId = accentValue.tvValue.text.toString().toInt()
                accentSoundId++

                if(accentSoundId <= 3) {
                    accentValue.tvValue.text = accentSoundId.toString()
                    model.setAccentSoundId(accentSoundId)
                }
            } catch (e: NumberFormatException) {
            }
        }

        accentValue.ivDecrease.setOnClickListener {
            try {
                var accentSoundId = accentValue.tvValue.text.toString().toInt()
                accentSoundId--

                if(accentSoundId >= 1) {
                    accentValue.tvValue.text = accentSoundId.toString()
                    model.setAccentSoundId(accentSoundId)
                }
            } catch (e: NumberFormatException) {
            }
        }

        beatValue.ivIncrease.setOnClickListener {
            try {
                var beatSoundId = beatValue.tvValue.text.toString().toInt()
                beatSoundId++

                if(beatSoundId <= 3) {
                    beatValue.tvValue.text = beatSoundId.toString()
                    model.setBeatSoundId(beatSoundId)
                }
            } catch (e: NumberFormatException) {
            }
        }

        beatValue.ivDecrease.setOnClickListener {
            try {
                var beatSoundId = beatValue.tvValue.text.toString().toInt()
                beatSoundId--

                if(beatSoundId >= 1) {
                    beatValue.tvValue.text = beatSoundId.toString()
                    model.setBeatSoundId(beatSoundId)
                }
            } catch (e: NumberFormatException) {
            }
        }
    }
}
