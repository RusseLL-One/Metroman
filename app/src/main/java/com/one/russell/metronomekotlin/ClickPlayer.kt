package com.one.russell.metronomekotlin

import android.media.AudioManager
import android.media.SoundPool

class ClickPlayer {
    private var clickSoundId: Int
    private var accentSoundId: Int

    private var beat = 0

    private val clicker: SoundPool

    /*private var listener = object : ParamsListener {
        override fun onBeatSoundChange(soundId: Int) {
            clickSoundId = soundId
        }

        override fun onAccentSoundChange(soundId: Int) {
            accentSoundId = soundId
        }
    }*/

    init {
        clicker = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        clickSoundId = clicker.load(App.getAppInstance(), R.raw.click, 1)
        accentSoundId = clicker.load(App.getAppInstance(), R.raw.clave, 1)
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

    fun click(beatType: BeatType) {
        val beatSoundId = if (beatType == BeatType.ACCENT || beatType == BeatType.SUBACCENT) {
            beat = 0
            accentSoundId
        } else if (beatType == BeatType.MUTE) {
            0
        } else {
            clickSoundId
        }
        clicker.play(beatSoundId, 1f, 1f, 0, 0, 1f)

        //return isAccent //isNextBar
    }


    fun release() {
        //clicker.release()
    }

    interface ParamsListener {
        fun onBeatSoundChange(soundId: Int)
        fun onAccentSoundChange(soundId: Int)
    }
}
