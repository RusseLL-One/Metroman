package com.one.russell.metronome_kotlin.fragments

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.core.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.one.russell.metronome_kotlin.MAX_BEATS_PER_BAR
import com.one.russell.metronome_kotlin.MAX_VALUES_OF_BEAT
import com.one.russell.metronome_kotlin.MainViewModel
import com.one.russell.metronome_kotlin.R
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.android.synthetic.main.subfragment_beatsize.*
import kotlin.properties.Delegates

class BeatsizeSubfragment : Fragment() {

    private var model: MainViewModel by Delegates.notNull()
    private var handler = Handler()
    private var runnable : Runnable? = null
    private val displayedValues = arrayOf("1", "2", "4", "8", "16", "32", "64")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity
        if(activity != null) {
            model = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        }

        return inflater.inflate(R.layout.subfragment_beatsize, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val cont = context

        npBeatsPerBar.maxValue = MAX_BEATS_PER_BAR
        npBeatsPerBar.minValue = 1
        npBeatsPerBar.wrapSelectorWheel = false
        if(cont != null) {
            npBeatsPerBar.typeface = ResourcesCompat.getFont(cont, R.font.xolonium_regular)
        }

        npValueOfBeat.maxValue = MAX_VALUES_OF_BEAT
        npValueOfBeat.minValue = 1
        npValueOfBeat.displayedValues = displayedValues
        npValueOfBeat.wrapSelectorWheel = false

        if(cont != null) {
            (npValueOfBeat as NumberPicker).typeface = ResourcesCompat.getFont(cont, R.font.xolonium_regular)
        }

        model.beatsPerBarLiveData.observe(this, object : Observer<Int> {
            override fun onChanged(it: Int?) {
                if (it != null) {
                    npBeatsPerBar.value = it
                    model.beatsPerBarLiveData.removeObserver(this)
                }
            }
        })

        model.valueOfBeatsLiveData.observe(this, object : Observer<Int> {
            override fun onChanged(it: Int?) {
                if (it != null) {
                    npValueOfBeat.value = displayedValues.indexOf(it.toString()) + 1
                    model.valueOfBeatsLiveData.removeObserver(this)
                }
            }
        })

        npBeatsPerBar.setOnValueChangedListener { _, _, newValue ->
            handler.removeCallbacks(runnable)
            runnable = Runnable {
                model.beatsPerBarLiveData.postValue(newValue)
            }
            handler.postDelayed(runnable, 500)
        }

        npValueOfBeat.setOnValueChangedListener { _, _, index ->
            //todo смена длительности ноты
            try {
                val value: Int = displayedValues[index - 1].toInt()
                model.valueOfBeatsLiveData.postValue(value)
            } catch (e: NumberFormatException) {}
        }
    }
}