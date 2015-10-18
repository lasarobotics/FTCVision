package org.lasarobotics.vision.detection;

import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.detection.objects.Ellipse;
import org.lasarobotics.vision.image.Filter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
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

    public void locateEllipses_hough(Mat grayImage, Mat output)
    {

    }

    public EllipseLocationResult locateEllipses_fit(Mat grayImage, Mat output)
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
