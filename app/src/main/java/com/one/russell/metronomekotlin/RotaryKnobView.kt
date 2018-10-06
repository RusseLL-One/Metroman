package com.one.russell.metronomekotlin

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.*
import android.media.AudioManager
import android.media.SoundPool
import android.support.v4.app.FragmentActivity
import android.view.MotionEvent
import android.view.View
import kotlin.properties.Delegates
import android.util.AttributeSet

const val BPM_STEP_DEGREES = 10

open class RotaryKnobView @JvmOverloads constructor(
        activity: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(activity, attrs, defStyleAttr) {


    var bpm = 10
    var knobImage: Bitmap? = null
    var startDegrees = 0f
    var deltaDegrees = 0f
    var degrees = 0f
    var rotateMatrix = Matrix()
    private var rotateClickPlayer: SoundPool
    private var rotateClickId: Int = 0
    private var model: MainViewModel by Delegates.notNull()
    val paint: Paint
    private var isBlocked = false

    init {
        rotateMatrix = Matrix()

        setKnobImage(activity)
        setBackgroundResource(R.drawable.knob)

        model = ViewModelProviders.of(activity as FragmentActivity).get(MainViewModel::class.java)

        rotateClickPlayer = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        rotateClickId = rotateClickPlayer.load(activity, R.raw.rotate_click, 1)

        model.bpmLiveData.observe(activity, Observer {
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

    fun setKnobImage(context: Context) {
        this.post {
            val drawable = context.resources.getDrawable(R.drawable.knob_dot)

            knobImage = Bitmap.createBitmap(this.measuredWidth, this.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(knobImage)
            drawable.setBounds(0, 0, this.measuredWidth, this.measuredHeight)
            drawable.draw(canvas)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //Делим ширину и высоту пополам, чтобы вращать вью вокруг середины
        rotateMatrix.setRotate(degrees, (this.width / 2).toFloat(), (this.height / 2).toFloat())

        if (knobImage != null) {
            canvas?.drawBitmap(knobImage, rotateMatrix, paint)
        }
    }

    fun block(block: Boolean) {
        isBlocked = block
    }

    private fun cartesianToPolar(x: Float, y: Float): Float {
        return 180 + (-Math.toDegrees(Math.atan2((x - 0.5f).toDouble(), (y - 0.5f).toDouble()))).toFloat()
    }
}
