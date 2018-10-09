package com.one.russell.metronomekotlin

import android.media.AudioManager
import android.media.SoundPool

class ClickPlayer {
    private var clickSoundId: Int
    private var subAccentSoundId: Int
    private var accentSoundId: Int

    private val clicker: SoundPool

    init {
        clicker = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        accentSoundId = clicker.load(App.getAppInstance(), R.raw.clave, 1)
        subAccentSoundId = clicker.load(App.getAppInstance(), R.raw.rotate_click, 1)
        clickSoundId = clicker.load(App.getAppInstance(), R.raw.click, 1)
    }

    fun setAccentSound(id: Int) {
        when (id) {
            1 -> {
                accentSoundId = clicker.load(App.getAppInstance(), R.raw.clave, 1)
                subAccentSoundId = clicker.load(App.getAppInstance(), R.raw.rotate_click, 1)
                clickSoundId = clicker.load(App.getAppInstance(), R.raw.click, 1)
            }
            2 -> {
                accentSoundId = clicker.load(App.getAppInstance(), R.raw.click, 1)
                subAccentSoundId = clicker.load(App.getAppInstance(), R.raw.clave, 1)
                clickSoundId = clicker.load(App.getAppInstance(), R.raw.rotate_click, 1)
            }
            3 -> {
                accentSoundId = clicker.load(App.getAppInstance(), R.raw.rotate_click, 1)
                subAccentSoundId = clicker.load(App.getAppInstance(), R.raw.click, 1)
                clickSoundId = clicker.load(App.getAppInstance(), R.raw.clave, 1)
            }
            else -> {
                accentSoundId = clicker.load(App.getAppInstance(), R.raw.clave, 1)
                subAccentSoundId = clicker.load(App.getAppInstance(), R.raw.rotate_click, 1)
                clickSoundId = clicker.load(App.getAppInstance(), R.raw.click, 1)
            }
        }
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
        val beatSoundId = when (beatType) {
            BeatType.ACCENT -> accentSoundId
            BeatType.SUBACCENT -> subAccentSoundId
            BeatType.BEAT -> clickSoundId
            else -> 0
        }
        clicker.play(beatSoundId, 1f, 1f, 0, 0, 1f)
    }


    fun release() {
        //clicker.release()
    }
}
