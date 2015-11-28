package org.lasarobotics.vision.test.opmode.extensions;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Surface;
import android.view.WindowManager;

import org.lasarobotics.vision.test.android.Sensors;
import org.lasarobotics.vision.test.image.Transform;
import org.lasarobotics.vision.test.opmode.VisionOpMode;
import org.lasarobotics.vision.test.util.ScreenOrientation;
import org.opencv.core.Mat;

/**
 * Implements image rotation correction to ensure the camera is facing the correct direction
 */
public class ImageRotationExtension implements VisionExtension {

    private Sensors sensors = new Sensors();
    public ScreenOrientation getScreenOrientationDisplay() {
        return sensors.getActivityScreenOrientation();
    }
    public ScreenOrientation getScreenOrientationActual() {
        return sensors.getScreenOrientation();
    }
    public double getRotationAngle()
    {
        return sensors.getScreenOrientationCompensation();
    }

    @Override
    public void init(VisionOpMode opmode) {

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

    }
}
