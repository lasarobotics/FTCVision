package org.lasarobotics.vision.detection;

import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.detection.objects.Line;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.image.Filter;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements Canny edge detection in order to find breaks in the course wall.
 * NEW ALGORITHM:
 * 1. Find rectangles in image.
 * 2. Find lines in image.
 * 3. Return rectangle contours that have parallel lines passing through them.
 */
public class EdgeDetection {

    //TODO find the ratio of the wall's top edge height to the beacons height
    public static final double WALL_BEACON_HEIGHT_RATIO = 0.5;
    public static final double RECT_RATIO_THRESHOLD = 0.15;
    public static final double SLOPE_THRESHOLD = 0.125;

    public EdgeDetection()
    {

    }

    public List<Contour> getBreaks(Mat grayImage)
    {
        Mat gray = grayImage.clone();

        Filter.blur(gray, 1);
        Filter.erode(gray, 1);
        Filter.dilate(gray, 1);
        //TODO Establish lowTreshold and highThreshold
        //Convert grayscale image to Canny Image. Canny(Input, Output, LowThresh, HighTresh, KernelSize)
        Imgproc.Canny(gray, gray, 5, 75, 3, true);
        Filter.blur(gray, 0);

        ArrayList<Line> lines = getLines(gray);
        List<Contour> rectangles = getRects(gray);
        return filterContours(lines, rectangles);
    }

    public List<Contour> filterContours(ArrayList<Line> lines, List<Contour> contours)
    {
        for(int i = 0; i < contours.size(); i++)
        {
            Point[] cornerPoints = sortPoints(contours.get(i).getPoints());
            double maxIntercept = (((double)cornerPoints[1].y - cornerPoints[3].y)/
                    (cornerPoints[1].x - cornerPoints[3].x) * -cornerPoints[1].x + cornerPoints[1].y);
            double minIntercept = (((double)cornerPoints[0].y - cornerPoints[2].y)/
                    (cornerPoints[1].x - cornerPoints[2].x) * -cornerPoints[0].x + cornerPoints[0].y);

        }
        return null;
    }

    //Point[0] = bottomLeft, Point[1] = topLeft, Point[2] = bottomRight, Point[3] = topRight
    private Point[] sortPoints(Point[] points)
    {
        Point[] results = new Point[4];

        int[] leftMostIndex = new int[]{0, 1};
        for(int i = 2; i < 4; i++)
        {
            if(points[i].x < points[leftMostIndex[0]].x)
                leftMostIndex[0] = i;
            else if(points[i].x < points[leftMostIndex[1]].x)
                leftMostIndex[1] = i;
        }
        if(points[leftMostIndex[0]].y < points[leftMostIndex[1]].y)
        {
            results[0] = points[leftMostIndex[0]];
            results[1] = points[leftMostIndex[1]];
        }
        else
        {
            results[0] = points[leftMostIndex[1]];
            results[1] = points[leftMostIndex[0]];
        }

        int[] rightMostIndex = new int[]{-1, -1};
        for(int i = 0; i < 4; i++)
        {
            if(!(i == leftMostIndex[0] || i == leftMostIndex[1]))
            {
                if(rightMostIndex[0] == -1)
                    rightMostIndex[0] = i;
                else
                    rightMostIndex[1] = i;
            }
        }
        if(points[rightMostIndex[0]].y < points[rightMostIndex[1]].y)
        {
            results[2] = points[rightMostIndex[0]];
            results[3] = points[leftMostIndex[1]];
        }
        else
        {
            results[2] = points[leftMostIndex[1]];
            results[3] = points[leftMostIndex[0]];
        }

        return results;
    }

    private ArrayList<Line> getLines(Mat canny)
    {
        Mat lineMat = new Mat();
        //Use Hough Lines Transform to identify lines in frame with the following characteristics
        int threshold = 100;
        int minLineSize = 100;
        int lineGap = 10;
        Imgproc.HoughLinesP(canny, lineMat, 1, Math.PI / 180, threshold, minLineSize, lineGap);
        //Create a list of lines detected
        ArrayList<Line> lines = new ArrayList<>();
        for(int i = 0; i < lineMat.cols(); i++)
        {
            double[] vec = lineMat.get(0, i);
            lines.add(new Line(new Point(vec[0], vec[1]), new Point(vec[2], vec[3])));
        }
        return lines;
    }
    private List<Contour> getRects(Mat canny)
    {
        Mat cacheHierarchy = new Mat();
        List<MatOfPoint> contoursTemp = new ArrayList<>();
        //Find contours - Copied from PrimitiveDetection
        Imgproc.findContours(canny, contoursTemp, cacheHierarchy, Imgproc.CV_RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //Instantiate contours
        List<Contour> contours = new ArrayList<>();
        for (MatOfPoint co : contoursTemp ) {
            contours.add(new Contour(co));
        }

        //If the percent difference in the contour area and the estimated rect area is greater than
        //threshold, remove contour
        //TODO create better method for getting only rects ??
        for(int i = 0; i < contours.size(); i++)
        {
            if(contours.get(i).getPoints().length >= 4)
                contours.remove(i);
        }

        return contours;
    }
}