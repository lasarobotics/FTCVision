package org.lasarobotics.vision.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.lasarobotics.vision.android.Util;

/**
 * Stores data collected by the accelerometer
 */
public class Accelerometer implements SensorEventListener {

    //Faster delays give more accurate results but slow program speed
    private static final int DELAY_SPEED = SensorManager.SENSOR_DELAY_NORMAL;

    private static final double ACCELERATION_THRESHOLD = 0.15; //Acceleration must be above this threshold to be considered not noise

    private float[] sensorData = new float[3];

    private Sensor accelerometer;
    private SensorManager sm;

    public Accelerometer() {
        sm = (SensorManager) Util.getContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, DELAY_SPEED);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorData = event.values;
    }

    public boolean isAccelerating(int axis) {
        if(axis >= 0 && axis <= 4)
            return Math.abs(sensorData[axis]) > ACCELERATION_THRESHOLD;
        return false;
    }

    public float[] getAcceleration() {
        return sensorData;
    }
    public float getAcceleration(int axis) {
        if(axis >= 0 && axis <= 4)
            return sensorData[axis];
        return 0;
    }

    public void stop() {

    }
}
