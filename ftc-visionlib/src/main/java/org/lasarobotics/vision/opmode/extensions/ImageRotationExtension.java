package org.lasarobotics.vision.opmode.extensions;

import org.lasarobotics.vision.test.android.Util;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Rotates image to work from all perspectives
 */
public class ImageRotationExtension {

    public static final int DESIRED_ORIENTATION = Configuration.ORIENTATION_LANDSCAPE;

    private int defaultOrientation;

    public void init() {
        defaultOrientation = getDeviceDefaultOrientation();
    }

    public Mat rotateImage(Mat img) {
        int rotation = getRotation();
        double angle = 0;
        Mat rotMatrix = Imgproc.getRotationMatrix2D(new Point(img.rows()/2, img.cols()/2), angle, 1);
        img.mul(rotMatrix);
        return img;
    }

    private int getRotation() {
        Context context = Util.getContext();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay().getRotation();
    }

    private int getDeviceDefaultOrientation() {

        Context context = Util.getContext();

        WindowManager windowManager =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Configuration config = context.getResources().getConfiguration();

        int rotation = getRotation();

        if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT))
            return Configuration.ORIENTATION_LANDSCAPE;
        else
            return Configuration.ORIENTATION_PORTRAIT;
    }
}
