package com.minar.randomix.utilities;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

// A listener to detect a shake gesture
public class ShakeEventListener implements SensorEventListener {
    // Minimum movement force
    private static final int MIN_FORCE = 14;
    // Number of direction changes
    private static final int MIN_DIRECTION_CHANGE = 4;
    // Maximum pause between movements
    private static final int MAX_PAUSE_BETWEEN_DIRECTION_CHANGE = 200;
    // Maximum time for the whole shake gesture
    private static final int MAX_TOTAL_DURATION_OF_SHAKE = 500;
    // Time for gesture start, last movement start and number of movements
    private long mFirstDirectionChangeTime = 0;
    private long mLastDirectionChangeTime;
    private int mDirectionChangeCount = 0;

    // Last x, y and z positions
    private float lastX = 0;
    private float lastY = 0;
    private float lastZ = 0;

    // Listener called when a shake is detected
    private OnShakeListener mShakeListener;

    // Interface for the listener
    public interface OnShakeListener {
        void onShake();
    }

    // Set the listener
    public void setOnShakeListener(OnShakeListener listener) {
        mShakeListener = listener;
    }

    public void onSensorChanged(SensorEvent se) {
        // Get sensor data (x, y, z)
        float x = se.values[0];
        float y = se.values[1];
        float z = se.values[2];

        // Calculate movement
        float totalMovement = Math.abs(x + y + z - lastX - lastY - lastZ);

        if (totalMovement > MIN_FORCE) {
            // Get time
            long now = System.currentTimeMillis();
            // Store first movement time
            if (mFirstDirectionChangeTime == 0) {
                mFirstDirectionChangeTime = now;
                mLastDirectionChangeTime = now;
            }

            // Check if the last movement was not long ago
            long lastChangeWasAgo = now - mLastDirectionChangeTime;
            if (lastChangeWasAgo < MAX_PAUSE_BETWEEN_DIRECTION_CHANGE) {
                // Store movement data
                mLastDirectionChangeTime = now;
                mDirectionChangeCount++;
                // Store last sensor data
                lastX = x;
                lastY = y;
                lastZ = z;
                // Check how many movements are so far
                if (mDirectionChangeCount >= MIN_DIRECTION_CHANGE) {
                    // Check total duration
                    long totalDuration = now - mFirstDirectionChangeTime;
                    if (totalDuration < MAX_TOTAL_DURATION_OF_SHAKE) {
                        mShakeListener.onShake();
                        resetShakeParameters();
                    }
                }
            } else {
                resetShakeParameters();
            }
        }
    }

    // Reset the parameters
    private void resetShakeParameters() {
        mFirstDirectionChangeTime = 0;
        mDirectionChangeCount = 0;
        mLastDirectionChangeTime = 0;
        lastX = 0;
        lastY = 0;
        lastZ = 0;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}