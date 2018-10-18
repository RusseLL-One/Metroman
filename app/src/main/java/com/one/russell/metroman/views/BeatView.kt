package com.one.russell.metroman.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.one.russell.metroman.BeatType
import com.one.russell.metroman.R


class BeatView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr), View.OnClickListener {

    private val paint = Paint()
    var fillBoxHeightMultiplier = 0f
    var beatType = BeatType.BEAT
    var borderWidth = 0f
    private var sideBorder = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var bottomBorder = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var beatTypeImageOff = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var beatTypeImageMid = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var beatTypeImageOn = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var beatTypeImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    val rectLeftSrc = Rect()
    val rectLeft = RectF()
    val rectRightSrc = Rect()
    val rectRight = RectF()
    val rectBottomSrc = Rect()
    val rectBottom = RectF()
    val rectTopSrc = Rect()
    val rectTop = RectF()
    val rectFillSrc = Rect()
    val rectFill = RectF()

    constructor(context: Context, beatType: BeatType) : this(context) {
        this.beatType = beatType
    }

    init {

        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                fillBoxHeightMultiplier = when (beatType) {
                    BeatType.ACCENT -> 0f
                    BeatType.SUBACCENT -> 0.33f
                    BeatType.BEAT -> 0.67f
                    else -> 1f
                }

                try {
                    Glide.with(getContext())
                            .asBitmap()
                            .load(R.drawable.buttonside)
                            .into(object : SimpleTarget<Bitmap>(1, measuredHeight) {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    sideBorder = resource
                                    rectLeftSrc.set(sideBorder.width, 0, 0, sideBorder.height)
                                    rectLeft.set(0f, 0f, sideBorder.width.toFloat(), height.toFloat())

                                    rectRightSrc.set(0, 0, sideBorder.width, sideBorder.height)
                                    rectRight.set((width - sideBorder.width).toFloat(), 0f, width.toFloat(), height.toFloat())

                                    borderWidth = sideBorder.width.toFloat() / 3

                                    Glide.with(getContext())
                                            .asBitmap()
                                            .load(R.drawable.buttonsbottom)
                                            .into(object : SimpleTarget<Bitmap>(1, sideBorder.width) {
                                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                    bottomBorder = resource

                                                    rectBottomSrc.set(0, 0, bottomBorder.width, bottomBorder.height)
                                                    rectBottom.set(sideBorder.width.toFloat(), (height - bottomBorder.height).toFloat(), (width - sideBorder.width).toFloat(), height.toFloat())

                                                    rectTopSrc.set(0, bottomBorder.height, bottomBorder.width, 0)
                                                    rectTop.set(sideBorder.width.toFloat(), 0f, (width - sideBorder.width).toFloat(), bottomBorder.height.toFloat())

                                                    Glide.with(getContext())
                                                            .asBitmap()
                                                            .load(R.drawable.beatvalue_off)
                                                            .into(object : SimpleTarget<Bitmap>(1, measuredHeight - bottomBorder.height * 2) {
                                                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                                    beatTypeImageOff = resource
                                                                    beatTypeImage = beatTypeImageOff

                                                                    rectFillSrc.set(0, (beatTypeImage.height * fillBoxHeightMultiplier).toInt(), beatTypeImage.width, beatTypeImage.height)
                                                                    rectFill.set(borderWidth, (height - 2 * borderWidth) * fillBoxHeightMultiplier + borderWidth, (width - sideBorder.width).toFloat(), height - borderWidth)
                                                                    invalidate()
                                                                }
                                                            })

                                                    Glide.with(getContext())
                                                            .asBitmap()
                                                            .load(R.drawable.beatvalue_mid)
                                                            .into(object : SimpleTarget<Bitmap>(1, measuredHeight - bottomBorder.height * 2) {
                                                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                                    beatTypeImageMid = resource
                                                                }
                                                            })

                                                    Glide.with(getContext())
                                                            .asBitmap()
                                                            .load(R.drawable.beatvalue_on)
                                                            .into(object : SimpleTarget<Bitmap>(1, measuredHeight - bottomBorder.height * 2) {
                                                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                                    beatTypeImageOn = resource
                                                                }
                                                            })
                                                }
                                            })
                                }
                            })

                    viewTreeObserver.removeOnPreDrawListener(this)
                } catch (e: Exception) {
                }

                return true
            }
        })
        setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        beatType = when (beatType) {
            BeatType.BEAT -> BeatType.SUBACCENT
            BeatType.SUBACCENT -> BeatType.ACCENT
            BeatType.ACCENT -> BeatType.MUTE
            BeatType.MUTE -> BeatType.BEAT
        }

        val positionAnimator = when (beatType) {
            BeatType.ACCENT -> ValueAnimator.ofFloat(0.33f, 0f)
            BeatType.SUBACCENT -> ValueAnimator.ofFloat(0.67f, 0.33f)
            BeatType.BEAT -> ValueAnimator.ofFloat(1f, 0.67f)
            else -> ValueAnimator.ofFloat(0f, 1f)
        }

        positionAnimator.interpolator = DecelerateInterpolator()
        positionAnimator.addUpdateListener { animation ->
            fillBoxHeightMultiplier = animation.animatedValue as Float
            invalidate()
        }

        positionAnimator.start()
    }

    fun startColorAnimation() {
        val colorAnimator = ValueAnimator.ofInt(0, 3)
        colorAnimator.interpolator = DecelerateInterpolator()
        colorAnimator.addUpdateListener { animation ->
            val animatorValue = (animation.animatedValue as Int)
            when (animatorValue) {
                0 -> beatTypeImage = beatTypeImageOn
                1 -> beatTypeImage = beatTypeImageMid
                2 -> beatTypeImage = beatTypeImageOff
            }
            invalidate()
        }

        colorAnimator.start()
    }

    override fun onDraw(canvas: Canvas?) {
        rectRight.set((width - sideBorder.width).toFloat(), 0f, width.toFloat(), height.toFloat())
        rectBottom.set(sideBorder.width.toFloat(), (height - bottomBorder.height).toFloat(), (width - sideBorder.width).toFloat(), height.toFloat())
        rectTop.set(sideBorder.width.toFloat(), 0f, (width - sideBorder.width).toFloat(), bottomBorder.height.toFloat())

        rectFillSrc.set(0, (beatTypeImage.height * fillBoxHeightMultiplier).toInt(), beatTypeImage.width, beatTypeImage.height)
        rectFill.set(borderWidth, (height - 2 * borderWidth) * fillBoxHeightMultiplier + borderWidth, width - borderWidth, height - borderWidth)

        canvas?.drawBitmap(beatTypeImage, rectFillSrc, rectFill, paint)

        canvas?.drawBitmap(bottomBorder, rectBottomSrc, rectBottom, paint)
        canvas?.drawBitmap(bottomBorder, rectTopSrc, rectTop, paint)
        canvas?.drawBitmap(sideBorder, rectLeftSrc, rectLeft, paint)
        canvas?.drawBitmap(sideBorder, rectRightSrc, rectRight, paint)

        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun toString(): String {
        return beatType.ordinal.toString()
    }
}