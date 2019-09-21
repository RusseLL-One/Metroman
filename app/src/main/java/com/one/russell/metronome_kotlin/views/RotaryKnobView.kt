package com.one.russell.metronome_kotlin.views

import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import androidx.fragment.app.FragmentActivity
import android.view.MotionEvent
import kotlin.properties.Delegates
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.one.russell.metronome_kotlin.*
import kotlin.math.cos
import kotlin.math.sin

const val BPM_STEP_DEGREES = 10

class RotaryKnobView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var bpm = 10
    var dotImage: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var startDegrees = 270f
    private var deltaDegrees = 0f
    private var degrees = 270f
    private var rotateMatrix = Matrix()
    private var model: MainViewModel by Delegates.notNull()
    private val paint: Paint
    private var isBlocked = false
    private var dotDistance = 0

    init {
        rotateMatrix = Matrix()

        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.dot_90)
                        .into(object : SimpleTarget<Bitmap>(Utils.getPixelsFromDp(10), Utils.getPixelsFromDp(10)) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                dotImage = resource
                                invalidate()
                            }
                        })
                dotDistance = (0.75f * measuredHeight / 2).toInt()

                try {
                    Glide.with(getContext())
                            .asBitmap()
                            .load(R.drawable.knob)
                            .into(object : SimpleTarget<Bitmap>(measuredWidth, measuredHeight) {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    background = BitmapDrawable(resources, resource)
                                }
                            })
                    viewTreeObserver.removeOnPreDrawListener(this)
                } catch (e: Exception) {
                }

                return true
            }
        })

        model = ViewModelProviders.of(context as FragmentActivity).get(MainViewModel::class.java)

        model.bpmLiveData.observe(context, Observer {
            if (it != null) {
                bpm = it
            }
        })

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isBlocked) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val startX = event.x / this.width.toFloat()
                    val startY = event.y / this.height.toFloat()
                    deltaDegrees = cartesianToPolar(startX, startY)
                    //Вычисляем начальные градусы с учётом предыдущего поворота
                    startDegrees = deltaDegrees - startDegrees
                }
                MotionEvent.ACTION_MOVE -> {
                    val newDegrees: Float
                    val x = event.x / this.width.toFloat()
                    val y = event.y / this.height.toFloat()
                    newDegrees = cartesianToPolar(x, y)
                    //bpm увеличивается каждые 10 градусов. 10 градусов - это наш шаг
                    var step = (newDegrees / BPM_STEP_DEGREES).toInt() - (deltaDegrees / BPM_STEP_DEGREES).toInt()
                    if (Math.abs(step) >= 1) {
                        //Если шаг слишком большой (например, при переходе от 0 к 360), то уменьшаем его
                        if (Math.abs(step) >= 30) step = -((if (step > 0) 1 else -1) * 36 - step)

                        if (bpm + step in MIN_BPM..MAX_BPM) {
                            bpm += step
                            model.setBpmLiveData(bpm)
                        }

                        native_rotate_click()
                    }
                    deltaDegrees = newDegrees
                    degrees = deltaDegrees - startDegrees
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    startDegrees = deltaDegrees - startDegrees
                    //Теперь startDegrees содержит градусы, на которые повёрнута ручка
                    performClick()
                }
            }
            return true
        }
        return false
    }

    private fun degreesToRadians(degrees: Float): Double {
        return degrees * (Math.PI / 180)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val stopX = width.toDouble() / 2 + cos(degreesToRadians(degrees)) * dotDistance - dotImage.width.toDouble() / 2
        val stopY = height.toDouble() / 2 + sin(degreesToRadians(degrees)) * dotDistance - dotImage.height.toDouble() / 2
        canvas?.drawBitmap(dotImage, stopX.toFloat(), stopY.toFloat(), paint)
    }

    fun block(block: Boolean) {
        isBlocked = block
    }

    private fun cartesianToPolar(x: Float, y: Float): Float {
        return 180 + (-Math.toDegrees(Math.atan2((x - 0.5f).toDouble(), (y - 0.5f).toDouble()))).toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        dotDistance = (0.75f * measuredHeight / 2).toInt()
    }

    private external fun native_rotate_click()
}
