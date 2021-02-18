package com.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null

    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var seekBarProgress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Refresh.setBackgroundDrawable(null)

        loadData()
        SeekBarListen()
        resetSteps()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun SeekBarListen() {
        slider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tv_totalMax.text = "/" + slider.progress.toString()
                progress_circular.progressMax = slider.progress.toFloat()
                saveData()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //Ignoring
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //Ignoring
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onResume() {
        super.onResume()
        running = true
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if(stepSensor == null) {
            Toast.makeText(this, "Aucun capteur detecté sur cette appareil!", Toast.LENGTH_SHORT).show()

        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Ignoring
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if(running) {
            totalSteps = event!!.values[0]
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            tv_stepsTaken.text = ("$currentSteps")
            textView.text = "Distance : " + (("$currentSteps").toInt() * 0.64).toString() + " m"

            progress_circular.apply {
                setProgressWithAnimation(currentSteps.toFloat())
            }
        }
    }

    private fun resetSteps() {
        tv_stepsTaken.setOnClickListener {
            Toast.makeText(this, "Rester appuyer pour reset les pas", Toast.LENGTH_SHORT).show()
        }

        tv_stepsTaken.setOnLongClickListener {
            previousTotalSteps = totalSteps
            tv_stepsTaken.text = 0.toString()
            saveData()

            true
        }
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.putInt("slide", slider.progress)
        editor.apply()
    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
        Log.d("MainActivity", "$savedNumber")
        previousTotalSteps = savedNumber
        seekBarProgress = sharedPreferences.getInt("slide",0)
        slider.progress = seekBarProgress
        tv_totalMax.text = "/" + seekBarProgress.toString()
    }

    fun refresh() {
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}
