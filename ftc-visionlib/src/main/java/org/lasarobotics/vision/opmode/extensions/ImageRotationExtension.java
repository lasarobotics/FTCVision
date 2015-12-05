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

    private Sensors sensors = new Sensors();
    private ScreenOrientation unbiasedOrientation = ScreenOrientation.LANDSCAPE;
    public ScreenOrientation getScreenOrientationDisplay() {
        return sensors.getActivityScreenOrientation();
    }
    public void setUnbiasedOrientation(ScreenOrientation orientation)
    {
        this.unbiasedOrientation = orientation;
    }
    public ScreenOrientation getScreenOrientationActual() {
        return sensors.getScreenOrientation();
    }
    public double getRotationAngle()
    {
        return ScreenOrientation.getFromAngle(sensors.getScreenOrientationCompensation() +
                unbiasedOrientation.getAngle()).getAngle();
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
        double angle = getRotationAngle();
        Transform.rotate(rgba, angle);
        opmode.width = rgba.width();
        opmode.height = rgba.height();
        return rgba;
    }

    @Override
    public void stop(VisionOpMode opmode) {
        sensors.stop();
    }
}
