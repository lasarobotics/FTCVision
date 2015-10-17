package org.lasarobotics.vision.detection;

import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.detection.objects.Ellipse;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.image.Filter;
import org.lasarobotics.vision.util.color.ColorGRAY;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements ellipse detection, based on a custom, highly-robust version of the Hough transform
 */
public class EllipseDetection {
    public EllipseDetection()
    {

    }

    public void computeEllipses(Mat gray, Mat rgba)
    {
        Filter.blur(gray, 1);
        Filter.erode(gray, 2);
        Filter.dilate(gray, 2);

        Imgproc.Canny(gray, gray, 5, 100, 3, true);
        //Filter.blur(gray, 3);

        Mat cacheHierarchy = new Mat();

        List<MatOfPoint> contoursTemp = new ArrayList<>();
        Imgproc.findContours(gray, contoursTemp, cacheHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_KCOS);
        List<Contour> contours = new ArrayList<>();
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
            Drawing.drawEllipse(gray, ellipse, new ColorGRAY(255), 2);
        }
        Drawing.drawContours(gray, contours, new ColorGRAY(127), 1);
    }
}
