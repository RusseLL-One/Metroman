package com.one.russell.metronomekotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        tvAccentSoundValue.setOnClickListener {
            try {
                var beatsPerBar = tvAccentSoundValue.text.toString().toInt()
                beatsPerBar++

                if(beatsPerBar <= 3) {
                    tvAccentSoundValue.text = beatsPerBar.toString()
                } else {
                    tvAccentSoundValue.text = "1"
                }
            } catch (e: NumberFormatException) {
            }
        }
    }
}
