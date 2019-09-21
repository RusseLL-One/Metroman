package com.one.russell.metronome_kotlin.views

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.one.russell.metronome_kotlin.BeatType
import com.one.russell.metronome_kotlin.MAX_BEATS_PER_BAR
import com.one.russell.metronome_kotlin.Utils

class BeatsContainerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private var viewListener: onViewChangedListener? = null
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
        if (measuredWidth != 0) {
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
                if (beatWidth < Utils.getPixelsFromDp(30)) beatWidth = Utils.getPixelsFromDp(30)
                (beatsViewList[index].layoutParams as LinearLayout.LayoutParams).width = beatWidth

                beatsViewList[index].setOnClickListener {
                    (it as BeatView).onTap()
                    viewListener?.onViewChanged()
                }
            }
        }
    }

    fun getBeatTypeList(): ArrayList<BeatType> {
        val beatsTypeList = ArrayList<BeatType>()
        for(item in beatsViewList) {
            beatsTypeList.add(item.beatType)
        }
        return beatsTypeList
    }

    fun getBeatTypeListAsString(): String {
        val result = StringBuilder()
        for(beat in beatsViewList) {
            val type = beat.beatType.ordinal
            result.append(type)
        }
        return result.toString()
    }

    fun setBeatViewListFromString(values: String) {
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (values.isEmpty()) return true
                val count = values.length
                setBeatsPerBar(count)
                try {
                    for (index in values.indices) {
                        val beat = values[index].toInt() - '0'.toInt()

                        if (beat >= BeatType.values.size) throw NumberFormatException()

                        if (!beatsViewList.isEmpty()) {
                            beatsViewList[index].beatType = BeatType.values[beat]
                        }
                    }
                } catch (e: NumberFormatException) {
                    val newValues = "3111"
                    for (index in newValues.indices) {
                        val beat = newValues[index].toInt() - '0'.toInt()

                        if (!beatsViewList.isEmpty()) {
                            beatsViewList[index].beatType = BeatType.values[beat]
                        }
                    }
                }
                invalidate()
                viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    fun setOnViewChangedListener(l: (View?) -> Unit) {
        viewListener = object: onViewChangedListener {
            override fun onViewChanged() {
                l(this@BeatsContainerView)
            }
        }
    }

    fun animateBeat(index: Int) {
        beatsViewList[index].startColorAnimation()
    }
}


interface onViewChangedListener {
    fun onViewChanged()
}