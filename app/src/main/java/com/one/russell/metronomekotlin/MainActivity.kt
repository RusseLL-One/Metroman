package com.one.russell.metronomekotlin

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

class MainActivity : AppCompatActivity() {
    internal lateinit var knob: RotaryKnob
    internal lateinit var sConn: ServiceConnection
    internal lateinit var tickService: TickService
    internal lateinit var clickPlayer: ClickPlayer
    internal lateinit var t: Thread
    internal var isBound = false

    private var model: MainViewModel by Delegates.notNull()

    private var tickListener = object : TickListener {
        override fun onTick(isEmphasis: Boolean) {


        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = ViewModelProviders.of(this).get(MainViewModel::class.java)
        model.initPrefs(this)

        bpmTextView.text = "BPM:\n" + model.bpm

        knob = RotaryKnob(this)
        clickPlayer = ClickPlayer(this)

        sConn = object : ServiceConnection {

            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                tickService = (binder as TickService.ServiceBinder).service
                Log.d("qwe", "onServiceConnected, thread:" + Thread.currentThread().name)

                tickService.setTickListener(tickListener)
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Log.d("qwe", "MainActivity onServiceDisconnected")
                isBound = false
            }
        }

        playButton.setOnClickListener {
            if(!clickPlayer.isPlaying){
            //if (clickPlayerTask.status != AsyncTask.Status.RUNNING) {
            //    clickPlayerTask = ClickPlayerTask(this)
                t = Thread(clickPlayer)
                Log.d("asd", "play")

                playButton.setText(R.string.buttonStop)
                //clickPlayerTask.execute()
                t.start()
                //clickPlayer.run()
            } else {
                playButton.setText(R.string.buttonPlay)
                Log.d("asd", "stop")

                //clickPlayerTask.stop()
                //clickPlayer.stop()
                //t.interrupt()
            }
            clickPlayer.isPlaying = !clickPlayer.isPlaying
        }

        ivIncBeatsPerBar.setOnClickListener {
            setBeatsPerBar(true)
        }

        ivDecBeatsPerBar.setOnClickListener {
            setBeatsPerBar(false)
        }

        ivIncValueOfBeat.setOnClickListener {
            setValueOfBeat(true)
        }

        ivDecValueOfBeat.setOnClickListener {
            setValueOfBeat(false)
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
                    model.bpm = bpm
                    bpmTextView.text = "BPM:\n$bpm"
                }
                prevTouchTime = System.currentTimeMillis()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        model.saveToPrefs()
    }

    interface TickListener {
        fun onTick(isEmphasis: Boolean)
    }

    private fun setBeatsPerBar(isIncreasing: Boolean) {
        try {
            var beatsPerBar = tvBeatsPerBar.text.toString().toInt()
            if(isIncreasing) beatsPerBar++
            else beatsPerBar--

            if(beatsPerBar in 1..16) {
                tvBeatsPerBar.text = beatsPerBar.toString()

                //clickPlayerTask.setBeatSize(beatsPerBar)
                clickPlayer.setBeatSize(beatsPerBar)
            }
        } catch (e: NumberFormatException) {
        }
    }

    private fun setValueOfBeat(isIncreasing: Boolean) {
        try {
            var beatsPerBar = tvValueOfBeat.text.toString().toInt()
            if(isIncreasing) beatsPerBar *= 2
            else beatsPerBar /= 2

            if(beatsPerBar in 1..64) {
                tvValueOfBeat.text = beatsPerBar.toString()

                //todo смена длительности ноты
            }
        } catch (e: NumberFormatException) {
        }
    }
}
