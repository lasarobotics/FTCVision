package org.lasarobotics.vision.opmode.extensions;

import org.lasarobotics.vision.opmode.VisionOpMode;
import org.opencv.core.Mat;

import android.content.res.Configuration;
import android.view.Display;
import android.view.WindowManager;

import com.qualcomm.ftccommon.configuration.FtcConfigurationActivity;

/**
 * Rotates image to work from all perspectives
 */
public class ImageRotationExtension implements VisionExtension {

    private int defualtConfig;

    public void init(VisionOpMode opMode) {

    }

    public Mat frame(VisionOpMode opmode, Mat rgba, Mat gray) {

        return rgba;
    }

    public void loop(VisionOpMode opMode) {

    }

    @Override
    public void stop(VisionOpMode opMode) {

    }
}
