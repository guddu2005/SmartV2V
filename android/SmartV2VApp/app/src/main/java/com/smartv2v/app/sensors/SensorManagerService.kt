package com.smartv2v.app.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SensorManagerService handles hardware Accelerometer and Gyroscope sensor readings.
 * 
 * NOTE & LIMITATION:
 * Raw smartphone X/Y/Z sensor axes are aligned with the phone frame, not necessarily
 * the vehicle coordinate frame (forward/lateral/vertical). For precise automotive alignment,
 * phone mounting orientation matrix transformation or sensor fusion would be required.
 */
data class SensorData(
    val accelX: Double = 0.0,
    val accelY: Double = 0.0,
    val accelZ: Double = 9.81,
    val gyroX: Double = 0.0,
    val gyroY: Double = 0.0,
    val gyroZ: Double = 0.0
)

class SensorManagerService(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _sensorState = MutableStateFlow(SensorData())
    val sensorState: StateFlow<SensorData> = _sensorState.asStateFlow()

    private var isListening = false

    fun startListening() {
        if (isListening) return
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        isListening = true
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val current = _sensorState.value
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                _sensorState.value = current.copy(
                    accelX = event.values[0].toDouble(),
                    accelY = event.values[1].toDouble(),
                    accelZ = event.values[2].toDouble()
                )
            }
            Sensor.TYPE_GYROSCOPE -> {
                _sensorState.value = current.copy(
                    gyroX = event.values[0].toDouble(),
                    gyroY = event.values[1].toDouble(),
                    gyroZ = event.values[2].toDouble()
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
