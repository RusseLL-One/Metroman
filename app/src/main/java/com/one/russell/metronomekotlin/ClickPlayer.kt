package com.one.russell.metronomekotlin

import android.app.Activity
import android.content.Context
import android.media.AudioFormat
import android.media.AudioTrack
import android.view.View
import java.io.IOException
import android.util.Log
import android.view.animation.*
import com.github.piasy.rxandroidaudio.StreamAudioPlayer
import com.github.piasy.rxandroidaudio.StreamAudioRecorder
import java.util.Collections.frequency
import android.media.AudioManager
import android.support.annotation.WorkerThread


private const val SAMPLE_RATE = 44100
private const val BUFFER_SIZE = 28800

class ClickPlayer(private var context: Context) {
    private var initSoundArray: ByteArray
    private var initAccentSoundArray: ByteArray
    private var beatLine: View
    private var beatBall: View
    private var soundLength: Int = 0
    private var bpm = 0
    private var beat = 0
    private var beatsPerBar = 4
    private var isBeatBallOnTop = true
    private val mStreamAudioPlayer: StreamAudioPlayer
    //private val audioTrack: AudioTrack

    init {
        initSoundArray = ByteArray(BUFFER_SIZE)
        initAccentSoundArray = ByteArray(BUFFER_SIZE)

        beatLine = (context as Activity).findViewById(R.id.vLine)
        beatBall = (context as Activity).findViewById(R.id.vBall)


        mStreamAudioPlayer = StreamAudioPlayer.getInstance()
        mStreamAudioPlayer.init(StreamAudioPlayer.DEFAULT_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE)

//        audioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
//                SAMPLE_RATE,
//                AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT,
//                SAMPLE_RATE,
//                AudioTrack.MODE_STREAM)
//        audioTrack.play()
    }

    fun initSound() {
        try {
            var inputStream = context.resources.openRawResource(R.raw.crisp_1)
            soundLength = inputStream.read(initSoundArray, 0, initSoundArray.size)

            inputStream = context.resources.openRawResource(R.raw.drum_1)
            inputStream.read(initAccentSoundArray, 0, initAccentSoundArray.size)
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

            animateBeatBall(beatSound.size + silenceLength)

            if (silenceLength < 0) {
                //audioTrack.write(beatSound, 0, beatSound.size + silenceLength)
                mStreamAudioPlayer.play(beatSound, beatSound.size + silenceLength)
            } else {
                //audioTrack.write(beatSound, 0, beatSound.size)
                //audioTrack.write(ByteArray(silenceLength), 0, silenceLength)
                mStreamAudioPlayer.play(beatSound, beatSound.size)
                mStreamAudioPlayer.play(ByteArray(silenceLength), silenceLength)
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
        //audioTrack.release()
        mStreamAudioPlayer.release()
    }

    fun setBPM(bpm: Int) {
        this.bpm = bpm
    }
}
