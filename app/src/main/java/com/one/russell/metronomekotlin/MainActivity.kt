package com.one.russell.metronomekotlin

import android.animation.ValueAnimator
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.FragmentTransaction
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.properties.Delegates
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout


class MainActivity : AppCompatActivity() {
    internal lateinit var sConn: ServiceConnection
    internal var tickService: TickService? = null
    internal lateinit var tickServiceIntent: Intent
    internal var isBound = false

    private var model: MainViewModel by Delegates.notNull()

    private var tickListener = object : TickListener {
        private var isBeatBallOnTop = true

        override fun onTick(isEmphasis: Boolean, duration: Int) {

            val pathLength = vLine.height - vBall.height

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
                (vBall.layoutParams as FrameLayout.LayoutParams).topMargin = x
                vBall.requestLayout()
            }

            /*val colorAnimator = ValueAnimator.ofInt(0, 255)
            colorAnimator.interpolator = DecelerateInterpolator()
            colorAnimator.addUpdateListener { animation ->

                val animatorValue = (animation.animatedValue as Int)
                val colorStr = Color.rgb(animatorValue, animatorValue, animatorValue)
                val blackFilter = PorterDuffColorFilter(colorStr, PorterDuff.Mode.MULTIPLY)
                vBall.background.setColorFilter(blackFilter)
            }*/

            Log.d("qwe", "onTick, thread:" + Thread.currentThread().name)

            positionAnimator.start()
            //colorAnimator.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = ViewModelProviders.of(this).get(MainViewModel::class.java)
        model.initPrefs(this)

        npBeatsPerBar.maxValue = 16
        npBeatsPerBar.minValue = 1
        npBeatsPerBar.wrapSelectorWheel = false
        npBeatsPerBar.value = model.beatsPerBar

        npValueOfBeat.maxValue = 7
        npValueOfBeat.minValue = 1
        npValueOfBeat.displayedValues = arrayOf("1", "2", "4", "8", "16", "32", "64")
        npValueOfBeat.wrapSelectorWheel = false
        npValueOfBeat.value = model.valueOfBeats

        model.bpmLiveData.observe(this, Observer {
            bpmTextView.text = "BPM\n$it"
            if(it != null) {
                tickService?.setBpm(it)
                //tickService?.bpm = it
            }
        })

        model.accentSoundLiveData.observe(this, Observer {
            tickService?.setAccentSound(it)
        })

        model.beatSoundLiveData.observe(this, Observer {
            tickService?.setBeatSound(it)
        })

        tickServiceIntent = Intent(this, TickService::class.java)

        sConn = object : ServiceConnection {

            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                tickService = (binder as TickService.ServiceBinder).service
                Log.d("qwe", "onServiceConnected, thread:" + Thread.currentThread().name)

                tickService?.setTickListener(tickListener)
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Log.d("qwe", "MainActivity onServiceDisconnected")
                isBound = false
            }
        }

        playButton.setOnClickListener {
            val service = tickService
            if (service != null) {
                if (service.isPlaying) {
                    service.stop()
                    playButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp)
                } else {
                    service.play()
                    playButton.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp)
                }
                /*if(!clickPlayer.isPlaying){
                t = Thread(clickPlayer)
                Log.d("asd", "play")

                t.start()
            } else {
                Log.d("asd", "stop")
            }
            clickPlayer.isPlaying = !clickPlayer.isPlaying*/
            }
        }

        npBeatsPerBar.setOnValueChangedListener { _, _, value ->
            tickService?.setBeatSize(value)
            model.beatsPerBar = value
        }

        npValueOfBeat.setOnValueChangedListener { _, _, value ->
            //todo смена длительности ноты
            model.valueOfBeats = value
        }

        bSettings.setOnClickListener {
            if (supportFragmentManager != null) {
                supportFragmentManager.
                    beginTransaction().
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
                        replace(R.id.frameForFragment, SettingsFragment()).
                        addToBackStack(null).
                        commit()
            }
        }

        tapButton.setOnClickListener(object : View.OnClickListener {
            var prevTouchTime = 0L
            var prevTouchInterval = 0L
            var isFirstClick = true

            override fun onClick(p0: View?) {
                if (prevTouchTime > 0) {
                    val interval = System.currentTimeMillis() - prevTouchTime

                    if (interval > 60000 / MAX_BPM) {
                        if (interval < 60000 / MIN_BPM) {
                            if (isFirstClick) {
                                prevTouchInterval = interval
                                isFirstClick = false
                            }
                            else {
                                prevTouchInterval = (prevTouchInterval + interval) / 2
                            }
                        } else {
                            //Если интервал нажатий слишком большой, сбрасываем последовательность
                            isFirstClick = true
                        }
                    } else {
                        //Если интервал нажатий слишком маленький, устанавливаем его минимальное значение
                        prevTouchInterval = (60000 / MAX_BPM).toLong()
                    }

                    val bpm = (60000 / prevTouchInterval).toInt()
                    model.setBpmLiveData(bpm)
                }
                prevTouchTime = System.currentTimeMillis()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        bindService(tickServiceIntent, sConn, 0)
        startService(tickServiceIntent)
    }

    override fun onStop() {
        super.onStop()
        model.saveToPrefs()
        if (!isBound) return
        unbindService(sConn)
        isBound = false
        stopService(tickServiceIntent)
    }

    interface TickListener {
        fun onTick(isEmphasis: Boolean, duration: Int)
    }
}
