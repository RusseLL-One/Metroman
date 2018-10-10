package com.one.russell.metronomekotlin.Fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.one.russell.metronomekotlin.MainViewModel
import com.one.russell.metronomekotlin.R
import com.one.russell.metronomekotlin.Views.RotaryKnobView
import com.one.russell.metronomekotlin.Views.ScaleView
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

        Glide.with(this)
                .asDrawable()
                .load(R.drawable.background)
                .into(background)

        accentValue.value = model.accentSoundLiveData.value ?: 1
        accentValue.minValue = 1
        accentValue.maxValue = 3

        accentValue.setOnValueChangedListener { _, _, value ->
            model.setAccentSoundId(value)
        }

        /*pickerValue.ivIncrease.setOnClickListener {
            try {
                var pickerId = pickerValue.tvValue.text.toString().toInt()
                pickerId++

                if(pickerId <= 2) {
                    pickerValue.tvValue.setText(pickerId.toString())
                    val knob = activity?.findViewById<RotaryKnobView>(R.id.rotaryKnob)
                    knob?.visibility = View.GONE

                    val scale = activity?.findViewById<ScaleView>(R.id.scaleKnob)
                    scale?.visibility = View.VISIBLE
                }
            } catch (e: NumberFormatException) {
            }
        }

        pickerValue.ivDecrease.setOnClickListener {
            try {
                var pickerId = pickerValue.tvValue.text.toString().toInt()
                pickerId--

                if(pickerId >= 1) {
                    pickerValue.tvValue.setText(pickerId.toString())
                    val knob = activity?.findViewById<RotaryKnobView>(R.id.rotaryKnob)
                    knob?.visibility = View.VISIBLE

                    val scale = activity?.findViewById<ScaleView>(R.id.scaleKnob)
                    scale?.visibility = View.GONE
                }
            } catch (e: NumberFormatException) {
            }
        }*/
    }
}
