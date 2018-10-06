package com.one.russell.metronomekotlin

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator


class BeatView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val greyPaint = Paint()
    val whitePaint = Paint()
    var accentBoxHeight = 0f
    var beatType = BeatType.BEAT
    var bordersImage: Bitmap? = null

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

                val drawable = context.resources.getDrawable(R.drawable.beats)

                bordersImage = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bordersImage)
                drawable.setBounds(0, 0, measuredWidth, measuredHeight)
                drawable.draw(canvas)
                return true
            }
        })
        greyPaint.color = Color.GRAY
        whitePaint.color = Color.WHITE
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
        greyPaint.colorFilter = background.colorFilter
        whitePaint.colorFilter = background.colorFilter
        //todo всё ломается при изменении количества ударов в такте
        canvas?.drawRoundRect(RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat()), Utils.getPixelsFromDp(5).toFloat(), Utils.getPixelsFromDp(5).toFloat(), whitePaint)
        canvas?.drawRoundRect(RectF(0f, accentBoxHeight, measuredWidth.toFloat(), measuredHeight.toFloat()), Utils.getPixelsFromDp(5).toFloat(), Utils.getPixelsFromDp(5).toFloat(), greyPaint)
        if(bordersImage != null) {
            canvas?.drawBitmap(bordersImage, 0f, 0f, Paint())
        }
        //canvas?.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), greyPaint)

        super.onDraw(canvas)
    }
}