package org.lasarobotics.vision.detection;

import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.util.color.Color;
import org.lasarobotics.vision.util.color.ColorHSV;
import org.lasarobotics.vision.util.color.ColorSpace;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements blob (regional) detection based on color
 */
public class ColorBlobDetector2 {

    //Lower bound for range checking
    private ColorHSV lowerBound = new ColorHSV(0, 0, 0);
    //Upper bound for range checking
    private ColorHSV upperBound = new ColorHSV(0, 0, 0);
    // Minimum contour area in percent for contours filtering
    private static double minContourArea = 0.1;
    //Color radius for range checking
    private Scalar colorRadius = new Scalar(25, 50, 50, 0);
    //Currently selected color
    private Color color;

    private List<MatOfPoint> contours = new ArrayList<>();

    public ColorBlobDetector2(Color color)
    {
        setColor(color);
    }
    public ColorBlobDetector2(Color color, Scalar colorRadius)
    {
        this.colorRadius = colorRadius;
        setColor(color);
    }

    public void setColor(Color color)
    {
        if (color == null)
            throw new IllegalArgumentException("Color must not be null!");

        this.color = color;
        Scalar hsvColor = color.convertColorScalar(ColorSpace.HSV);

        //calculate min and max hues
        double minH = (hsvColor.val[0] >= colorRadius.val[0]) ? hsvColor.val[0]-colorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+colorRadius.val[0] <= 255) ? hsvColor.val[0]+colorRadius.val[0] : 255;

        Scalar lowerBoundScalar = lowerBound.getScalar();
        Scalar upperBoundScalar = upperBound.getScalar();

        lowerBoundScalar.val[0] = minH;
        upperBoundScalar.val[0] = maxH;

        lowerBoundScalar.val[1] = hsvColor.val[1] - colorRadius.val[1];
        upperBoundScalar.val[1] = hsvColor.val[1] + colorRadius.val[1];

        lowerBoundScalar.val[2] = hsvColor.val[2] - colorRadius.val[2];
        upperBoundScalar.val[2] = hsvColor.val[2] + colorRadius.val[2];

        lowerBoundScalar.val[3] = 0;
        upperBoundScalar.val[3] = 255;

        lowerBound = new ColorHSV(lowerBoundScalar);
        upperBound = new ColorHSV(upperBoundScalar);
    }

    public void setColorRadius(Scalar radius) {
        this.colorRadius = radius;
        //Update the bounds again
        setColor(color);
    }

    public void setMinContourArea(double area) {
        minContourArea = area;
        //Update the bounds again
        setColor(color);
    }

    public void process(Mat rgbaImage) {
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        //mHsvMat = Color.convertColorMat(mPyrDownMat, ColorSpace.RGBA, ColorSpace.HSV);
        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHsvMat, lowerBound.getScalar(), upperBound.getScalar(), mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        contours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > minContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);
                contours.add(contour);
            }
        }

        this.contours = contours;
    }

    public void drawContours(Mat img, Color color)
    {
        Drawing.drawContours(img, contours, color);
    }
    public void drawContours(Mat img, Color color, int thickness)
    {
        Drawing.drawContours(img, contours, color, thickness);
    }

    public List<MatOfPoint> getContours() {
        return contours;
    }

    // Cache
    private Mat mPyrDownMat = new Mat();
    private Mat mHsvMat = new Mat();
    private Mat mMask = new Mat();
    private Mat mDilatedMask = new Mat();
    private Mat mHierarchy = new Mat();
}
