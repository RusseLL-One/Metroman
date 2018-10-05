package com.one.russell.metronomekotlin

import android.view.View
import kotlinx.android.synthetic.main.item_settings_param.view.*

class IncrementDecrementListener(private val isIncreasing : Boolean, private val bound: Int) : View.OnClickListener {

    override fun onClick(view: View) {

        try {
            var value = view.tvValue.text.toString().toInt()
            if(isIncreasing) {
                value++
                if(value <= bound) {
                    view.tvValue.text = value.toString()
                }
            }
            else {
                value--
                if(value >= bound) {
                    view.tvValue.text = value.toString()
                }
            }
        } catch (e: NumberFormatException) {
        }
    }
}