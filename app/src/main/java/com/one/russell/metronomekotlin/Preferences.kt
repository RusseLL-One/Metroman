package com.one.russell.metronomekotlin

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import kotlin.properties.Delegates

class Preferences(context : Context) {

    private var pref : SharedPreferences by Delegates.notNull()

    init {
        pref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun setAccentSoundId(soundId: Int) {
        val editor = pref.edit()
        editor.putInt("accent_sound_id", soundId)
        editor.apply()
    }

    fun setBeatSoundId(soundId: Int) {
        val editor = pref.edit()
        editor.putInt("beat_sound_id", soundId)
        editor.apply()
    }

    fun getBeatSoundId() = pref.getInt("beat_sound_id", 1)
    fun getAccentSoundId() = pref.getInt("accent_sound_id", 1)
}