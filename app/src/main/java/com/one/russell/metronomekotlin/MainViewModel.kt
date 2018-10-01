package com.one.russell.metronomekotlin

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.util.Log
import kotlin.properties.Delegates
const val MIN_BPM = 10
const val MAX_BPM = 500

class MainViewModel : ViewModel() {
    var bpm = 10
    private var prefs: Preferences by Delegates.notNull()
    var accentSoundLiveData = MutableLiveData<Int>()
    var beatSoundLiveData = MutableLiveData<Int>()

    fun initPrefs(context: Context) {
        prefs = Preferences(context)

        accentSoundLiveData.postValue(prefs.getAccentSoundId())
        beatSoundLiveData.postValue(prefs.getBeatSoundId())
        bpm = prefs.getLastBpm()
    }

    fun setAccentSoundId(id: Int) {
        accentSoundLiveData.postValue(id)
    }

    fun setBeatSoundId(id: Int) {
        beatSoundLiveData.postValue(id)
    }

    fun saveToPrefs() {
        val accentSoundId = accentSoundLiveData.value ?: 1
        prefs.setAccentSoundId(accentSoundId)

        val beatSoundId = beatSoundLiveData.value ?: 1
        prefs.setBeatSoundId(beatSoundId)

        prefs.setLastBpm(bpm)
    }
    //todo вынести все глобальные переменные сюда

}