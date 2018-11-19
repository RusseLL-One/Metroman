package com.one.russell.metroman

import android.app.Activity
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
import android.content.res.Configuration
import android.os.Build
import android.support.v4.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.one.russell.metroman.fragments.SettingsFragment
import com.one.russell.metroman.fragments.TrainingFragment
import com.one.russell.metroman.views.BeatsContainerView
import com.shawnlin.numberpicker.NumberPicker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds


class MainActivity : AppCompatActivity() {
    private lateinit var sConn: ServiceConnection
    internal var tickService: TickService? = null
    private lateinit var tickServiceIntent: Intent
    internal var isBound = false
    private var beatPerBarDisposable: Disposable? = null
    private var mInterstitialAd: InterstitialAd? = null

    private var model: MainViewModel by Delegates.notNull()

    private var tickListener = object : TickListener {
        override fun onTick(beat: Int, duration: Int) {
        vLine.post {
            vLine.animateBall(duration)
            vLine.animateBorder()

            (llBeats as BeatsContainerView).animateBeat(beat)
        }
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

        override fun onStopClicking(isAutostop: Boolean) {
            Glide.with(this@MainActivity)
                    .load(R.drawable.play)
                    .into(playButton)
            if(!isAutostop) {
                model.adShowAttempts++
                if (mInterstitialAd?.isLoaded == true && model.adShowAttempts > 2) {
                    mInterstitialAd?.show()
                    model.adShowAttempts = 0
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.")
                }
            }
        }

        override fun onTrainingToggle(text: String?, isGoing: Boolean) {
            pbTrainingTime.post {
                if (isGoing) {
                    tvTrainingTitle.text = text
                    tvTrainingTitle.visibility = View.VISIBLE
                    pbTrainingTime.visibility = View.VISIBLE
                    pbTrainingTime.setProgress(0)
                } else {
                    tvTrainingTitle.visibility = View.GONE
                    pbTrainingTime.visibility = View.GONE
                }
            }
        }

        override fun onTrainingUpdate(percent: Float) {
            pbTrainingTime.post {
                pbTrainingTime.setProgress((percent * 100).toInt())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this, "ca-app-pub-4449968809046813~5053592036")

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        mInterstitialAd = InterstitialAd(this)
        //mInterstitialAd?.adUnitId = "ca-app-pub-3940256099942544/1033173712" //testkey
        mInterstitialAd?.adUnitId = "ca-app-pub-4449968809046813/7722652240"
        mInterstitialAd?.loadAd(AdRequest.Builder().build())

        pbTrainingTime.setProgressWidth(Utils.getPixelsFromDp(10))

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Glide.with(this)
                    .load(R.drawable.background_land)
                    .into(background)
        } else {
            Glide.with(this)
                    .load(R.drawable.background)
                    .into(background)
        }

        Glide.with(this)
                .load(R.drawable.tap)
                .into(tapButton)

        Glide.with(this@MainActivity)
                .load(R.drawable.pause)
                .into(playButton)

        Glide.with(this)
                .load(R.drawable.play)
                .into(playButton)

        model = ViewModelProviders.of(this).get(MainViewModel::class.java)
        model.initPrefs(this)

        background.setOnClickListener {
            etBpm.clearFocus()
        }

        (etBpm as EditText).setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
                return@OnEditorActionListener true
            }
            false
        })

        (etBpm as EditText).setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val manager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(view.windowToken, 0)
                try {
                    if ((etBpm as EditText).text.toString().toInt() < MIN_BPM) (etBpm as EditText).setText(MIN_BPM.toString())
                    if ((etBpm as EditText).text.toString().toInt() > MAX_BPM) (etBpm as EditText).setText(MAX_BPM.toString())
                    model.bpmLiveData.postValue((etBpm as EditText).text.toString().toInt())
                } catch (e : NumberFormatException) {
                    (etBpm as EditText).setText((model.bpmLiveData.value ?: 100).toString())
                }
            }
        }

        llBeats.setBeatViewListFromString(model.beatsValues)

        (npBeatsPerBar as NumberPicker).maxValue = MAX_BEATS_PER_BAR
        (npBeatsPerBar as NumberPicker).minValue = 1
        (npBeatsPerBar as NumberPicker).wrapSelectorWheel = false
        (npBeatsPerBar as NumberPicker).typeface = ResourcesCompat.getFont(this, R.font.xolonium_regular)

        (npValueOfBeat as NumberPicker).maxValue = MAX_VALUES_OF_BEAT
        (npValueOfBeat as NumberPicker).minValue = 1
        (npValueOfBeat as NumberPicker).displayedValues = arrayOf("1", "2", "4", "8", "16", "32", "64")
        (npValueOfBeat as NumberPicker).wrapSelectorWheel = false
        (npValueOfBeat as NumberPicker).value = model.valueOfBeatsLiveData.value ?: 3
        (npValueOfBeat as NumberPicker).typeface = ResourcesCompat.getFont(this, R.font.xolonium_regular)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pbTrainingTime.setProgressColor(resources.getColor(R.color.colorAccent, theme))
            pbTrainingTime.setTextColor(resources.getColor(R.color.colorAccent, theme))
        } else {
            @Suppress("DEPRECATION")
            pbTrainingTime.setProgressColor(resources.getColor(R.color.colorAccent))
            @Suppress("DEPRECATION")
            pbTrainingTime.setTextColor(resources.getColor(R.color.colorAccent))
        }

        val beatsPerBarSubject = PublishSubject.create<Int>()

        beatPerBarDisposable = beatsPerBarSubject.debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { beatsPerBar ->
                    model.beatsPerBarLiveData.postValue(beatsPerBar)
                }

        tickServiceIntent = Intent(this, TickService::class.java)

        sConn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                tickService = (binder as TickService.ServiceBinder).service

                tickService?.setTickListener(tickListener)
                tickService?.setBeatsSequence((llBeats as BeatsContainerView).getBeatTypeList())
                tickService?.setSoundPreset(model.soundPresetLiveData.value)
                tickService?.setBpm(model.bpmLiveData.value)
                tickService?.setFlasherEnabled(model.flasherValueLiveData.value)
                tickService?.setVibrateEnabled(model.vibrateValueLiveData.value)
                if (tickService?.isPlaying == true) {
                    tickListener.onStartClicking()
                }
                if (tickService?.isTrainingGoing == true) {
                    tickListener.onTrainingToggle(tickService?.trainingMessage ?: "", tickService?.isTrainingGoing ?: false)
                    tickListener.onTrainingUpdate(tickService?.completionPercentage ?: 0f)
                }
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                isBound = false
            }
        }

        model.bpmLiveData.observe(this, Observer {
            (etBpm as EditText).setText(it.toString())
            (etBpm as EditText).setSelection((etBpm as EditText).text.length)
            if (it != null) {
                tickService?.setBpm(it)
            }
        })

        model.soundPresetLiveData.observe(this, Observer {
            tickService?.setSoundPreset(it)
        })

        model.trainingLiveData.observe(this, Observer {
            if (it != null) {
                tickService?.startTraining(it)
            }
        })

        model.flasherValueLiveData.observe(this, Observer {
            tickService?.setFlasherEnabled(it)
        })

        model.vibrateValueLiveData.observe(this, Observer {
            tickService?.setVibrateEnabled(it)
        })

        model.beatsPerBarLiveData.observe(this, Observer {
            if (it != null) {
                (npBeatsPerBar as NumberPicker).value = it
                (llBeats as BeatsContainerView).setBeatsPerBar(it)
                tickService?.setBeatsSequence((llBeats as BeatsContainerView).getBeatTypeList())
            }
        })

        playButton.setOnClickListener {
            val service = tickService
            if (service != null) {
                if (service.isPlaying) {
                    service.stop(false)
                } else {
                    service.play()
                }
            }
        }

        llBeats.setOnViewChangedListener {
            tickService?.setBeatsSequence((llBeats as BeatsContainerView).getBeatTypeList())
        }

        (npBeatsPerBar as NumberPicker).setOnValueChangedListener { _, _, newValue ->
            beatsPerBarSubject.onNext(newValue)
        }

        (npValueOfBeat as NumberPicker).setOnValueChangedListener { _, _, value ->
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

            override fun onClick(p0: View?) {
                native_tap_click()

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
        model.beatsValues = llBeats.getBeatTypeListAsString()
        model.saveToPrefs()
        if (!isBound) return
        unbindService(sConn)
        isBound = false
    }

    override fun onDestroy() {
        super.onDestroy()
        beatPerBarDisposable?.dispose()
    }

    interface TickListener {
        fun onTick(beat: Int, duration: Int)
        fun onBpmChange(bpm: Int)
        fun onControlsBlock(block: Boolean)
        fun onStartClicking()
        fun onStopClicking(isAutostop: Boolean)
        fun onTrainingToggle(text: String?, isGoing: Boolean)
        fun onTrainingUpdate(percent: Float)
    }

    private external fun native_tap_click()
}
