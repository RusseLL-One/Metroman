package com.one.russell.metronomekotlin

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.*
import android.media.AudioManager
import android.media.SoundPool
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.properties.Delegates

class ScaleView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var degrees = 0f
    var bpm = 10
    var scaleImage: Bitmap? = null
    var arrowImage: Bitmap? = null
    val paint: Paint
    private var model: MainViewModel by Delegates.notNull()
    var rotateMatrix = Matrix()
    private var rotateClickPlayer: SoundPool
    private var rotateClickId: Int = 0

    init {
        degrees = -90f
        rotateMatrix = Matrix()

        setKnobImage(context)

        model = ViewModelProviders.of(context as FragmentActivity).get(MainViewModel::class.java)

        rotateClickPlayer = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        rotateClickId = rotateClickPlayer.load(context, R.raw.rotate_click, 1)

        model.bpmLiveData.observe(context, Observer {
            if(it != null) {
                bpm = it
            }
        })

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val startX = event.x / this.width.toFloat()
                val startY = event.y / this.height.toFloat()
                val newDegrees = cartesianToPolar(startX, startY)
                if(newDegrees in -70..70) {
                    degrees = newDegrees
                    bpm = (((degrees + 70) / 140) * 500).toInt()
                    model.setBpmLiveData(bpm)
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.x / this.width.toFloat()
                val y = event.y / this.height.toFloat()

                degrees = cartesianToPolar(x, y)
                if(degrees < -70) degrees = -70f
                if(degrees > 70) degrees = 70f
                bpm = (((degrees + 70) / 140) * (MAX_BPM - MIN_BPM) + MIN_BPM).toInt()
                model.setBpmLiveData(bpm)
                invalidate()
            }
        }
        return true
    }

    fun setKnobImage(context: Context) {
        this.post {
            val scaleDrawable = context.resources.getDrawable(R.drawable.ic_arc)
            scaleImage = Bitmap.createBitmap(this.measuredWidth, this.measuredHeight, Bitmap.Config.ARGB_8888)
            val canv1 = Canvas(scaleImage)
            scaleDrawable.setBounds(0, 0, this.measuredWidth, this.measuredHeight)
            scaleDrawable.draw(canv1)

            val arrowDrawable = context.resources.getDrawable(R.drawable.ic_scale_arrow)
            arrowImage = Bitmap.createBitmap(this.measuredWidth, this.measuredHeight, Bitmap.Config.ARGB_8888)
            val canv2 = Canvas(arrowImage)
            arrowDrawable.setBounds(0, 0, this.measuredWidth, this.measuredHeight)
            arrowDrawable.draw(canv2)
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        rotateMatrix.setRotate(degrees, (this.width / 2).toFloat(), (this.height / 2).toFloat())
        canvas?.drawBitmap(arrowImage, rotateMatrix, paint)
        canvas?.drawBitmap(scaleImage, 0f, 0f, paint)
    }

    private fun cartesianToPolar(x: Float, y: Float): Float {
        return  (-Math.toDegrees(Math.atan2((-x + 0.5f).toDouble(), (-y + 0.5f).toDouble()))).toFloat()
    }

}