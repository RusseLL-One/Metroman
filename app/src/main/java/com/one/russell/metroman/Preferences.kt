package com.one.russell.metroman

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import kotlin.properties.Delegates

class Preferences(context: Context) {

    private var pref: SharedPreferences by Delegates.notNull()

    init {
        pref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun setLastBpm(bpm: Int) {
        val editor = pref.edit()
        editor.putInt("last_bpm", bpm)
        editor.apply()
    }

    fun setSoundPresetId(soundId: Int) {
        val editor = pref.edit()
        editor.putInt("accent_sound_id", soundId)
        editor.apply()
    }

    fun setBookmarks(bookmarks: String) {
        val editor = pref.edit()
        editor.putString("bookmarks", bookmarks)
        editor.apply()
    }

    fun setValueOfBeats(value: Int) {
        val editor = pref.edit()
        editor.putInt("value_of_beats", value)
        editor.apply()
    }

    fun setBeatsValues(values: String) {
        val editor = pref.edit()
        editor.putString("beatsValues", values)
        editor.apply()
    }

    fun setFlasherValue(value: Boolean) {
        val editor = pref.edit()
        editor.putBoolean("flasher", value)
        editor.apply()
    }

    fun setVibrateValue(value: Boolean) {
        val editor = pref.edit()
        editor.putBoolean("vibrate", value)
        editor.apply()
    }

    fun setTempoIncStartValue(value: Int) {
        val editor = pref.edit()
        editor.putInt("startBpm", value)
        editor.apply()
    }

    fun setTempoIncEndValue(value: Int) {
        val editor = pref.edit()
        editor.putInt("endBpm", value)
        editor.apply()
    }

    fun setTempoIncBarsValue(value: Int) {
        val editor = pref.edit()
        editor.putInt("bars", value)
        editor.apply()
    }

    fun setTempoIncIncreaseValue(value: Int) {
        val editor = pref.edit()
        editor.putInt("increment", value)
        editor.apply()
    }

    fun setTempoIncTimeValue(value: Int) {
        val editor = pref.edit()
        editor.putInt("minutes", value)
        editor.apply()
    }

    fun setBarDropChanceValue(value: Int) {
        val editor = pref.edit()
        editor.putInt("barChance", value)
        editor.apply()
    }

    fun setBarDropNormalValue(value: Int) {
        val editor = pref.edit()
        editor.putInt("normalBars", value)
        editor.apply()
    }

    fun setBarDropMutedValue(value: Int) {
        val editor = pref.edit()
        editor.putInt("mutedBars", value)
        editor.apply()
    }

    fun setBeatDropChanceValue(value: Int) {
        val editor = pref.edit()
        editor.putInt("beatChance", value)
        editor.apply()
    }

    fun getLastBpm() = pref.getInt("last_bpm", 60)
    fun getAccentSoundId() = pref.getInt("accent_sound_id", 1)
    fun getBookmarks() = pref.getString("bookmarks", "")
    fun getValueOfBeats() = pref.getInt("value_of_beats", 4)
    fun getFlasherValue() = pref.getBoolean("flasher", false)
    fun getVibrateValue() = pref.getBoolean("vibrate", false)
    fun getTempoIncStartValue() = pref.getInt("startBpm", 90)
    fun getTempoIncEndValue() = pref.getInt("endBpm", 160)
    fun getTempoIncBarsValue() = pref.getInt("bars", 1)
    fun getTempoIncIncreaseValue() = pref.getInt("increment", 5)
    fun getTempoIncTimeValue() = pref.getInt("minutes", 5)
    fun getBarDropChanceValue() = pref.getInt("barChance", 30)
    fun getBarDropNormalValue() = pref.getInt("normalBars", 2)
    fun getBarDropMutedValue() = pref.getInt("mutedBars", 1)
    fun getBeatDropChanceValue() = pref.getInt("beatChance", 30)
    fun getBeatsValues(): String {
        val result = pref.getString("beatsValues", null)
        return if(result == null || result.isEmpty()) "3111"
        else result
    }
}
