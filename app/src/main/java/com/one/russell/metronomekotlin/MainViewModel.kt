package com.one.russell.metronomekotlin

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.util.Log
import kotlin.properties.Delegates
const val MIN_BPM = 10
const val MAX_BPM = 600

class MainViewModel : ViewModel() {
    private var prefs: Preferences by Delegates.notNull()
    var bpmLiveData = MutableLiveData<Int>()
    var accentSoundLiveData = MutableLiveData<Int>()
    var beatSoundLiveData = MutableLiveData<Int>()
    var beatsPerBar = 4
    var valueOfBeats = 4

    fun initPrefs(context: Context) {
        prefs = Preferences(context)

        accentSoundLiveData.postValue(prefs.getAccentSoundId())
        beatSoundLiveData.postValue(prefs.getBeatSoundId())
        bpmLiveData.postValue(prefs.getLastBpm())
        beatsPerBar = prefs.getBeatsPerBar()
        valueOfBeats = prefs.getValueOfBeats()
    }

    fun setBpmLiveData(bpm: Int) {
        bpmLiveData.postValue(bpm)
    }

    fun setAccentSoundId(id: Int) {
        accentSoundLiveData.postValue(id)
    }

    fun setBeatSoundId(id: Int) {
        beatSoundLiveData.postValue(id)
    }

    /*fun setBeatsPerBar(beats: Int) {
        beatsPerBar = beats
    }

    fun setValueOfBeats(value: Int) {
        valueOfBeats = value
    }*/

    fun saveToPrefs() {
        val accentSoundId = accentSoundLiveData.value ?: 1
        prefs.setAccentSoundId(accentSoundId)

        val beatSoundId = beatSoundLiveData.value ?: 1
        prefs.setBeatSoundId(beatSoundId)

        val lastBpm = bpmLiveData.value ?: 10
        prefs.setLastBpm(lastBpm)

        prefs.setBeatsPerBar(beatsPerBar)
        prefs.setValueOfBeats(valueOfBeats)
    }
}