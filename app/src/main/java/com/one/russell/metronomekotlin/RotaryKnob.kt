package com.one.russell.metronomekotlin

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.AudioManager
import android.media.SoundPool
import android.support.v4.app.FragmentActivity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlin.properties.Delegates

class RotaryKnob (activity: FragmentActivity) {
    private val MIN_BPM = 10
    private val MAX_BPM = 500
    var knobImageView: ImageView
    var rotateMatrix: Matrix? = null
    private var bpmTextView: TextView
    private var rotateClickPlayer: SoundPool
    private var rotateClickId: Int = 0
    private var model: MainViewModel by Delegates.notNull()

    init {
        this.knobImageView = activity.findViewById(R.id.knobImageView)
        this.bpmTextView = activity.findViewById(R.id.bpmTextView)
        rotateMatrix = Matrix()

        setKnobImage(activity)

        model = ViewModelProviders.of(activity).get(MainViewModel::class.java)

        rotateClickPlayer = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        rotateClickId = rotateClickPlayer.load(activity, R.raw.rotate_click, 1)
    }

    private val knobRotateListener = object : View.OnTouchListener {
        var startDegrees: Float = 0.toFloat()
        var currentDegrees: Float = 0.toFloat()

        override fun onTouch(v: View, event: MotionEvent): Boolean {

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val startX = event.x / knobImageView.width.toFloat()
                    val startY = event.y / knobImageView.height.toFloat()
                    currentDegrees = cartesianToPolar(startX, startY)
                    //Вычисляем начальные градусы с учётом предыдущего поворота
                    startDegrees = currentDegrees - startDegrees
                }
                MotionEvent.ACTION_MOVE -> {
                    val newDegrees: Float
                    val x = event.x / knobImageView.width.toFloat()
                    val y = event.y / knobImageView.height.toFloat()
                    newDegrees = cartesianToPolar(x, y)
                    //bpm увеличивается каждые 10 градусов. 10 градусов - это наш шаг
                    var step = (newDegrees / 10).toInt() - (currentDegrees / 10).toInt()
                    if (Math.abs(step) >= 1) {
                        //Если шаг слишком большой (например, при переходе от 0 к 360), то уменьшаем его
                        if (Math.abs(step) >= 30) step = -((if (step > 0) 1 else -1) * 36 - step)

                        if (model.bpm + step in MIN_BPM..MAX_BPM) {
                            model.bpm += step
                        }

                        rotateClickPlayer.play(rotateClickId, 0.75f, 0.75f, 0, 0, 1f)
                    }
                    bpmTextView.text = "BPM:\n" + model.bpm
                    currentDegrees = newDegrees
                    turn(currentDegrees - startDegrees)
                }
                MotionEvent.ACTION_UP -> {
                    startDegrees = currentDegrees - startDegrees
                    //Теперь startDegrees содержит градусы, на которые повёрнута ручка
                    v.performClick()
                }
            }
            return true
        }
    }

    private fun setKnobImage(context: Context) {
        knobImageView.post {
            val knobImage = BitmapFactory.decodeResource(context.resources, R.drawable.red_knob)
            val imageSizeMatrix = Matrix()
            val width = knobImageView.measuredWidth.toFloat() / knobImage.width.toFloat()
            val height = knobImageView.measuredHeight.toFloat() / knobImage.height.toFloat()
            imageSizeMatrix.preScale(width, height)
            knobImageView.setImageBitmap(Bitmap.createBitmap(knobImage, 0, 0,
                    knobImage.width, knobImage.height, imageSizeMatrix, true))
            knobImageView.scaleType = ImageView.ScaleType.MATRIX
            knobImageView.imageMatrix = rotateMatrix
            knobImageView.setOnTouchListener(knobRotateListener)
        }
    }

    private fun turn(degrees: Float) {
        rotateMatrix!!.setRotate(degrees, (knobImageView.width / 2).toFloat(), (knobImageView.height / 2).toFloat())
        knobImageView.imageMatrix = rotateMatrix
    }

    private fun cartesianToPolar(x: Float, y: Float): Float {
        return 180 + (-Math.toDegrees(Math.atan2((x - 0.5f).toDouble(), (y - 0.5f).toDouble()))).toFloat()
    }
}
