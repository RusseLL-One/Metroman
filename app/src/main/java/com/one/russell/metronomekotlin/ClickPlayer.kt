package com.one.russell.metronomekotlin

import android.animation.ValueAnimator
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.support.v4.app.FragmentActivity
import android.view.View
import java.io.IOException
import android.util.Log
import android.view.animation.*
import android.widget.FrameLayout
import kotlin.properties.Delegates
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.SoundPool


private const val SAMPLE_RATE = 44100
private const val BUFFER_SIZE = 28800

class ClickPlayer(/*activity: FragmentActivity*/): Runnable {
    //private var initSoundArray: ByteArray
    //private var initAccentSoundArray: ByteArray
    private var clickSoundId: Int
    private var accentSoundId: Int

    private var soundLength: Int = 0
    var bpm = 20
    private var beat = 0
    private var beatsPerBar = 4
    private var isBeatBallOnTop = true
    private var listener = object : ParamsListener {
        override fun onBeatSoundChange(soundId: Int) {
            clickSoundId = soundId
        }

        override fun onAccentSoundChange(soundId: Int) {
            accentSoundId = soundId
        }

        override fun onBpmChange(bpm: Int) {
            this@ClickPlayer.bpm = bpm
        }
    }
    //private val mStreamAudioPlayer: StreamAudioPlayer
    //private val audioTrack: AudioTrack

    private val clicker: SoundPool
    private var model: MainViewModel by Delegates.notNull()
    var isPlaying = false

    init {
        //initSoundArray = ByteArray(BUFFER_SIZE)
        //initAccentSoundArray = ByteArray(BUFFER_SIZE)

        //beatLine = activity.findViewById(R.id.vLine)
        //beatBall = activity.findViewById(R.id.vBall)

        //mStreamAudioPlayer = StreamAudioPlayer.getInstance()
        //mStreamAudioPlayer.init(StreamAudioPlayer.DEFAULT_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE)

        /*audioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE,
                AudioTrack.MODE_STREAM)
        audioTrack.play()*/
        clicker = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        clickSoundId = clicker.load(App.getAppInstance(), R.raw.click, 1)
        accentSoundId = clicker.load(App.getAppInstance(), R.raw.clave, 1)


        /*model = ViewModelProviders.of(activity).get(MainViewModel::class.java)

        model.accentSoundLiveData.observe(activity, Observer {
            setAccentSound(it)
        })
        model.beatSoundLiveData.observe(activity, Observer {
            setBeatSound(it)
        })

        model.bpmLiveData.observe(activity, Observer {
            if(it != null) {
                bpm = it
            }
        })*/
    }

    fun setAccentSound(id: Int) {
        val resId = when (id) {
            1 -> R.raw.clave
            2 -> R.raw.click
            3 -> R.raw.rotate_click
            else -> R.raw.clave
        }
        accentSoundId = clicker.load(App.getAppInstance(), resId, 1)
    }

    fun setBeatSound(id: Int) {
        val resId = when (id) {
            1 -> R.raw.clave
            2 -> R.raw.click
            3 -> R.raw.rotate_click
            else -> R.raw.clave
        }
        clickSoundId = clicker.load(App.getAppInstance(), resId, 1)
    }

    fun play() {
        val silenceLength = (60f / bpm * SAMPLE_RATE * 2f - soundLength).toInt()
        Log.d("qwe", "play(), silenceLength=" + silenceLength)

        //val beatSound: ByteArray
        val beatSoundId: Int
            if (beat % beatsPerBar == 0) {
                //beatSound = initAccentSoundArray
                beatSoundId = accentSoundId
                beat = 0
            } else {
                //beatSound = initSoundArray
                beatSoundId = clickSoundId
            }

            //animateBeatBall ((beatSound.size + silenceLength) * 1000 / (2 * StreamAudioPlayer.DEFAULT_SAMPLE_RATE))

            /*if (silenceLength < 0) {
                audioTrack.write(beatSound, 0, beatSound.size + silenceLength)
                //mStreamAudioPlayer.play(beatSound, beatSound.size + silenceLength)
            } else {
                audioTrack.write(beatSound, 0, beatSound.size)
                audioTrack.write(ByteArray(silenceLength), 0, silenceLength)

                //mStreamAudioPlayer.play(beatSound, beatSound.size)
                //mStreamAudioPlayer.play(ByteArray(silenceLength), silenceLength)
            }*/

        clicker.play(beatSoundId, 1f, 1f, 0, 0, 1f)

        beat++
    }

    override fun run() {
        isPlaying = true
        while(isPlaying) {
            play()
        }
        //release()
    }

    fun setBeatSize(size: Int) {
        beatsPerBar = size
    }

    fun stop() {
        isPlaying = false
    }

    fun release() {
        //audioTrack.stop()
        //audioTrack.release()
        //mStreamAudioPlayer.release()
    }

    interface ParamsListener {
        fun onBeatSoundChange(soundId: Int)
        fun onAccentSoundChange(soundId: Int)
        fun onBpmChange(bpm: Int)
    }
}
