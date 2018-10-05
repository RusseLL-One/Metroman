package com.one.russell.metronomekotlin

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout



class BeatView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val paint = Paint()
    var accentBoxHeight = 0f
    var beatType = BeatType.BEAT

    constructor(context: Context, beatType: BeatType) : this(context) {
        this.beatType = beatType
    }

    init {
        setBackgroundResource(R.drawable.buttons)

        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)
                accentBoxHeight = when(beatType) {
                    BeatType.ACCENT -> 0f
                    BeatType.SUBACCENT -> (measuredHeight / 2).toFloat()
                    else -> measuredHeight.toFloat()
                }
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        beatType = when(beatType) {
            BeatType.BEAT -> BeatType.SUBACCENT
            BeatType.SUBACCENT -> BeatType.ACCENT
            BeatType.ACCENT -> BeatType.BEAT
        }

        val positionAnimator = when(beatType) {
            BeatType.ACCENT -> ValueAnimator.ofFloat((measuredHeight / 2).toFloat(), 0f)
            BeatType.SUBACCENT -> ValueAnimator.ofFloat(measuredHeight.toFloat(), (measuredHeight / 2).toFloat())
            else -> ValueAnimator.ofFloat(0f, measuredHeight.toFloat())
        }

        //positionAnimator.duration = duration.toLong()
        positionAnimator.interpolator = DecelerateInterpolator()
        positionAnimator.addUpdateListener { animation ->
            accentBoxHeight = animation.animatedValue as Float
            invalidate()
        }

        positionAnimator.start()

        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {

        canvas?.drawRoundRect(RectF(0f, accentBoxHeight, measuredWidth.toFloat(), measuredHeight.toFloat()), Utils.getPixelsFromDp(5).toFloat(), Utils.getPixelsFromDp(5).toFloat(), paint)
        //canvas?.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)

        super.onDraw(canvas)
    }
}