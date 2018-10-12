package com.one.russell.metronomekotlin.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.one.russell.metronomekotlin.R


class BeatLineView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    val paint = Paint()
    private var ballPositionY = 0f
    private var isBeatBallOnTop = true
    private val topBorderMatrix = Matrix()
    private var emptyImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var beatBallImage = emptyImage
    private var borderOff = emptyImage
    private var borderMid = emptyImage
    private var borderOn = emptyImage
    private var topBorder = emptyImage
    private var bottomBorder = emptyImage

    init {
        topBorderMatrix.preScale(1f, -1f)

        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)

                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.borders_on)
                        .into(object : SimpleTarget<Bitmap>(measuredWidth, 1) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                borderOn = resource
                            }
                        })
                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.borders_mid)
                        .into(object : SimpleTarget<Bitmap>(measuredWidth, 1) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                borderMid = resource
                            }
                        })
                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.borders_off)
                        .into(object : SimpleTarget<Bitmap>(measuredWidth, 1) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                borderOff = resource
                                topBorder = resource
                                bottomBorder = resource
                                topBorderMatrix.postTranslate(0f, (resource.height).toFloat())
                                invalidate()
                            }
                        })
                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.beatball)
                        .into(object : SimpleTarget<Bitmap>(measuredWidth, measuredWidth) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                beatBallImage = resource
                                invalidate()
                            }
                        })
                return true
            }
        })
    }

    fun animateBall(duration: Int) {
        val pathLength = height - beatBallImage.height

        val positionAnimator = if (isBeatBallOnTop) {
            ValueAnimator.ofInt(0, pathLength)
        } else {
            ValueAnimator.ofInt(pathLength, 0)
        }

        isBeatBallOnTop = !isBeatBallOnTop
        positionAnimator.duration = duration.toLong()
        positionAnimator.interpolator = LinearInterpolator()
        positionAnimator.addUpdateListener { animation ->
            val x = animation.animatedValue as Int
            ballPositionY = x.toFloat()
            invalidate()
        }
        positionAnimator.start()
    }

    fun animateBorder() {
        val borderAnimator = ValueAnimator.ofInt(0, 3)
        //borderAnimator.duration = 2000L
        borderAnimator.repeatCount = 0
        borderAnimator.interpolator = DecelerateInterpolator()
        borderAnimator.addUpdateListener { animation ->

            val x = animation.animatedValue as Int
            Log.d("qwe", "borderAnimator = $x")
            when (x) {
                0 -> {
                    if (!isBeatBallOnTop) topBorder = borderOn else bottomBorder = borderOn
                }
                1 -> {
                    if (!isBeatBallOnTop) topBorder = borderMid else bottomBorder = borderMid
                }
                2 -> {
                    if (!isBeatBallOnTop) topBorder = borderOff else bottomBorder = borderOff
                }
            }
            invalidate()
        }
        borderAnimator.start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawBitmap(topBorder, topBorderMatrix, paint)
        canvas?.drawBitmap(bottomBorder, 0f, (measuredHeight - bottomBorder.height).toFloat(), paint)
        canvas?.drawBitmap(beatBallImage, 0f, ballPositionY, paint)
    }
}