package com.one.russell.metronomekotlin

import android.app.Activity
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.View
import java.io.IOException
import android.transition.TransitionManager
import android.support.constraint.ConstraintSet
import android.transition.ChangeBounds
import android.util.Log
import com.github.piasy.rxandroidaudio.StreamAudioPlayer

private const val SAMPLE_RATE = 28800

class ClickPlayer(private var context: Context) {
    private var initSoundArray: ByteArray
    private var initAccentSoundArray: ByteArray
    private var mainLayout: ConstraintLayout
    private var beatLine: View
    private var beatBall: View
    private var soundLength: Int = 0
    private var bpm = 0
    private var beat = 0
    private val mStreamAudioPlayer: StreamAudioPlayer

    init {
        initSoundArray = ByteArray(SAMPLE_RATE)
        initAccentSoundArray = ByteArray(SAMPLE_RATE)

        mainLayout = (context as Activity).findViewById(R.id.clMainActivity)
        beatLine = mainLayout.findViewById(R.id.vLine)
        beatBall = mainLayout.findViewById(R.id.vBall)

        mStreamAudioPlayer = StreamAudioPlayer.getInstance()
        mStreamAudioPlayer.init()
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
        val silenceLength = (60f / bpm * SAMPLE_RATE.toFloat() * 2f - soundLength).toInt()
        Log.d("qwe", "play(), silenceLength=" + silenceLength)

        if(beat % 4 == 0) {
            beat = 0
            if(silenceLength < 0) {
                mStreamAudioPlayer.play(initAccentSoundArray, initAccentSoundArray.size + silenceLength)
            } else {
                mStreamAudioPlayer.play(initAccentSoundArray, initAccentSoundArray.size)
                mStreamAudioPlayer.play(ByteArray(silenceLength), silenceLength)
            }
        } else {
            if(silenceLength < 0) {
                mStreamAudioPlayer.play(initSoundArray, initSoundArray.size + silenceLength)
            } else {
                mStreamAudioPlayer.play(initSoundArray, initSoundArray.size)
                mStreamAudioPlayer.play(ByteArray(silenceLength), silenceLength)
            }
        }
        beat++

        mainLayout.postOnAnimation {
            TransitionManager.beginDelayedTransition(mainLayout, ChangeBounds().setDuration((5000 / 100).toLong()))

            val constraintSet = ConstraintSet()
            constraintSet.clone(mainLayout)

            if(beat % 2 == 0) {
                constraintSet.connect(beatBall.id, ConstraintSet.BOTTOM, beatLine.id, ConstraintSet.BOTTOM)
                constraintSet.clear(beatBall.id, ConstraintSet.TOP)

            } else {
                constraintSet.connect(beatBall.id, ConstraintSet.TOP, beatLine.id, ConstraintSet.TOP)
                constraintSet.clear(beatBall.id, ConstraintSet.BOTTOM)

            }
            constraintSet.applyTo(mainLayout)
        }
    }

    fun stop() {
        mStreamAudioPlayer.release()
    }

    fun setBPM(bpm: Int) {
        this.bpm = bpm
    }
}
