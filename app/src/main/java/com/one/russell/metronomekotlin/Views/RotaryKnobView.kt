package com.one.russell.metronomekotlin.Views

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.*
import android.media.AudioManager
import android.media.SoundPool
import android.support.v4.app.FragmentActivity
import android.view.MotionEvent
import kotlin.properties.Delegates
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.one.russell.metronomekotlin.*
import kotlin.math.cos
import kotlin.math.sin

const val BPM_STEP_DEGREES = 10

class RotaryKnobView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    var bpm = 10
    var dotImage: Bitmap = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)
    var startDegrees = 270f
    var deltaDegrees = 0f
    var degrees = 270f
    var rotateMatrix = Matrix()
    private var rotateClickPlayer: SoundPool
    private var rotateClickId: Int = 0
    private var model: MainViewModel by Delegates.notNull()
    val paint: Paint
    private var isBlocked = false
    private var dotDistance = 0

    init {
        rotateMatrix = Matrix()

        Glide.with(this)
                .load(R.drawable.knob)
                .into(this)

        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)

                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.dot_130)
                        .into(object : SimpleTarget<Bitmap>(Utils.getPixelsFromDp(15), Utils.getPixelsFromDp(15)) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                dotImage = resource
                                invalidate()
                            }
                        })

                dotDistance = measuredHeight/2 - Utils.getPixelsFromDp(25)
                return true
            }
        })

        model = ViewModelProviders.of(context as FragmentActivity).get(MainViewModel::class.java)

        rotateClickPlayer = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        rotateClickId = rotateClickPlayer.load(context, R.raw.rotate_click, 1)

        model.bpmLiveData.observe(context, Observer {
            if (it != null) {
                bpm = it
            }
        })

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

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

                        rotateClickPlayer.play(rotateClickId, 0.75f, 0.75f, 0, 0, 1f)
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

    private fun degreesToRadians(degrees: Float) : Double {
        return degrees * (Math.PI / 180)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val stopX = width.toDouble()/2 + cos(degreesToRadians(degrees)) * dotDistance - dotImage.width.toDouble()/2
        val stopY = height.toDouble()/2 + sin(degreesToRadians(degrees)) * dotDistance - dotImage.height.toDouble()/2
        canvas?.drawBitmap(dotImage, stopX.toFloat(), stopY.toFloat(), paint)
    }

    fun block(block: Boolean) {
        isBlocked = block
    }

    private fun cartesianToPolar(x: Float, y: Float): Float {
        return 180 + (-Math.toDegrees(Math.atan2((x - 0.5f).toDouble(), (y - 0.5f).toDouble()))).toFloat()
    }
}
