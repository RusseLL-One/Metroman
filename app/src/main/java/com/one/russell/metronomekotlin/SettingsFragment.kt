package com.one.russell.metronomekotlin

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_settings.*
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


        //tvAccentSoundValue.text = model.prefs.getAccentSoundId().toString()
        //tvBeatSoundValue.text = model.prefs.getBeatSoundId().toString()
        tvAccentSoundValue.text = model.accentSoundLiveData.value.toString()
        tvBeatSoundValue.text = model.beatSoundLiveData.value.toString()

        tvAccentSoundValue.setOnClickListener {
            try {
                var accentSoundId = tvAccentSoundValue.text.toString().toInt()
                accentSoundId++

                if(accentSoundId > 3) {
                    accentSoundId = 1
                }
                tvAccentSoundValue.text = accentSoundId.toString()
                model.setAccentSoundId(accentSoundId)

            } catch (e: NumberFormatException) {
            }
        }

        tvBeatSoundValue.setOnClickListener {
            try {
                var beatSoundId = tvBeatSoundValue.text.toString().toInt()
                beatSoundId++

                if(beatSoundId > 3) {
                    beatSoundId = 1
                }
                tvBeatSoundValue.text = beatSoundId.toString()
                model.setBeatSoundId(beatSoundId)

            } catch (e: NumberFormatException) {
            }
        }
    }
}
