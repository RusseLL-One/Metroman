package com.one.russell.metronomekotlin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.AudioManager
import android.media.SoundPool
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class RotaryKnob (context: Context) : View(context) {
    private val KNOB_WIDTH = 500f
    private val KNOB_HEIGTH = 500f
    private val MIN_BPM = 10
    private val MAX_BPM = 300
    var knobImageView: ImageView? = null
    var rotateMatrix: Matrix? = null
    private val debugTextView: TextView
    private lateinit var bpmTextView: TextView
    private lateinit var rotateClickPlayer: SoundPool
    private var rotateClickId: Int = 0
    var bpm = 10
    internal var clickPlayerTask: ClickPlayerTask? = null

    private val knobRotateListener = object : View.OnTouchListener {
        internal var startDegrees: Float = 0.toFloat()
        internal var currentDegrees: Float = 0.toFloat()


        override fun onTouch(v: View, event: MotionEvent): Boolean {

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val startX = event.x / knobImageView!!.width.toFloat()
                    val startY = event.y / knobImageView!!.height.toFloat()
                    currentDegrees = cartesianToPolar(startX, startY)
                    //Вычисляем начальные градусы с учётом предыдущего поворота
                    startDegrees = currentDegrees - startDegrees
                }
                MotionEvent.ACTION_MOVE -> {
                    val newDegrees: Float
                    val x = event.x / knobImageView!!.width.toFloat()
                    val y = event.y / knobImageView!!.height.toFloat()
                    newDegrees = cartesianToPolar(x, y)
                    //bpm увеличивается каждые 10 градусов. 10 градусов - это наш шаг
                    var step = (newDegrees / 10).toInt() - (currentDegrees / 10).toInt()
                    if (Math.abs(step) >= 1) {
                        //Если шаг слишком большой (например, при переходе от 0 к 360), то уменьшаем его
                        if (Math.abs(step) >= 30) step = -((if (step > 0) 1 else -1) * 36 - step)

                        if (bpm + step >= MIN_BPM && bpm + step <= MAX_BPM) {
                            bpm += step
                            if (clickPlayerTask != null) {
                                clickPlayerTask!!.setBPM(bpm)
                            }
                        }

                        rotateClickPlayer.play(rotateClickId, 0.75f, 0.75f, 0, 0, 1f)
                    }
                    bpmTextView.text = "BPM:\n$bpm"
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

    init {
        this.knobImageView = (context as AppCompatActivity).findViewById(R.id.knobImageView)
        this.bpmTextView = context.findViewById(R.id.bpmTextView)
        rotateMatrix = Matrix()

        setKnobImage()

        debugTextView = context.findViewById(R.id.debugTextView)

        rotateClickPlayer = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        rotateClickId = rotateClickPlayer.load(context, R.raw.rotate_click, 1)
    }

    private fun setKnobImage() {
        val knobImage = BitmapFactory.decodeResource(context.resources, R.drawable.knob)
        val imageSizeMatrix = Matrix()
        //TODO: Размер изображения задаётся в пикселях, нужно конвертировать в dp
        imageSizeMatrix.preScale(KNOB_WIDTH / knobImage.width, KNOB_HEIGTH / knobImage.height)
        knobImageView!!.setImageBitmap(Bitmap.createBitmap(knobImage, 0, 0,
                knobImage.width, knobImage.height, imageSizeMatrix, true))
        knobImageView!!.scaleType = ImageView.ScaleType.MATRIX
        knobImageView!!.imageMatrix = rotateMatrix
        knobImageView!!.setOnTouchListener(knobRotateListener)
    }

    private fun turn(degrees: Float) {
        rotateMatrix!!.setRotate(degrees, (knobImageView!!.width / 2).toFloat(), (knobImageView!!.height / 2).toFloat())
        knobImageView!!.imageMatrix = rotateMatrix
    }

    private fun cartesianToPolar(x: Float, y: Float): Float {
        return 180 + (-Math.toDegrees(Math.atan2((x - 0.5f).toDouble(), (y - 0.5f).toDouble()))).toFloat()
    }

    fun setPlayerTask(task: ClickPlayerTask) {
        this.clickPlayerTask = task
    }
}
