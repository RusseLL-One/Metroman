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
import com.github.piasy.rxandroidaudio.StreamAudioPlayer
import kotlin.properties.Delegates
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter




private const val SAMPLE_RATE = 44100
private const val BUFFER_SIZE = 28800

class ClickPlayer(activity: FragmentActivity): Runnable {
    private var initSoundArray: ByteArray
    private var initAccentSoundArray: ByteArray
    private var beatLine: View
    private var beatBall: View
    private var soundLength: Int = 0
    private var bpm = 0
    private var beat = 0
    private var beatsPerBar = 4
    private var isBeatBallOnTop = true
    //private val mStreamAudioPlayer: StreamAudioPlayer
    private val audioTrack: AudioTrack
    private var model: MainViewModel by Delegates.notNull()
    var isPlaying = false

    init {
        initSoundArray = ByteArray(BUFFER_SIZE)
        initAccentSoundArray = ByteArray(BUFFER_SIZE)

        beatLine = activity.findViewById(R.id.vLine)
        beatBall = activity.findViewById(R.id.vBall)

        //mStreamAudioPlayer = StreamAudioPlayer.getInstance()
        //mStreamAudioPlayer.init(StreamAudioPlayer.DEFAULT_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE)

        audioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE,
                AudioTrack.MODE_STREAM)
        audioTrack.play()

        model = ViewModelProviders.of(activity).get(MainViewModel::class.java)

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
        })
    }

    fun setAccentSound(id: Int?) {
        val soundResourceId = when (id) {
            1 -> R.raw.drum_1
            2 -> R.raw.wave_1
            3 -> R.raw.wave_2
            else -> R.raw.drum_1
        }
        try {
            val inputStream = App.getAppInstance().resources.openRawResource(soundResourceId)
            inputStream.read(initAccentSoundArray, 0, initAccentSoundArray.size)
        } catch (e: IOException) {
        }
    }

    fun setBeatSound(id: Int?) {
        val soundResourceId = when (id) {
            1 -> R.raw.crisp_1
            2 -> R.raw.crisp_2
            3 -> R.raw.drum_2
            else -> R.raw.crisp_1
        }
        try {
            val inputStream = App.getAppInstance().resources.openRawResource(soundResourceId)
            soundLength = inputStream.read(initSoundArray, 0, initSoundArray.size)
        } catch (e: IOException) {
        }
    }

    fun play() {
        val silenceLength = (60f / bpm * StreamAudioPlayer.DEFAULT_SAMPLE_RATE.toFloat() * 2f - soundLength).toInt()
        Log.d("qwe", "play(), silenceLength=" + silenceLength)

        val beatSound: ByteArray
            if (beat % beatsPerBar == 0) {
                beatSound = initAccentSoundArray
                beat = 0
            } else {
                beatSound = initSoundArray
            }

            animateBeatBall ((beatSound.size + silenceLength) * 1000 / (2 * StreamAudioPlayer.DEFAULT_SAMPLE_RATE))

            if (silenceLength < 0) {
                audioTrack.write(beatSound, 0, beatSound.size + silenceLength)
                //mStreamAudioPlayer.play(beatSound, beatSound.size + silenceLength)
            } else {
                audioTrack.write(beatSound, 0, beatSound.size)
                audioTrack.write(ByteArray(silenceLength), 0, silenceLength)
                //mStreamAudioPlayer.play(beatSound, beatSound.size)
                //mStreamAudioPlayer.play(ByteArray(silenceLength), silenceLength)
            }


        beat++
    }

    override fun run() {
        isPlaying = true
        while(isPlaying) {
            play()
        }
        //release()
    }

    //todo перенести в MainActivity
    private fun animateBeatBall(duration: Int) {
        val pathLength = beatLine.height - beatBall.height

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
            (beatBall.layoutParams as FrameLayout.LayoutParams).topMargin = x
            beatBall.requestLayout()
        }

        val colorAnimator = ValueAnimator.ofInt(0, 255)
        colorAnimator.interpolator = DecelerateInterpolator()
        colorAnimator.addUpdateListener { animation ->

            val animatorValue = (animation.animatedValue as Int)
            val colorStr = Color.rgb(animatorValue,animatorValue,animatorValue)
            val blackFilter = PorterDuffColorFilter(colorStr, PorterDuff.Mode.MULTIPLY)
            beatBall.background.setColorFilter(blackFilter)
        }

        Log.d("qwe", "onTick, thread:" + Thread.currentThread().name)
        beatBall.post {
            positionAnimator.start()
            colorAnimator.start()
        }

    }

    fun setBeatSize(size: Int) {
        beatsPerBar = size
    }

    fun stop() {
        isPlaying = false
    }

    fun release() {
        //audioTrack.stop()
        audioTrack.release()
        //mStreamAudioPlayer.release()
    }
}
