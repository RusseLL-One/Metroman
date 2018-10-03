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

    fun setLastBpm(bpm: Int) {
        val editor = pref.edit()
        editor.putInt("last_bpm", bpm)
        editor.apply()
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

    fun setBeatsPerBar(beats: Int) {
        val editor = pref.edit()
        editor.putInt("beats_per_bar", beats)
        editor.apply()
    }

    fun setValueOfBeats(value: Int) {
        val editor = pref.edit()
        editor.putInt("value_of_beats", value)
        editor.apply()
    }

    fun getLastBpm() = pref.getInt("last_bpm", 10)
    fun getBeatSoundId() = pref.getInt("beat_sound_id", 1)
    fun getAccentSoundId() = pref.getInt("accent_sound_id", 1)
    fun getBeatsPerBar() = pref.getInt("beats_per_bar", 4)
    fun getValueOfBeats() = pref.getInt("value_of_beats", 3)
}