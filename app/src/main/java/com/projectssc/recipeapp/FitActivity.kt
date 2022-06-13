package com.projectssc.recipeapp

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_fit.*


class FitActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fit)

        loadData()
        resetSteps()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        backToMain.setOnClickListener {
            var intent = Intent(this@FitActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepsSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepsSensor == null) {
            Toast.makeText(this, "This device don't support step counting.", Toast.LENGTH_SHORT)
                .show()
        } else {
            sensorManager?.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (running) {
            totalSteps = event!!.values[0]
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            val currentCalories = currentSteps * 0.04
            caloriesBurned.text = ("$currentCalories")
            stepsDone.text = ("$currentSteps")

            progress_circular.apply {
                setProgressWithAnimation(currentCalories.toFloat())
            }
        }
    }

    private fun resetSteps() {
        caloriesBurned.setOnClickListener {
            Toast.makeText(this, "Long tap to reset the counters", Toast.LENGTH_SHORT)
                .show()
        }

        caloriesBurned.setOnLongClickListener {
            previousTotalSteps = totalSteps
            caloriesBurned.text = 0.toString()
            stepsDone.text = 0.toString()
            saveData()

            true
        }

    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
        Log.d("FitActivity", "$savedNumber")
        previousTotalSteps = savedNumber
    }


}