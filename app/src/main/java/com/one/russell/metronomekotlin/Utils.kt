package com.one.russell.metronomekotlin

import android.content.res.Resources

class Utils {

    companion object {
        fun getPixelsFromDp(dp: Int): Int {
            return (Resources.getSystem().displayMetrics.density * dp).toInt()
        }

        fun getDpFromPixels(pixels: Int): Int {
            return (pixels / Resources.getSystem().displayMetrics.density).toInt()
        }
    }
}
