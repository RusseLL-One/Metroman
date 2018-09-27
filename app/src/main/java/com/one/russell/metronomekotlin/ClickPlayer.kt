package com.one.russell.metronomekotlin

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.View
import java.io.IOException
import android.util.Log
import android.view.animation.*
import com.github.piasy.rxandroidaudio.StreamAudioPlayer
import kotlin.properties.Delegates


private const val SAMPLE_RATE = 44100
private const val BUFFER_SIZE = 28800

class ClickPlayer(private var activity: FragmentActivity) {
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
    }

    fun setAccentSound(id: Int?) {
        val soundResourceId = when (id) {
            1 -> R.raw.drum_1
            2 -> R.raw.wave_1
            3 -> R.raw.wave_2
            else -> R.raw.drum_1
        }
        try {
            val inputStream = activity.resources.openRawResource(soundResourceId)
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
            val inputStream = activity.resources.openRawResource(soundResourceId)
            soundLength = inputStream.read(initSoundArray, 0, initSoundArray.size)
        } catch (e: IOException) {
        }
    }

    fun play() {
        val silenceLength = (60f / model.bpm * StreamAudioPlayer.DEFAULT_SAMPLE_RATE.toFloat() * 2f - soundLength).toInt()
        Log.d("qwe", "play(), silenceLength=" + silenceLength)

        val beatSound: ByteArray
            if (beat % beatsPerBar == 0) {
                beatSound = initAccentSoundArray
                beat = 0
            } else {
                beatSound = initSoundArray
            }

            animateBeatBall(beatSound.size + silenceLength)

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

    private fun animateBeatBall(duration: Int) {
        val pathLength = (beatLine.height - beatBall.height).toFloat()
        val animation: Animation
        animation = if(isBeatBallOnTop) {
            TranslateAnimation(Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, pathLength);
        } else {
            TranslateAnimation(Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, pathLength, Animation.ABSOLUTE, 0f);
        }
        isBeatBallOnTop = !isBeatBallOnTop
        animation.interpolator = LinearInterpolator()
        animation.duration = (duration * 1000 / (2 * StreamAudioPlayer.DEFAULT_SAMPLE_RATE)).toLong()

            Log.d("qwe", "animateBeatBall(), duration=" + duration)
        beatBall.postDelayed({
            beatBall.startAnimation(animation)
            beatBall.requestLayout()
        }, 400) //todo magic numbers

    }

    fun setBeatSize(size: Int) {
        beatsPerBar = size
    }

    fun release() {
        //audioTrack.stop()
        audioTrack.release()
        //mStreamAudioPlayer.release()
    }
}
