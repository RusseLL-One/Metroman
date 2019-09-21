package com.one.russell.metronome_kotlin

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.FragmentTransaction
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.properties.Delegates
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.one.russell.metronome_kotlin.fragments.SettingsFragment
import com.one.russell.metronome_kotlin.fragments.TrainingFragment
import com.one.russell.metronome_kotlin.views.BeatsContainerView
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.ToggleButton
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.one.russell.metronome_kotlin.fragments.BeatsizeSubfragment
import com.one.russell.metronome_kotlin.fragments.BookmarksSubfragment


class MainActivity : AppCompatActivity() {
    private lateinit var sConn: ServiceConnection
    internal var tickService: TickService? = null
    private lateinit var tickServiceIntent: Intent
    internal var isBound = false
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
        mInterstitialAd?.adUnitId = "ca-app-pub-3940256099942544/1033173712" //testkey
        //mInterstitialAd?.adUnitId = "ca-app-pub-4449968809046813/7722652240"
        mInterstitialAd?.loadAd(AdRequest.Builder().build())

        if(supportFragmentManager.findFragmentByTag("bookmarks_fragment") == null) {
            supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.bookmarksFragment, BookmarksSubfragment(), "bookmarks_fragment").commit()
        }
        if(supportFragmentManager.findFragmentByTag("beatsize_fragment") == null) {
            supportFragmentManager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.beatSizeFragment, BeatsizeSubfragment(), "beatsize_fragment").commit()
        }

        btBeatSize.typeface = ResourcesCompat.getFont(this, R.font.xolonium_regular)

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pbTrainingTime.setProgressColor(resources.getColor(R.color.colorAccent, theme))
            pbTrainingTime.setTextColor(resources.getColor(R.color.colorAccent, theme))
        } else {
            @Suppress("DEPRECATION")
            pbTrainingTime.setProgressColor(resources.getColor(R.color.colorAccent))
            @Suppress("DEPRECATION")
            pbTrainingTime.setTextColor(resources.getColor(R.color.colorAccent))
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
                btBeatSize.text = getString(R.string.beatsize, it, model.valueOfBeatsLiveData.value ?: 4)

                (llBeats as BeatsContainerView).setBeatsPerBar(it)
                tickService?.setBeatsSequence((llBeats as BeatsContainerView).getBeatTypeList())
            }
        })

        model.valueOfBeatsLiveData.observe(this, Observer {
            if (it != null) {
                btBeatSize.text = getString(R.string.beatsize, model.beatsPerBarLiveData.value ?: 4, it)
            }
        })

        btBookmarks.setOnClickListener {
            if(bookmarksFragment.visibility == View.INVISIBLE) {
                bookmarksFragment.visibility = View.VISIBLE
                beatSizeFragment.visibility = View.INVISIBLE
                btBeatSize.isChecked = false
                setToggleButtonTextColor(btBeatSize, R.color.colorAccent)
            } else {
                bookmarksFragment.visibility = View.INVISIBLE
            }
        }

        btBeatSize.setOnClickListener {
            if(beatSizeFragment.visibility == View.INVISIBLE) {
                beatSizeFragment.visibility = View.VISIBLE
                setToggleButtonTextColor(btBeatSize, R.color.colorPrimaryDark)
                bookmarksFragment.visibility = View.INVISIBLE
                btBookmarks.isChecked = false
            } else {
                setToggleButtonTextColor(btBeatSize, R.color.colorAccent)
                beatSizeFragment.visibility = View.INVISIBLE
            }
        }

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

    override fun onResume() {
        super.onResume()

        if(btBeatSize.isChecked) {
            beatSizeFragment.visibility = View.VISIBLE
            setToggleButtonTextColor(btBeatSize, R.color.colorPrimaryDark)
        } else if(btBookmarks.isChecked) {
            bookmarksFragment.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        model.beatsValues = llBeats.getBeatTypeListAsString()
        model.saveToPrefs()
        if (!isBound) return
        unbindService(sConn)
        isBound = false
    }

    private fun setToggleButtonTextColor(button: ToggleButton, colorId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setTextColor(resources.getColor(colorId,theme))
        } else {
            @Suppress("DEPRECATION")
            button.setTextColor(resources.getColor(colorId))
        }
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
