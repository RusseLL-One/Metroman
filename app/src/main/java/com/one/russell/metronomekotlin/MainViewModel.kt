package com.one.russell.metronomekotlin

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.os.Bundle
import android.util.Log
import kotlin.properties.Delegates
const val MIN_BPM = 10
const val MAX_BPM = 600
const val MAX_BEATS_PER_BAR = 16
const val MAX_VALUES_OF_BEAT = 7

class MainViewModel : ViewModel() {
    private var prefs: Preferences by Delegates.notNull()
    var bpmLiveData = MutableLiveData<Int>()
    var accentSoundLiveData = MutableLiveData<Int>()
    var beatSoundLiveData = MutableLiveData<Int>()
    var trainingLiveData = MutableLiveData<Bundle>()
    var beatsPerBarLiveData = MutableLiveData<Int>()
    var valueOfBeatsLiveData = MutableLiveData<Int>()

    fun initPrefs(context: Context) {
        prefs = Preferences(context)

        accentSoundLiveData.postValue(prefs.getAccentSoundId())
        beatSoundLiveData.postValue(prefs.getBeatSoundId())
        bpmLiveData.postValue(prefs.getLastBpm())
        beatsPerBarLiveData.postValue(prefs.getBeatsPerBar())
        valueOfBeatsLiveData.postValue(prefs.getValueOfBeats())
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

    fun startTraining(params: Bundle) {
        trainingLiveData.postValue(params)
    }

    fun saveToPrefs() {
        val accentSoundId = accentSoundLiveData.value ?: 1
        prefs.setAccentSoundId(accentSoundId)

        val beatSoundId = beatSoundLiveData.value ?: 1
        prefs.setBeatSoundId(beatSoundId)

        val lastBpm = bpmLiveData.value ?: 10
        prefs.setLastBpm(lastBpm)

        val beatsPerBar = beatsPerBarLiveData.value ?: 4
        prefs.setBeatsPerBar(beatsPerBar)

        val valueOfBeats = valueOfBeatsLiveData.value ?: 4
        prefs.setValueOfBeats(valueOfBeats)
    }
}