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
    var colorFilter = ColorFilter()
    var borderWidth = Utils.getPixelsFromDp(2).toFloat()
    var fillRectangle = RectF()

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
                    BeatType.BEAT -> measuredHeight.toFloat() - borderWidth*2
                    else -> (measuredHeight / 4).toFloat()
                }
                fillRectangle = RectF(borderWidth, accentBoxHeight + borderWidth, measuredWidth.toFloat() - borderWidth, measuredHeight.toFloat() - borderWidth)

                val drawable = context.resources.getDrawable(R.drawable.beats)

                /*bordersImage = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bordersImage)
                drawable.setBounds(0, 0, measuredWidth, measuredHeight)
                drawable.draw(canvas)*/
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
            BeatType.ACCENT -> BeatType.MUTE
            BeatType.MUTE -> BeatType.BEAT
        }

        val positionAnimator = when(beatType) {
            BeatType.ACCENT -> ValueAnimator.ofFloat((measuredHeight / 2).toFloat(), 0f)
            BeatType.SUBACCENT -> ValueAnimator.ofFloat(measuredHeight.toFloat(), (measuredHeight / 2).toFloat())
            BeatType.BEAT -> ValueAnimator.ofFloat((measuredHeight / 4).toFloat(), (measuredHeight - borderWidth*2).toFloat())
            else -> ValueAnimator.ofFloat(0f, (measuredHeight / 4).toFloat())
        }

        //positionAnimator.duration = duration.toLong()
        positionAnimator.interpolator = DecelerateInterpolator()
        positionAnimator.addUpdateListener { animation ->
            accentBoxHeight = animation.animatedValue as Float
            fillRectangle = RectF(borderWidth, accentBoxHeight + borderWidth, measuredWidth.toFloat() - borderWidth, measuredHeight.toFloat() - borderWidth)
            invalidate()
        }

        positionAnimator.start()

        return super.onTouchEvent(event)
    }

    fun startColorAnimation() {
        val colorAnimator = ValueAnimator.ofInt(0, 255)
        colorAnimator.interpolator = DecelerateInterpolator()
        colorAnimator.addUpdateListener { animation ->
            val animatorValue = (animation.animatedValue as Int)
            colorFilter = when (beatType) {
                BeatType.ACCENT -> {
                    val redColor = Color.rgb(255, animatorValue, animatorValue)
                    PorterDuffColorFilter(redColor, PorterDuff.Mode.MULTIPLY)
                }
                BeatType.SUBACCENT -> {
                    val greenColor = Color.rgb(animatorValue, 255, animatorValue)
                    PorterDuffColorFilter(greenColor, PorterDuff.Mode.MULTIPLY)
                }
                BeatType.BEAT -> {
                    val blueColor = Color.rgb(animatorValue, animatorValue, 255)
                    PorterDuffColorFilter(blueColor, PorterDuff.Mode.MULTIPLY)
                }
                else -> {
                    colorAnimator.setIntValues(122, 255)
                    val grayColor = Color.rgb(animatorValue, animatorValue, animatorValue)
                    PorterDuffColorFilter(grayColor, PorterDuff.Mode.MULTIPLY)
                }
            }
            background.colorFilter = colorFilter
        }

        colorAnimator.start()
    }

    override fun onDraw(canvas: Canvas?) {
        greyPaint.colorFilter = colorFilter
        whitePaint.colorFilter = colorFilter
        //todo всё ломается при изменении количества ударов в такте
        //canvas?.drawRoundRect(RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat()), Utils.getPixelsFromDp(5).toFloat(), Utils.getPixelsFromDp(5).toFloat(), whitePaint)
        canvas?.drawRoundRect(fillRectangle, Utils.getPixelsFromDp(5).toFloat(), Utils.getPixelsFromDp(5).toFloat(), greyPaint)
        /*if(bordersImage != null) {
            canvas?.drawBitmap(bordersImage, 0f, 0f, Paint())
        }*/
        //canvas?.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), greyPaint)

        super.onDraw(canvas)
    }
}