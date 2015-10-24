package org.lasarobotics.vision.detection;

import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.detection.objects.Ellipse;
import org.lasarobotics.vision.detection.objects.Rectangle;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.image.Filter;
import org.lasarobotics.vision.util.MathUtil;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements primitive (ellipse, polygon) detection based on a custom, highly-robust (size and position invariant) version of the Hough transform
 */
public class PrimitiveDetection {

    private static final int THRESHOLD_STEPS = 5;
    private static final int THRESHOLD_CANNY = 75;
    private static final double MAX_COSINE_VALUE = 0.3;

    //TODO convert this to locatePolygons() with n sides
    //TODO http://opencv-code.com/tutorials/detecting-simple-shapes-in-an-image/
    public List<Rectangle> locateRectangles(Mat grayImage, Mat output) {
        Mat gray = grayImage.clone();

        //Filter out some noise
        Filter.downsample(gray, 2);
        Filter.upsample(gray, 2);

        Mat cacheHierarchy = new Mat();
        Mat grayTemp = new Mat();
        List<Rectangle> rectangles = new ArrayList<>();

        for (int step = 0; step < THRESHOLD_STEPS; step++) {

            //Filter the threshold a bit
            if (step == 0)
            {
                //Use Canny at zero-threshold to help with gradient shading
                //TODO not sure about the last parameter
                Imgproc.Canny(gray, grayTemp, 0, THRESHOLD_CANNY, 3, true);

                //Remove potential holes between segments
                Filter.dilate(grayTemp, 0);
            }
            else
            {
                //Apply threshold
                Imgproc.threshold(gray, grayTemp, (step+1)*255/THRESHOLD_STEPS, 255, Imgproc.THRESH_TRUNC);
            }

            List<MatOfPoint> contoursTemp = new ArrayList<>();
            //Find contours - the parameters here are very important to compression and retention
            Imgproc.findContours(grayTemp, contoursTemp, cacheHierarchy, Imgproc.CV_RETR_TREE, Imgproc.CHAIN_APPROX_TC89_KCOS);
            //DEBUG
            Imgproc.drawContours(output, contoursTemp, -1, new ColorRGBA("#9E9E9E").getScalarRGBA(), 1);

            //For each contour, test whether the contour is a rectangle
            //List<Contour> contours = new ArrayList<>();
            MatOfPoint2f approx = new MatOfPoint2f();
            for (MatOfPoint co : contoursTemp) {
                //contours.add(new Contour(co));
                MatOfPoint2f matOfPoint2f = new MatOfPoint2f(co.toArray());
                Contour c = new Contour(co);

                //Attempt to fit the contour to the best polygon
                Imgproc.approxPolyDP(matOfPoint2f, approx,
                        c.arcLength(true) * 0.02, true);

                Contour approxContour = new Contour(approx);

                //Make sure the contour is big enough, CLOSED (convex), and has exactly 4 points

                if (approx.toArray().length > 4 && approx.toArray().length <= 12
                        && Math.abs(approxContour.area()) > 1000 &&
                        approxContour.isConvex())
                {
                    double maxCosine = 0;

                    for (int j = 2; j < approx.toArray().length + 1; j++) {
                        double cosine = Math.abs(MathUtil.angle(approx.toArray()[j % approx.toArray().length],
                                approx.toArray()[j - 2], approx.toArray()[j - 1]));
                        maxCosine = Math.max(maxCosine, cosine);
                    }

                    if (maxCosine < 20) {
                        //DEBUG
                        Drawing.drawContour(output, approxContour, new ColorRGBA("#FFFF00"), 1);
                    }
                }

                if (approx.toArray().length == 4 &&
                        Math.abs(approxContour.area()) > 1000 &&
                        approxContour.isConvex()) {
                    double maxCosine = 0;

                    //DEBUG
                    Drawing.drawContour(output, approxContour, new ColorRGBA("#FF9100"), 2);

                    //Check each angle to be approximately 90 degrees
                    for (int j = 2; j < 5; j++) {
                        double cosine = Math.abs(MathUtil.angle(approx.toArray()[j % 4],
                                approx.toArray()[j - 2], approx.toArray()[j - 1]));
                        maxCosine = Math.max(maxCosine, cosine);
                    }

                    if (maxCosine < MAX_COSINE_VALUE) {
                        //Convert the points to a rectangle instance
                        rectangles.add(new Rectangle(approx.toArray()));
                    }
                }
            }
        }

        //DEBUG
        Drawing.drawRectangles(output, rectangles, new ColorRGBA("#00FF00"), 3);
        return rectangles;
    }

    public class EllipseLocationResult
    {
        List<Contour> contours;
        List<Ellipse> ellipses;

        EllipseLocationResult(List<Contour> contours, List<Ellipse> ellipses)
        {
            this.contours = contours;
            this.ellipses = ellipses;
        }

        public List<Contour> getContours()
        {
            return contours;
        }

        public List<Ellipse> getEllipses()
        {
            return ellipses;
        }
    }

    public EllipseLocationResult locateEllipses(Mat grayImage)
    {
        Mat gray = grayImage.clone();

        Filter.blur(gray, 1);
        Filter.erode(gray, 1);
        Filter.dilate(gray, 1);

        Imgproc.Canny(gray, gray, 5, 75, 3, true);
        Filter.blur(gray, 0);

        Mat cacheHierarchy = new Mat();

        List<MatOfPoint> contoursTemp = new ArrayList<>();
        //Find contours - the parameters here are very important to compression and retention
        Imgproc.findContours(gray, contoursTemp, cacheHierarchy, Imgproc.CV_RETR_TREE, Imgproc.CHAIN_APPROX_TC89_KCOS);

        //List and draw contours
        List<Contour> contours = new ArrayList<>();
        for (MatOfPoint co : contoursTemp ) {
            contours.add(new Contour(co));
        }

        //Find ellipses by finding fit
        List<Ellipse> ellipses = new ArrayList<>();
        for (MatOfPoint co : contoursTemp ) {
            contours.add(new Contour(co));
            //Contour must have at least 6 points for fitEllipse
            if (co.toArray().length < 6)
                continue;
            //Copy MatOfPoint to MatOfPoint2f
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(co.toArray());
            //Fit an ellipse to the current contour
            Ellipse ellipse = new Ellipse(Imgproc.fitEllipse(matOfPoint2f));
            //Test eccentricity of ellipse, if it's too eccentric, ignore it
            if (ellipse.eccentricity() > 0.5)
                continue;

            //Draw ellipse
            ellipses.add(ellipse);
        }

        return new EllipseLocationResult(contours, ellipses);
    }
}
