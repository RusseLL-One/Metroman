package com.one.russell.metronomekotlin

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    internal lateinit var knob: RotaryKnob
    internal lateinit var clickPlayerTask: ClickPlayerTask

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        knob = RotaryKnob(this)
        clickPlayerTask = ClickPlayerTask(this, knob.bpm)
        applicationContext

        playButton.setOnClickListener {
            if (clickPlayerTask.status != AsyncTask.Status.RUNNING) {
                clickPlayerTask = ClickPlayerTask(this, knob.bpm)
                playButton.setText(R.string.buttonStop)
                knob.setPlayerTask(clickPlayerTask)
                clickPlayerTask.initSound()
                clickPlayerTask.execute()
            } else {
                playButton.setText(R.string.buttonPlay)
                clickPlayerTask.stop()

            }
        }

        tapButton.setOnClickListener(
                object : View.OnClickListener {

                    //todo доделать
                    var startTime = 0L;
                    var tapCount = 0
                    var list = LongArray(10)
                    var sum = 0L

                    override fun onClick(p0: View?) {
                        val currTime = System.currentTimeMillis()
                        if (startTime == 0L) {
                            startTime = currTime - 1
                        }
                        val y = currTime - startTime

                        if (tapCount < 10) {
                            list[tapCount] = (y)
                            tapCount++

                            var str = ""
                            for(l in list) {
                                str += l.toString() + " "
                            }
                            Log.d("qwe", str)

                            sum = 0L
                            for (i in 0 until tapCount) {
                                sum += list[i]
                            }
                            sum /= tapCount

                        } else {
                            for(i in 0 until tapCount - 1) {
                                list[i] = list[i+1]
                            }
                            list[tapCount - 1] = y

                            var str = ""
                            for(l in list) {
                                str += l.toString() + " "
                            }
                            Log.d("qwe", str)

                            sum = 0L
                            for (i in 0 until tapCount) {
                                sum += list[i]
                            }
                            sum /= tapCount
                        }

                        val tempo = 60000 * tapCount / y;

                        bpmTextView.text = sum.toString()

                        /*val currTime = System.currentTimeMillis()
                        if (startTime == 0L) {
                            startTime = currTime - 1
                        }
                        val y = currTime - startTime

                        val tempo = 60000 * tapCount / y;

                        if (tapCount < 20) {
                            tapCount++
                        }
                        startTime = currTime

                        bpmTextView.text = tempo.toString()*/
                    }
                }
        )
    }
}
