package com.one.russell.metronome_kotlin.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.one.russell.metronome_kotlin.MainViewModel
import com.one.russell.metronome_kotlin.R
import kotlin.properties.Delegates
import kotlinx.android.synthetic.main.fragment_training_beat_drop.*

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

        beatMuteChanceValue.minValue = 0
        beatMuteChanceValue.maxValue = 99
        beatMuteChanceValue.value = model.beatDropChanceValue
        beatMuteChanceValue.wrapSelectorWheel = false

        val cont = context
        if(cont != null) {
            beatMuteChanceValue.typeface = ResourcesCompat.getFont(cont, R.font.xolonium_regular)
        }
    }
}