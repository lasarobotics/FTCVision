package org.lasarobotics.vision.detection;

import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.detection.objects.Ellipse;
import org.lasarobotics.vision.image.Filter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements primitive (ellipse, rectangle, etc.) detection, based on a custom, highly-robust version of the Hough transform
 */
public class PrimitiveDetection {
    public PrimitiveDetection()
    {

    }

    public void locateRectangles(Mat gray, Mat output)
    {

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

    public List<Ellipse> locateEllipses_houghCircles(Mat grayImage, Mat output)
    {
        Filter.blur(grayImage, 2);

        Mat circles = new Mat();
        Imgproc.HoughCircles(grayImage, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 10, 100, 30, 1, 100);
        List<Ellipse> ellipses = new ArrayList<>();
        for(int i=0; i<circles.rows(); i++)
        {
            double x = circles.get(i, 0)[0];
            double y = circles.get(i, 0)[1];
            double radius = circles.get(i, 0)[2];

            RotatedRect r = new RotatedRect(new Point(x, y), new Size(radius, radius), 0);
            ellipses.add(new Ellipse(r));
        }
        return ellipses;
    }

    public EllipseLocationResult locateEllipses_fit(Mat grayImage)
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
        Imgproc.findContours(gray, contoursTemp, cacheHierarchy, Imgproc.CV_RETR_CCOMP, Imgproc.CHAIN_APPROX_TC89_KCOS);

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
