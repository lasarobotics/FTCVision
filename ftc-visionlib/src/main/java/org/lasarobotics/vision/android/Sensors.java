package org.lasarobotics.vision.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.WindowManager;

import org.lasarobotics.vision.util.ScreenOrientation;
import org.lasarobotics.vision.util.Vector3;

/**
 * Contains methods for reading Android native sensors, other than the camera
 */
public final class Sensors implements SensorEventListener {

    private static final float PITCH_TOLERANCE = 20.0f;
    private static final float PITCH_TOLERANCE_HIGH = 45.0f;
    private static final float ROLL_MINIMUM = 0.0f;
    private static final int READ_SPEED = SensorManager.SENSOR_DELAY_NORMAL;
    private static final double ACCELERATION_THRESHOLD = 0.15; //Acceleration must be above this threshold to be considered not noise
    static float[] gravity = new float[3];
    static float[] linear_acceleration = new float[3];
    static float[] geomagnetic = new float[3];
    static float[] acceleration = new float[3];
    private static boolean activated = false;
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private final Sensor mMagneticField;
    private ScreenOrientation screenOrientation = null;

    public Sensors() {
        mSensorManager = (SensorManager) Util.getContext().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        activated = false;
        resume();
    }

    public void resume() {
        if (activated)
            return;
        mSensorManager.registerListener(this, mAccelerometer, READ_SPEED);
        mSensorManager.registerListener(this, mMagneticField, READ_SPEED);
        activated = true;
    }

    public void stop() {
        if (!activated)
            return;
        activated = false;
        mSensorManager.unregisterListener(this);
    }

    public Vector3<Float> getAccelerationVector() {
        return new Vector3<>(acceleration[0], acceleration[1], acceleration[2]);
    }

    public Vector3<Float> getLinearAccelerationVector() {
        return new Vector3<>(linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]);
    }

    public Vector3<Float> getGeomagneticVector() {
        return new Vector3<>(geomagnetic[0], geomagnetic[1], geomagnetic[2]);
    }

    public Vector3<Float> getGravityVector() {
        return new Vector3<>(gravity[0], gravity[1], gravity[2]);
    }

    public boolean isAccelerating(Axis axis) {
        return Math.abs(acceleration[axis.id]) > ACCELERATION_THRESHOLD;
    }

    public boolean hasOrientation() {
        return screenOrientation != null;
    }

    public ScreenOrientation getScreenOrientation() {
        return screenOrientation != null ? screenOrientation : ScreenOrientation.LANDSCAPE;
    }

    public ScreenOrientation getActivityScreenOrientation() {
        WindowManager windowManager = (WindowManager) Util.getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        return ScreenOrientation.getFromSurface(rotation);
    }

    public double getScreenOrientationCompensation() {
        return getScreenOrientation().getAngle() - getActivityScreenOrientation().getAngle() + ScreenOrientation.PORTRAIT.getAngle();
    }

    private void updateScreenOrientation() {
        float[] R = new float[9];
        float[] I = new float[9];
        SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
        float[] orientation = new float[3];
        SensorManager.getOrientation(R, orientation);

        //device rotation angle = pitch (first value) [clockwise from horizontal]

        double pitch = orientation[1] / 2 / Math.PI * 360.0;
        double roll = orientation[2] / 2 / Math.PI * 360.0;
        double azimuth = orientation[0] / 2 / Math.PI * 360.0;

        Log.d("Rotation", pitch + ", " + roll + ", " + azimuth);

        //If the phone is too close to the ground, don't update
        if (Math.abs(roll) <= ROLL_MINIMUM)
            return;

        ScreenOrientation current = screenOrientation;

        if (Math.abs(pitch) <= PITCH_TOLERANCE)
            if (roll > 0.0f)
                current = ScreenOrientation.LANDSCAPE_WEST;
            else
                current = ScreenOrientation.LANDSCAPE;
        else if (Math.abs(pitch) >= PITCH_TOLERANCE_HIGH)
            if (pitch > 0.0f)
                current = ScreenOrientation.PORTRAIT;
            else
                current = ScreenOrientation.PORTRAIT_REVERSE;

        screenOrientation = current;
    }

    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // alpha is calculated as t / (t + dT)
                // with t, the low-pass filter's time-constant
                // and dT, the event delivery rate
                final float alpha = 0.8f;

                acceleration = event.values;

                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                linear_acceleration[0] = event.values[0] - gravity[0];
                linear_acceleration[1] = event.values[1] - gravity[1];
                linear_acceleration[2] = event.values[2] - gravity[2];
                updateScreenOrientation();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnetic = event.values;
                updateScreenOrientation();
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public enum Axis {
        X(0),
        Y(1),
        Z(2);

        int id;

        Axis(int id) {
            this.id = id;
        }
    }
}
