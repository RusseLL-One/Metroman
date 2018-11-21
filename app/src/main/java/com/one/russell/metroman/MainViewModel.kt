package com.one.russell.metroman

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.os.Bundle
import java.lang.StringBuilder
import java.util.*
import kotlin.properties.Delegates

const val MIN_BPM = 10
const val MAX_BPM = 500
const val MAX_BEATS_PER_BAR = 16
const val MAX_VALUES_OF_BEAT = 7

class MainViewModel : ViewModel() {
    private var prefs: Preferences by Delegates.notNull()
    var bpmLiveData = MutableLiveData<Int>()
    var soundPresetLiveData = MutableLiveData<Int>()
    var trainingLiveData = MutableLiveData<Bundle>()
    var beatsPerBarLiveData = MutableLiveData<Int>()
    var valueOfBeatsLiveData = MutableLiveData<Int>()
    var flasherValueLiveData = MutableLiveData<Boolean>()
    var vibrateValueLiveData = MutableLiveData<Boolean>()
    var beatsValues = String()
    val bookmarks = ArrayList<Int>()
    var adShowAttempts = 2

    var tempoIncStartValue = 90
    var tempoIncEndValue = 90
    var tempoIncBarsValue = 1
    var tempoIncIncreaseValue = 5
    var tempoIncTimeValue = 5

    var barDropChanceValue = 30
    var barDropNormalValue = 1
    var barDropMutedValue = 1

    var beatDropChanceValue = 30

    fun initPrefs(context: Context) {
        prefs = Preferences(context)

        beatsValues = prefs.getBeatsValues()

        bookmarks.clear() //For screen rotation case
        val bookmarksStr = prefs.getBookmarks()
        val st = StringTokenizer(bookmarksStr, ",")
        while (st.hasMoreTokens()) {
            bookmarks.add(Integer.parseInt(st.nextToken()))
        }

        soundPresetLiveData.postValue(prefs.getAccentSoundId())
        bpmLiveData.postValue(prefs.getLastBpm())
        beatsPerBarLiveData.postValue(beatsValues.length)
        valueOfBeatsLiveData.postValue(prefs.getValueOfBeats())
        flasherValueLiveData.postValue(prefs.getFlasherValue())
        vibrateValueLiveData.postValue(prefs.getVibrateValue())

        tempoIncStartValue = prefs.getTempoIncStartValue()
        tempoIncEndValue = prefs.getTempoIncEndValue()
        tempoIncBarsValue = prefs.getTempoIncBarsValue()
        tempoIncIncreaseValue = prefs.getTempoIncIncreaseValue()
        tempoIncTimeValue = prefs.getTempoIncTimeValue()

        barDropChanceValue = prefs.getBarDropChanceValue()
        barDropNormalValue = prefs.getBarDropNormalValue()
        barDropMutedValue = prefs.getBarDropMutedValue()

        beatDropChanceValue = prefs.getBeatDropChanceValue()

    }

    fun setBpmLiveData(bpm: Int) {
        bpmLiveData.postValue(bpm)
    }

    fun setSoundPresetId(id: Int) {
        soundPresetLiveData.postValue(id)
    }

    fun setFlasherValue(checked: Boolean) {
        flasherValueLiveData.postValue(checked)
    }

    fun setVibrateValue(checked: Boolean) {
        vibrateValueLiveData.postValue(checked)
    }

    fun startTraining(params: Bundle) {
        var tempValue = params.getInt("startBpm")
        if (tempValue != 0) {
            tempoIncStartValue = tempValue
            prefs.setTempoIncStartValue(tempoIncStartValue)
        }

        tempValue = params.getInt("endBpm")
        if (tempValue != 0) {
            tempoIncEndValue = tempValue
            prefs.setTempoIncEndValue(tempoIncEndValue)
        }

        tempValue = params.getInt("bars")
        if (tempValue != 0) {
            tempoIncBarsValue = tempValue
            prefs.setTempoIncBarsValue(tempoIncBarsValue)
        }

        tempValue = params.getInt("increment")
        if (tempValue != 0) {
            tempoIncIncreaseValue = tempValue
            prefs.setTempoIncIncreaseValue(tempoIncIncreaseValue)
        }

        tempValue = params.getInt("minutes")
        if (tempValue != 0) {
            tempoIncTimeValue = tempValue
            prefs.setTempoIncTimeValue(tempoIncTimeValue)
        }

        tempValue = params.getInt("barChance")
        if (tempValue != 0) {
            barDropChanceValue = tempValue
            prefs.setBarDropChanceValue(barDropChanceValue)
        }

        tempValue = params.getInt("normalBars")
        if (tempValue != 0) {
            barDropNormalValue = tempValue
            prefs.setBarDropNormalValue(barDropNormalValue)
        }

        tempValue = params.getInt("mutedBars")
        if (tempValue != 0) {
            barDropMutedValue = tempValue
            prefs.setBarDropMutedValue(barDropMutedValue)
        }

        tempValue = params.getInt("beatChance")
        if (tempValue != 0) {
            beatDropChanceValue = tempValue
            prefs.setBeatDropChanceValue(beatDropChanceValue)
        }

        trainingLiveData.postValue(params)
    }

    fun saveToPrefs() {
        val soundPresetId = soundPresetLiveData.value ?: 1
        prefs.setSoundPresetId(soundPresetId)

        val lastBpm = bpmLiveData.value ?: 10
        prefs.setLastBpm(lastBpm)

        val bookmarksStr = StringBuilder()
        for (i in bookmarks) {
            bookmarksStr.append(i).append(",")
        }
        prefs.setBookmarks(bookmarksStr.toString())

        val valueOfBeats = valueOfBeatsLiveData.value ?: 4
        prefs.setValueOfBeats(valueOfBeats)

        val flasherValue = flasherValueLiveData.value ?: false
        prefs.setFlasherValue(flasherValue)

        val vibrateValue = vibrateValueLiveData.value ?: false
        prefs.setVibrateValue(vibrateValue)

        prefs.setBeatsValues(beatsValues)
    }
}