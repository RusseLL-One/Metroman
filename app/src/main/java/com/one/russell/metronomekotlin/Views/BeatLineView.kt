package com.one.russell.metronomekotlin.Views

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
    private var beatBallImage: Bitmap
    private val topBorderMatrix = Matrix()
    private var border0: Bitmap
    private var border1: Bitmap
    private var border2: Bitmap
    private var border3: Bitmap
    private var border4: Bitmap
    private var topBorder: Bitmap
    private var bottomBorder: Bitmap
    private var emptyImage: Bitmap

    init {
        emptyImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        beatBallImage = emptyImage
        border0 = emptyImage
        border1 = emptyImage
        border2 = emptyImage
        border3 = emptyImage
        border4 = emptyImage
        topBorder = emptyImage
        bottomBorder = emptyImage

        topBorderMatrix.preScale(1f, -1f)

        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)

                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.borders0000)
                        .into(object : SimpleTarget<Bitmap>(measuredWidth, 1) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                border0 = resource
                            }
                        })
                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.borders0001)
                        .into(object : SimpleTarget<Bitmap>(measuredWidth, 1) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                border1 = resource
                            }
                        })
                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.borders0002)
                        .into(object : SimpleTarget<Bitmap>(measuredWidth, 1) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                border2 = resource
                            }
                        })
                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.borders0003)
                        .into(object : SimpleTarget<Bitmap>(measuredWidth, 1) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                border3 = resource
                            }
                        })
                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.borders0004)
                        .into(object : SimpleTarget<Bitmap>(measuredWidth, 1) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                border4 = resource
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

        val positionAnimator: ValueAnimator
        if (isBeatBallOnTop) {
            positionAnimator = ValueAnimator.ofInt(0, pathLength)
        } else {
            positionAnimator = ValueAnimator.ofInt(pathLength, 0)
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
        val borderAnimator = ValueAnimator.ofInt(0, 5)
        //borderAnimator.duration = 2000L
        borderAnimator.repeatCount = 0
        borderAnimator.interpolator = DecelerateInterpolator()
        borderAnimator.addUpdateListener { animation ->

            val x = animation.animatedValue as Int
            Log.d("qwe", "borderAnimator = " + x)
            when (x) {
                0 -> {
                    if (!isBeatBallOnTop) topBorder = border0 else bottomBorder = border0
                }
                1 -> {
                    if (!isBeatBallOnTop) topBorder = border1 else bottomBorder = border1
                }
                2 -> {
                    if (!isBeatBallOnTop) topBorder = border2 else bottomBorder = border2
                }
                3 -> {
                    if (!isBeatBallOnTop) topBorder = border3 else bottomBorder = border3
                }
                4 -> {
                    if (!isBeatBallOnTop) topBorder = border4 else bottomBorder = border4
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