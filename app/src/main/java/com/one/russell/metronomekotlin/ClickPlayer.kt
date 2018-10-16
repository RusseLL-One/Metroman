package com.one.russell.metronomekotlin

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.Log

class ClickPlayer {
    private var clickSoundId: Int
    private var subAccentSoundId: Int
    private var accentSoundId: Int

    private val clicker: SoundPool

    init {
        clicker = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(2)
                    .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        }
        accentSoundId = clicker.load(App.getAppInstance(), R.raw.acc1, 1)
        subAccentSoundId = clicker.load(App.getAppInstance(), R.raw.sub1, 1)
        clickSoundId = clicker.load(App.getAppInstance(), R.raw.beat1, 1)
    }

    fun setSoundPreset(id: Int) {
        Log.d("qwe", "soundpreset = " + id)
        val accentResId = App.getAppInstance().resources.getIdentifier("acc$id", "raw", App.getAppInstance().packageName)
        val subaccResId = App.getAppInstance().resources.getIdentifier("sub$id", "raw", App.getAppInstance().packageName)
        val beatResId = App.getAppInstance().resources.getIdentifier("beat$id", "raw", App.getAppInstance().packageName)

        accentSoundId = clicker.load(App.getAppInstance(), accentResId, 1)
        subAccentSoundId = clicker.load(App.getAppInstance(), subaccResId, 1)
        clickSoundId = clicker.load(App.getAppInstance(), beatResId, 1)
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

    /*fun release() {
        clicker.release()
    }*/
}
