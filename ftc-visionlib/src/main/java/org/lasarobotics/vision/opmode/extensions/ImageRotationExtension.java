package org.lasarobotics.vision.opmode.extensions;

import org.lasarobotics.vision.android.Sensors;
import org.lasarobotics.vision.image.Transform;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.opencv.core.Mat;

/**
 * Implements image rotation correction to ensure the cameraControl is facing the correct direction
 */
public class ImageRotationExtension implements VisionExtension {

    private final Sensors sensors = new Sensors();
    private ScreenOrientation defaultOrientation = ScreenOrientation.LANDSCAPE;
    private boolean isInverted = false;

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
     * Get the "zero" orientation
     * This is the rotation in which the program starts - likely this is either LANDSCAPE or PORTRAIT
     * You can set this with setDefaultOrientation()
     *
     * @return The "zero" orientation
     */
    private ScreenOrientation getDefaultOrientation() {
        return ScreenOrientation.getFromAngle((isInverted ? -1 : 1) * defaultOrientation.getAngle());
    }

    /**
     * Set the "zero" orientation
     * This is the rotation in which the program starts - likely this is either LANDSCAPE or PORTRAIT
     *
     * @param orientation Default or "zero" orientation
     */
    public void setDefaultOrientation(ScreenOrientation orientation) {
        this.defaultOrientation = orientation;
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

    private double getRotationCompensationAngleUnbiased() {
        return (isInverted ? -1 : 1) * sensors.getScreenOrientationCompensation();
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
        return (isInverted ? -1 : 1) * ScreenOrientation.getFromAngle(sensors.getScreenOrientationCompensation()
                - defaultOrientation.getAngle()).getAngle();
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
     * This may be necessary when using the inner cameraControl
     *
     * @param inverted True to rotate counterclockwise, false for clockwise
     */
    public void setRotationInversion(boolean inverted) {
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
        double angle = getDefaultOrientation().getAngle();
        if (angle != 0) {
            Transform.rotate(rgba, angle);
            opmode.width = rgba.width();
            opmode.height = rgba.height();
        }
        return rgba;
    }

    @Override
    public void stop(VisionOpMode opmode) {
        sensors.stop();
    }
}
