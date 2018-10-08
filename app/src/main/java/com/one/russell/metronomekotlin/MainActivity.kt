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
import android.media.AudioManager
import android.media.SoundPool
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    internal lateinit var sConn: ServiceConnection
    internal var tickService: TickService? = null
    internal lateinit var tickServiceIntent: Intent
    internal var isBound = false
    internal val beatsViewList = ArrayList<BeatView>()

    private var model: MainViewModel by Delegates.notNull()

    private var tickListener = object : TickListener {
        private var isBeatBallOnTop = true

        override fun onTick(beatType: BeatType, beat: Int, duration: Int) {

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
            positionAnimator.start()


            val border = if(!isBeatBallOnTop) topBorder else bottomBorder
            val borderAnimator = ValueAnimator.ofInt(0, 5)
            borderAnimator.duration = 2000L
            borderAnimator.repeatCount = 0
            //borderAnimator.interpolator = DecelerateInterpolator()
            borderAnimator.addUpdateListener { animation ->

                val x = animation.animatedValue as Int
                Log.d("qwe", "borderAnimator = " + x)
                when (x) {
                    0 -> Glide.with(this@MainActivity)
                            .load(R.drawable.borders0000)
                            .into(border)
                    1 -> Glide.with(this@MainActivity)
                            .load(R.drawable.borders0001)
                            .into(border)
                    2 -> Glide.with(this@MainActivity)
                            .load(R.drawable.borders0002)
                            .into(border)
                    3 -> Glide.with(this@MainActivity)
                            .load(R.drawable.borders0003)
                            .into(border)
                    4 -> Glide.with(this@MainActivity)
                            .load(R.drawable.borders0004)
                            .into(border)
                }

            }
            borderAnimator.start()


            beatsViewList[beat].startColorAnimation()
        }

        override fun onBpmChange(bpm: Int) {
            model.bpmLiveData.postValue(bpm)
        }

        override fun onControlsBlock(block: Boolean) {
            rotaryKnob.block(block)
        }

        override fun onStartClicking() {
            Glide.with(this@MainActivity)
                    .load(R.drawable.pause)
                    .into(playButton)
        }

        override fun onStopClicking() {
            Glide.with(this@MainActivity)
                    .load(R.drawable.play)
                    .into(playButton)
        }
    }

    fun beatBallAnimation() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Glide.with(this)
                .load(R.drawable.background)
                .into(background)

        Glide.with(this)
                .load(R.drawable.tap)
                .into(tapButton)

        Glide.with(this)
                .load(R.drawable.play)
                .into(playButton)

        Glide.with(this)
                .load(R.drawable.knob)
                .into(rotaryKnob)

        Glide.with(this)
                .load(R.drawable.borders0004)
                .into(bottomBorder)

        Glide.with(this)
                .load(R.drawable.borders0004)
                .into(topBorder)

        model = ViewModelProviders.of(this).get(MainViewModel::class.java)
        model.initPrefs(this)

        npBeatsPerBar.maxValue = MAX_BEATS_PER_BAR
        npBeatsPerBar.minValue = 1
        npBeatsPerBar.wrapSelectorWheel = false

        npValueOfBeat.maxValue = MAX_VALUES_OF_BEAT
        npValueOfBeat.minValue = 1
        npValueOfBeat.displayedValues = arrayOf("1", "2", "4", "8", "16", "32", "64")
        npValueOfBeat.wrapSelectorWheel = false
        npValueOfBeat.value = model.valueOfBeatsLiveData.value ?: 3

        val beatsPerBarSubject = PublishSubject.create<Int>()

        beatsPerBarSubject.debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { beatsPerBar ->
                    model.beatsPerBarLiveData.postValue(beatsPerBar)
                }

        tickServiceIntent = Intent(this, TickService::class.java)

        sConn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                tickService = (binder as TickService.ServiceBinder).service
                Log.d("qwe", "onServiceConnected, thread:" + Thread.currentThread().name)

                tickService?.setTickListener(tickListener)
                tickService?.setBeatsSequence(beatsViewList)
                tickService?.setAccentSound(model.accentSoundLiveData.value)
                tickService?.setBeatSound(model.beatSoundLiveData.value)
                tickService?.setBpm(model.bpmLiveData.value)
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Log.d("qwe", "MainActivity onServiceDisconnected")
                isBound = false
            }
        }

        model.bpmLiveData.observe(this, Observer {
            bpmTextView.text = "BPM\n$it"
            if (it != null) {
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

        model.trainingLiveData.observe(this, Observer {
            if(it != null) {
                tickService?.startTraining(it)
            }
        })

        model.beatsPerBarLiveData.observe(this, Observer {
            if (it != null) {
                npBeatsPerBar.value = it
                val newItemsCount = it - beatsViewList.size
                if (newItemsCount > 0) {
                    for (i in beatsViewList.size until it) {
                        val view = if (i == 0) BeatView(this, BeatType.ACCENT)
                        else BeatView(this, BeatType.BEAT)
                        beatsViewList.add(view)
                        val params = LinearLayout.LayoutParams(
                                Utils.getPixelsFromDp(50),
                                Utils.getPixelsFromDp(50),
                                1f)
                        llBeats.addView(view, params)
                    }
                } else if (newItemsCount < 0) {
                    for (i in beatsViewList.size - 1 downTo it) {
                        beatsViewList.removeAt(i)
                        llBeats.removeViewAt(i)
                    }
                }
                val gap = (Utils.getPixelsFromDp(16) * (1 - beatsViewList.size.toFloat() / (MAX_BEATS_PER_BAR + 4))).toInt()
                for (index in beatsViewList.indices) {
                    (beatsViewList[index].layoutParams as LinearLayout.LayoutParams).leftMargin =
                            if (index == 0) 0 else gap
                }
                tickService?.setBeatsSequence(beatsViewList)
            }
        })

        playButton.setOnClickListener {
            val service = tickService
            if (service != null) {
                if (service.isPlaying) {
                    service.stop()
                } else {
                    service.play()
                }
            }
        }

        npBeatsPerBar.setOnValueChangedListener { _, _, newValue ->
            beatsPerBarSubject.onNext(newValue)
        }

        npValueOfBeat.setOnValueChangedListener { _, _, value ->
            //todo смена длительности ноты
            model.valueOfBeatsLiveData.postValue(value)
        }

        bSettings.setOnClickListener {
            if (supportFragmentManager != null) {
                supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.frameForFragment, SettingsFragment()).addToBackStack(null).commit()
            }
        }

        bTempoTrain.setOnClickListener {
            if (supportFragmentManager != null) {
                supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.frameForFragment, TrainingFragment()).addToBackStack(null).commit()
            }
        }

        tapButton.setOnClickListener(object : View.OnClickListener {
            var prevTouchTime = 0L
            var prevTouchInterval = 0L
            var isFirstClick = true
            val tapClickPlayer = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
            val tapClickId = tapClickPlayer.load(this@MainActivity, R.raw.rotate_click, 1)

            override fun onClick(p0: View?) {
                tapClickPlayer.play(tapClickId, 0.75f, 0.75f, 0, 0, 1f)

                if (prevTouchTime > 0) {
                    val interval = System.currentTimeMillis() - prevTouchTime

                    if (interval > 60000 / MAX_BPM) {
                        if (interval < 60000 / MIN_BPM) {
                            if (isFirstClick) {
                                prevTouchInterval = interval
                                isFirstClick = false
                            } else {
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
    }

    interface TickListener {
        fun onTick(beatType: BeatType, beat: Int, duration: Int)
        fun onBpmChange(bpm: Int)
        fun onControlsBlock(block: Boolean)
        fun onStartClicking()
        fun onStopClicking()
    }
}
