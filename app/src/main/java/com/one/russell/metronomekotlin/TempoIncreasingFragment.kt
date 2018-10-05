package com.one.russell.metronomekotlin

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_training_tempo_increasing.*
import kotlinx.android.synthetic.main.item_settings_param.view.*
import kotlin.properties.Delegates

class TempoIncreasingFragment : Fragment() {

    private var model: MainViewModel by Delegates.notNull()
    private val clickListener = View.OnClickListener {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity
        if(activity != null) {
            model = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        }
        return inflater.inflate(R.layout.fragment_training_tempo_increasing, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        startValue.tvValue.text = "90"
        endValue.tvValue.text = "160"
        barsValue.tvValue.text = "1"
        increaseValue.tvValue.text = "5"

        startValue.ivIncrease.setOnClickListener {
            try {
                var startBpm = startValue.tvValue.text.toString().toInt()
                startBpm++

                val endBpm = endValue.tvValue.text.toString().toInt()
                if(startBpm <= endBpm) {
                    startValue.tvValue.text = startBpm.toString()
                }
            } catch (e: NumberFormatException) {
            }
        }

        startValue.ivDecrease.setOnClickListener {
            try {
                var startBpm = startValue.tvValue.text.toString().toInt()
                startBpm--

                if(startBpm >= MIN_BPM) {
                    startValue.tvValue.text = startBpm.toString()
                }
            } catch (e: NumberFormatException) {
            }
        }

        endValue.ivIncrease.setOnClickListener {
            try {
                var endBpm = endValue.tvValue.text.toString().toInt()
                endBpm++

                if(endBpm <= MAX_BPM) {
                    endValue.tvValue.text = endBpm.toString()
                }
            } catch (e: NumberFormatException) {
            }
        }

        endValue.ivDecrease.setOnClickListener {
            try {
                var endBpm = endValue.tvValue.text.toString().toInt()
                endBpm--

                var startBpm = startValue.tvValue.text.toString().toInt()
                if (endBpm >= startBpm) {
                    endValue.tvValue.text = endBpm.toString()
                }
            } catch (e: NumberFormatException) {
            }
        }

        barsValue.ivIncrease.setOnClickListener {
            try {
                var bars = barsValue.tvValue.text.toString().toInt()
                bars++

                if(bars <= 100) {
                    barsValue.tvValue.text = bars.toString()
                }
            } catch (e: NumberFormatException) {
            }
        }

        barsValue.ivDecrease.setOnClickListener {
            try {
                var bars = barsValue.tvValue.text.toString().toInt()
                bars--

                if (bars >= 1) {
                    barsValue.tvValue.text = bars.toString()
                }
            } catch (e: NumberFormatException) {
            }
        }

        increaseValue.ivIncrease.setOnClickListener {
            try {
                var increment = increaseValue.tvValue.text.toString().toInt()
                increment++

                if(increment <= 200) {
                    increaseValue.tvValue.text = increment.toString()
                }
            } catch (e: NumberFormatException) {
            }
        }

        increaseValue.ivDecrease.setOnClickListener {
            try {
                var increment = increaseValue.tvValue.text.toString().toInt()
                increment--

                if (increment >= 1) {
                    increaseValue.tvValue.text = increment.toString()
                }
            } catch (e: NumberFormatException) {
            }
        }

        btStart.setOnClickListener {
            try {
                val trainingType = TrainingType.TEMPO_INCREASING
                val startBpm = startValue.tvValue.text.toString().toInt()
                val endBpm = endValue.tvValue.text.toString().toInt()
                val bars = barsValue.tvValue.text.toString().toInt()
                val increment = increaseValue.tvValue.text.toString().toInt()

                val params = Bundle()
                params.putString("trainingType", trainingType.name)
                params.putInt("startBpm", startBpm)
                params.putInt("endBpm", endBpm)
                params.putInt("bars", bars)
                params.putInt("increment", increment)

                model.startTraining(params)
                activity?.supportFragmentManager?.popBackStack();
            } catch (e: NumberFormatException) {
            }
        }

        /*startValue.setOnTouchListener(object : View.OnTouchListener {
            var startX = 0f
            var handler = Handler()
            override fun onTouch(view: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val x = event.x

                        if(x - startX > 10) {
                            Log.d("qwe", "MOVE, x= " + x + " startX=" + startX)
                            //handler.postDelayed() {  }

                            clickDisposable = clickObservable.switchMap { interval ->
                                Observable.interval(interval, TimeUnit.MILLISECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnNext { click() }
                            }.subscribe()



                        }
                    }
                }
                return true
            }
        })*/
    }
}
