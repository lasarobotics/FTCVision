package org.lasarobotics.vision.opmode.extensions;

import org.lasarobotics.vision.android.Sensors;
import org.lasarobotics.vision.image.Transform;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.opencv.core.Mat;

/**
 * Implements image rotation correction to ensure the camera is facing the correct direction
 */
public class ImageRotationExtension implements VisionExtension {

    private final Sensors sensors = new Sensors();
    private ScreenOrientation unbiasedOrientation = ScreenOrientation.LANDSCAPE;
    private boolean isInverted = false;

    public ScreenOrientation getScreenOrientationDisplay() {
        return sensors.getActivityScreenOrientation();
    }

    private ScreenOrientation getUnbiasedOrientation() {
        return ScreenOrientation.getFromAngle((isInverted ? -1 : 1) * unbiasedOrientation.getAngle());
    }

    public void setUnbiasedOrientation(ScreenOrientation orientation) {
        this.unbiasedOrientation = orientation;
    }

    public ScreenOrientation getScreenOrientationActual() {
        return sensors.getScreenOrientation();
    }

    public double getRotationCompensationAngle() {
        return (isInverted ? -1 : 1) * sensors.getScreenOrientationCompensation();
    }

    public double getRotationCompensationAngleBiased() {
        return (isInverted ? -1 : 1) * ScreenOrientation.getFromAngle(sensors.getScreenOrientationCompensation()
                + unbiasedOrientation.getAngle()).getAngle();
    }

    public ScreenOrientation getRotationCompensation() {
        return ScreenOrientation.getFromAngle(getRotationCompensationAngle());
    }

    /**
     * Set whether the direction of rotation should be inverted.
     * This may be necessary when using the inner camera
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
        double angle = getUnbiasedOrientation().getAngle();
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
