package org.lasarobotics.vision.opmode.extensions;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.util.Log;

import org.lasarobotics.vision.android.Sensors;
import org.lasarobotics.vision.android.Util;
import org.lasarobotics.vision.image.Transform;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.opencv.core.Mat;

/**
 * Implements image rotation correction to ensure the camera is facing the correct direction
 */
public class ImageRotationExtension implements VisionExtension {

    private final Sensors sensors = new Sensors();
    private boolean isInverted = false;

    public void enableAutoRotate()
    {
        setAutoRotateState(true);
    }

    public void disableAutoRotate()
    {
        setAutoRotateState(false);
    }

    private void setAutoRotateState(boolean enabled)
    {
        Settings.System.putInt( Util.getContext().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
    }

    public void setActivityOrientationAutoRotate()
    {
        setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    public void setActivityOrientationFixed(ScreenOrientation orientation)
    {
        switch(orientation) {
            case LANDSCAPE:
            default:
                setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case PORTRAIT:
                setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case LANDSCAPE_REVERSE:
                setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case PORTRAIT_REVERSE:
                setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
        }
    }

    private void setActivityOrientation(int state)
    {
        try {
            Activity activity = Util.getActivity();
            activity.setRequestedOrientation(state);
        } catch (IllegalArgumentException e)
        {
            Log.e("ScreenOrientation", "Looks like screen orientation changed and the app crashed!\r\n" +
                    "It's likely you are using an incompatible Activity or a TestableVisionOpMode.\r\n" +
                    "Refrain from setting screen orientation settings to fix this issue.");
        }
    }

    /**
     * Get the screen orientation of the current activity
     * This is the native orientation of the display
     * Some rotations, including upside down, are natively disabled in Android
     * <p/>
     * Do not use this to figure out the current phone orientation as Android can lock the screen
     * orientation and therefore force your program into one orientation.
     *
     * @return Screen orientation as reported by the current activity
     */
    public ScreenOrientation getScreenOrientationDisplay() {
        return sensors.getActivityScreenOrientation();
    }

    /**
     * Get the screen orientation as returned by Android sensors
     * Use getRotationCompensationAngle() instead if you want to correct for the screen rotation
     * for purposes such as drawing to the screen.
     * If all you need is to figure out which way the phone is facing, you can use this method.
     * @return Screen orientation as reported by Android sensors
     */
    public ScreenOrientation getScreenOrientationActual() {
        return sensors.getScreenOrientation();
    }

    /**
     * Get rotation compensation angle as reported by fusing data from the Android sensors and
     * the native Android drawing API. Use this when you need to figure out which way you need to
     * draw onto the screen.
     * This is a compensation between the activity orientation and the actual phone orientation.
     * If you need to get the actual phone orientation alone then use getScreenOrientationActual().
     *
     * @return Fused angle compensating for the difference between the actual orientation and the
     * Android API drawing orientation.
     */
    public double getRotationCompensationAngle() {
        return (isInverted ? -1 : 1) * ScreenOrientation.getFromAngle(sensors.getScreenOrientationCompensation()).getAngle();
    }

    private double getRotationCompensationAngleUnbiased() {
        return (isInverted ? -1 : 1) * sensors.getScreenOrientationCompensation();
    }

    /**
     * Get rotation compensation as reported by fusing data from the Android sensors and
     * the native Android drawing API. Use this when you need to figure out which way you need to
     * draw onto the screen.
     * This is a compensation between the activity orientation and the actual phone orientation.
     * If you need to get the actual phone orientation alone then use getScreenOrientationActual().
     * @return Fused orientation compensating for the difference between the actual orientation and the
     * Android API drawing orientation.
     */
    public ScreenOrientation getRotationCompensation() {
        return ScreenOrientation.getFromAngle(getRotationCompensationAngleUnbiased());
    }

    /**
     * Set whether the direction of rotation should be inverted.
     * This may be necessary when using the inner camera
     *
     * @param inverted True to rotate counterclockwise, false for clockwise
     */
    public void setIsUsingSecondaryCamera(boolean inverted) {
        isInverted = inverted;
    }

    /**
     * Returns whether rotation is inverted
     *
     * @return True is rotating counterclockwise, false otherwise
     */
    public boolean isRotationInverted() {
        return isInverted;
    }


    @Override
    public void init(VisionOpMode opmode) {
        sensors.resume();
    }

    @Override
    public void loop(VisionOpMode opmode) {

    }

    @Override
    public Mat frame(VisionOpMode opmode, Mat rgba, Mat gray) {
        if (isInverted)
            Transform.flip(rgba, Transform.FlipType.FLIP_ACROSS_X);
        return rgba;
    }

    @Override
    public void stop(VisionOpMode opmode) {
        sensors.stop();
    }
}
