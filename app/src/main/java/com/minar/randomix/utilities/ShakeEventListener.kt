package com.minar.randomix.utilities

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.abs

class ShakeEventListener : SensorEventListener {

    private val minForce = 14
    private val minDirectionChange = 4
    private val maxPauseBetweenDirectionChange = 200L
    private val maxTotalDurationOfShake = 500L

    private var firstDirectionChangeTime = 0L
    private var lastDirectionChangeTime = 0L
    private var directionChangeCount = 0

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    private var shakeListener: OnShakeListener? = null

    fun interface OnShakeListener {
        fun onShake()
    }

    fun setOnShakeListener(listener: OnShakeListener) {
        shakeListener = listener
    }

    override fun onSensorChanged(se: SensorEvent) {
        val x = se.values[0]
        val y = se.values[1]
        val z = se.values[2]

        val totalMovement = abs(x + y + z - lastX - lastY - lastZ)

        if (totalMovement > minForce) {
            val now = System.currentTimeMillis()
            if (firstDirectionChangeTime == 0L) {
                firstDirectionChangeTime = now
                lastDirectionChangeTime = now
            }
            val lastChangeWasAgo = now - lastDirectionChangeTime
            if (lastChangeWasAgo < maxPauseBetweenDirectionChange) {
                lastDirectionChangeTime = now
                directionChangeCount++
                lastX = x; lastY = y; lastZ = z
                if (directionChangeCount >= minDirectionChange) {
                    val totalDuration = now - firstDirectionChangeTime
                    if (totalDuration < maxTotalDurationOfShake) {
                        shakeListener?.onShake()
                        resetShakeParameters()
                    }
                }
            } else {
                resetShakeParameters()
            }
        }
    }

    private fun resetShakeParameters() {
        firstDirectionChangeTime = 0L
        directionChangeCount = 0
        lastDirectionChangeTime = 0L
        lastX = 0f; lastY = 0f; lastZ = 0f
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
}
