package com.one.russell.metronomekotlin.views

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.one.russell.metronomekotlin.BeatType
import com.one.russell.metronomekotlin.MAX_BEATS_PER_BAR
import com.one.russell.metronomekotlin.Utils

class BeatsContainerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private val beatsViewList = ArrayList<BeatView>()
    private val llBeats = LinearLayout(context)
    private var beatsPerBar = 4

    init {
        llBeats.orientation = LinearLayout.HORIZONTAL
        val transition = LayoutTransition()
        transition.enableTransitionType(LayoutTransition.CHANGING)
        transition.setInterpolator(LayoutTransition.CHANGING, AccelerateDecelerateInterpolator())
        llBeats.layoutTransition = transition
        addView(llBeats)

        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)

                refresh()
                return true
            }
        })
    }

    fun setBeatsPerBar(value: Int) {
        beatsPerBar = value
        refresh()
    }

    fun refresh() {
        if(measuredWidth != 0) {
            val newItemsCount = beatsPerBar - beatsViewList.size
            if (newItemsCount > 0) {
                for (i in beatsViewList.size until beatsPerBar) {
                    val view = if (i == 0) BeatView(context, BeatType.ACCENT)
                    else BeatView(context, BeatType.BEAT)
                    beatsViewList.add(view)

                    val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            Utils.getPixelsFromDp(60),
                            1f)
                    view.minimumWidth = 500
                    llBeats.addView(view, params)
                }
            } else if (newItemsCount < 0) {
                for (i in beatsViewList.size - 1 downTo beatsPerBar) {
                    beatsViewList.removeAt(i)
                    llBeats.removeViewAt(i)
                }
            }
            val gap = (Utils.getPixelsFromDp(16) * (1 - beatsViewList.size.toFloat() / (MAX_BEATS_PER_BAR + 4))).toInt()
            for (index in beatsViewList.indices) {
                (beatsViewList[index].layoutParams as LinearLayout.LayoutParams).leftMargin =
                        if (index == 0) 0 else gap
                var beatWidth = ((measuredWidth - (gap * (beatsViewList.size - 1))).toFloat() / beatsViewList.size).toInt()
                if(beatWidth < Utils.getPixelsFromDp(30)) beatWidth = Utils.getPixelsFromDp(30)
                (beatsViewList[index].layoutParams as LinearLayout.LayoutParams).width = beatWidth
            }
        }
    }

    fun getBeatViewList(): ArrayList<BeatView> {
        return beatsViewList
    }

    fun animateBeat(index: Int) {
        beatsViewList[index].startColorAnimation()
    }
}