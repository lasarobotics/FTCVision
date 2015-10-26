package org.lasarobotics.vision.detection;

import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.detection.objects.Line;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.image.Filter;
import org.lasarobotics.vision.util.MathUtil;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
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
    public static final double WALL_BEACON_HEIGHT_RATIO = 1.5;
    public static final double HEIGHT_RATIO_THRESHOLD = 0.1;
    public static final double LENGTH_DIFF_THRESHOLD = 0.10;
    public static final double SLOPE_THRESHOLD = 0.125;

    public EdgeDetection() {

    }

    public List<Contour> getBreaks(Mat grayImage) {
        Mat gray = grayImage.clone();

        Filter.blur(gray, 1);
        Filter.erode(gray, 1);
        Filter.dilate(gray, 1);

        //Convert grayscale image to Canny Image. Canny(Input, Output, LowThresh, HighTresh, KernelSize)
        Imgproc.Canny(gray, gray, 5, 75, 3, true);
        Filter.blur(gray, 0);

        ArrayList<Line> lines = getLines(gray);
        List<Contour> rectangles = getRects(gray);
        return filterContours(lines, rectangles);
    }

    private List<Contour> filterContours(ArrayList<Line> lines, List<Contour> contours) {
        for (int i = 0; i < contours.size(); i++) {
            Point[] cornerPoints = sortPoints(contours.get(i).getPoints());
            Line topLine = new Line(cornerPoints[1], cornerPoints[3]);
            Line bottomLine = new Line(cornerPoints[0], cornerPoints[2]);

            double rectHeight = Math.sqrt(Math.pow(topLine.getStartPoint().x - bottomLine.getStartPoint().x, 2)
                    + Math.pow(topLine.getStartPoint().y - bottomLine.getStartPoint().y, 2));

            double maxIntercept = topLine.getYIntercept();
            double minIntercept = bottomLine.getYIntercept();
            double avgSlope = (topLine.getSlope() + bottomLine.getSlope()) / 2.0;

            //First Array = lines left of contour, Second Array = lines right of contour
            ArrayList<ArrayList<Line>> possibleLines = findLines(maxIntercept, minIntercept, avgSlope, lines,
                    Math.min(topLine.getStartPoint().x, bottomLine.getStartPoint().x),
                    Math.min(topLine.getEndPoint().x, bottomLine.getEndPoint().x));
            if (possibleLines.get(0).size() + possibleLines.get(1).size() < 4 || possibleLines.get(0).size() < 2
                    || possibleLines.get(1).size() < 2)
            {
                contours.remove(i);
                continue;
            }

            if (!meetsBreakRatio(possibleLines, rectHeight))
                contours.remove(i);
        }
        return contours;
    }

    private boolean meetsBreakRatio(ArrayList<ArrayList<Line>> lines, double rectHeight)
    {
        //TODO add extra check to insure we get best two lines
        ArrayList<Line> leftTwoLines = new ArrayList<>();
        Collections.sort(lines.get(0), Line.LengthCompare);
        boolean lastHadPair = hasPair(lines.get(0).get(lines.get(0).size() - 1), lines.get(0));
        for(int i = lines.get(0).size() - 2; i >= 0; i--)
        {
            boolean thisHasPair = hasPair(lines.get(0).get(i), lines.get(0));
            if(lines.get(0).get(i + 1).getLength()/lines.get(0).get(i).getLength() - 1 <= LENGTH_DIFF_THRESHOLD
                    && lastHadPair && thisHasPair)
            {
                leftTwoLines.add(lines.get(0).get(i + 1));
                leftTwoLines.add(lines.get(0).get(i));
                break;
            }
            else
            {
                lastHadPair = thisHasPair;
            }
        }
        if(leftTwoLines.isEmpty())
            return false;
        if(Math.max(leftTwoLines.get(0).getStartPoint().x, leftTwoLines.get(1).getStartPoint().x) <
                Math.min(leftTwoLines.get(0).getEndPoint().x, leftTwoLines.get(1).getEndPoint().x))
        {
            double x = Math.max(leftTwoLines.get(0).getStartPoint().x, leftTwoLines.get(1).getStartPoint().x);
            double lineHeight = Math.cos(Math.atan((leftTwoLines.get(0).getSlope() + leftTwoLines.get(1).getSlope()) /
                2))*Math.sqrt(Math.pow(leftTwoLines.get(0).evaluateX(x) - leftTwoLines.get(1).evaluateX(x), 2));
            if(Math.abs(WALL_BEACON_HEIGHT_RATIO - rectHeight/lineHeight) <= HEIGHT_RATIO_THRESHOLD)
                return true;
        }
        else
            return false;
        return false;
    }

    private boolean hasPair(Line line, ArrayList<Line> lines)
    {
        boolean hasPair = false;
        for(Line li : lines)
        {
            if(sameSlope(new Line(line.getStartPoint(), li.getStartPoint()), line.getSlope()))
                hasPair = true;
        }
        return hasPair;
    }

    private ArrayList<ArrayList<Line>> findLines(double maxYIntercept, double minYIntercept, double slope,
                                                ArrayList<Line> lines, double leftVal, double rightVal)
    {
        ArrayList<ArrayList<Line>> result = new ArrayList<>();
        for(Line li : lines)
        {
            if(MathUtil.inBounds(minYIntercept, maxYIntercept, li.getYIntercept()) && sameSlope(li, slope))
            {
                if(li.getEndPoint().x <= leftVal)
                {
                    result.get(0).add(li);
                }
                else if(li.getStartPoint().x >= rightVal)
                {
                    result.get(1).add(li);
                }
            }
        }
        return result;
    }

    public boolean sameSlope(Line line, double slope)
    {
        return Math.abs(line.getSlope() - slope) <= SLOPE_THRESHOLD;
    }

    //Point[0] = bottomLeft, Point[1] = topLeft, Point[2] = bottomRight, Point[3] = topRight
    private Point[] sortPoints(Point[] points)
    {
        //TODO return an array of the rects corner points matching the above listed sortment
        return null;
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
        //TODO fix rectangle detection
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
        for(int i = 0; i < contours.size(); i++)
        {
            if(contours.get(i).getPoints().length >= 4)
                contours.remove(i);
        }

        return contours;
    }
}