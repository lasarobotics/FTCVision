package org.lasarobotics.vision.detection;

import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

/**
 * Implements a single contour (MatOfPoint), with advanced measurement utilities
 */
public class Contour extends MatOfPoint {

    Contour(MatOfPoint data)
    {
        super(data);
    }

    public double area()
    {
        return Imgproc.contourArea(this);
    }

    public boolean isConvex()
    {
        return Imgproc.isContourConvex(this);
    }
}
