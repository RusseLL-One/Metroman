package com.one.russell.metronome_kotlin.fragments

import android.Manifest
import androidx.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.one.russell.metronome_kotlin.MainViewModel
import com.one.russell.metronome_kotlin.R
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlin.properties.Delegates

const val PERMISSIONS_REQUEST_CAMERA = 168

class SettingsFragment : Fragment() {

    private var model: MainViewModel by Delegates.notNull()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity
        if (activity != null) {
            model = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        }
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Glide.with(this)
                    .load(R.drawable.background_land)
                    .into(background)
        } else {
            Glide.with(this)
                    .load(R.drawable.background)
                    .into(background)
        }

        presetValue.minValue = 1
        presetValue.maxValue = 12
        presetValue.value = model.soundPresetLiveData.value ?: 1
        presetValue.wrapSelectorWheel = false

        flasherValue.isChecked = model.flasherValueLiveData.value ?: false

        vibrateValue.isChecked = model.vibrateValueLiveData.value ?: false

        presetValue.setOnValueChangedListener { _, _, value ->
            model.setSoundPresetId(value)
        }

        flasherValue.setOnCheckedChangeListener { _, checked ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                model.setFlasherValue(checked)
            } else {
                if (checked) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA)
                } else {
                    model.setFlasherValue(false)
                }
            }
        }

        vibrateValue.setOnCheckedChangeListener { _, checked -> model.setVibrateValue(checked) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.isNotEmpty()
                    && grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                model.setFlasherValue(true)
            } else {
                flasherValue.isChecked = false
            }
        }
    }
}
