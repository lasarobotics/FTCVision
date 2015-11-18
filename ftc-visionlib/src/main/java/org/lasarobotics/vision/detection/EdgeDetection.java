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
    public static final double SLOPE_THRESHOLD = 0.5;
    public static final double SLOPE_ABS_RANGE = 1000;

    public EdgeDetection()
    {

    }

    public List<Contour> getBreaks(Mat grayImage, Mat img)
    {
        //Manipulate image in order to get best canny results
        Mat gray = grayImage.clone();

        Filter.blur(gray, 1);
        Filter.erode(gray, 1);
        Filter.dilate(gray, 1);

        //Convert grayscale image to Canny Image. Canny(Input, Output, LowThresh, HighTresh, KernelSize)
        Imgproc.Canny(gray, gray, 5, 75, 3, true);
        Filter.blur(gray, 0);

        ArrayList<Line> lines = combineLines(getLines(gray), img.rows());
        //List<Contour> rectangles = getRects(gray);
        //DEBUG
        for(int i = 0; i < lines.size(); i++) {
            Drawing.drawLine(img, lines.get(i).getStartPoint(), lines.get(i).getEndPoint(), new ColorRGBA("#FFFF00"));
        }

        //return filterContours(lines, rectangles);
        return null;
    }

    public ArrayList<Line> combineLines(ArrayList<Line> lines, int rows)
    {
        ArrayList<Line> resultLines = new ArrayList<>();
        if(lines.size() > 0) {
            Collections.sort(lines, Line.InterceptCompare);
            Line startLine = lines.get(0);
            Point endPoint = lines.get(0).getEndPoint();
            for (int i = 0; i < lines.size(); i += 2) {
                if (i == 0)
                    continue;
                if (Line.slopeInterceptCompare(startLine, lines.get(i), SLOPE_THRESHOLD))
                    endPoint = lines.get(i).getEndPoint();
                else {
                    if (Line.slopeInterceptCompare(startLine, lines.get(i - 1), SLOPE_THRESHOLD)) {
                        resultLines.add(new Line(startLine.getStartPoint(), lines.get(i - 1).getEndPoint()));
                        startLine = lines.get(i);
                        endPoint = startLine.getEndPoint();
                    } else {
                        resultLines.add(new Line(startLine.getStartPoint(), endPoint));
                        startLine = lines.get(i - 1);
                        endPoint = startLine.getEndPoint();
                        i -= 2;

                    }
                }
            }
        }
        return resultLines;
    }

    //Filters out all rectangles/contours that do not contain parallel lines going through it
    /*
    private List<Contour> filterContours(ArrayList<Line> lines, List<Contour> contours) {
        for (int i = 0; i < contours.size(); i++) {
            //Get the contours corner points and create the top line and bottom line of the contour
            Point[] cornerPoints = getCornerPoints(contours.get(i).getPoints());
            Line topLine = new Line(cornerPoints[1], cornerPoints[3]);
            Line bottomLine = new Line(cornerPoints[0], cornerPoints[2]);

            //If the top and bottom lines of the quadrilateral arent approx. parallel, its not the beacon
            if(!sameSlope(topLine, bottomLine.getSlope()))
            {
                contours.remove(i);
                continue;
            }

            //Get the height of the contour
            double rectHeight = Math.sqrt(Math.pow(topLine.getStartPoint().x - bottomLine.getStartPoint().x, 2)
                    + Math.pow(topLine.getStartPoint().y - bottomLine.getStartPoint().y, 2));

            //Get the intercepts of the top line and bottom line, for a line to be passing through
            //this contour it must have an intercept inbetween these two, as well as a similar slope
            //as the top and bottom
            double maxIntercept = topLine.getYIntercept();
            double minIntercept = bottomLine.getYIntercept();
            double avgSlope = (topLine.getSlope() + bottomLine.getSlope()) / 2.0;

            //Find all the lines that have approx the same slope as avgSlope and have intercepts
            //inbetween the min and max intercepts. The 2D array stores the lines such that the
            //First Array = lines left of contour, Second Array = lines right of contour
            ArrayList<ArrayList<Line>> possibleLines = findLines(maxIntercept, minIntercept, avgSlope, lines,
                    Math.min(topLine.getStartPoint().x, bottomLine.getStartPoint().x),
                    Math.min(topLine.getEndPoint().x, bottomLine.getEndPoint().x));
            //If there are under 4 possible lines crossing through the contour its not the beacon
            //If there is less than 2 possible lines on either side of the contour, its not the beacon
            if (possibleLines.get(0).size() + possibleLines.get(1).size() < 4 || possibleLines.get(0).size() < 1
                    || possibleLines.get(1).size() < 1)
            {
                contours.remove(i);
                continue;
            }

            //If all previous conditions have been met, check if the height ratio of the contour to
            //the height of the parallel lines meets the specified ratio of the real beacon to the real
            //wall
            if (!meetsBreakRatio(possibleLines.get(0), rectHeight, bottomLine))
                contours.remove(i);
        }
        //Return filtered contours
        return contours;
    }


    private boolean meetsBreakRatio(ArrayList<Line> lines, double rectHeight, Line bottomLine)
    {
        for(int i = 0; i < lines.size(); i++)
        {
            if(Math.abs(rectHeight/bottomLine.distFormula(new Point(50, lines.get(i).evaluateX(50)), new Point(50,
                    bottomLine.evaluateX(50))) - WALL_BEACON_HEIGHT_RATIO) <= HEIGHT_RATIO_THRESHOLD)
                return true;
        }
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
    */

    //Checks if two lines are considered the same slope given a set tolerance
    public boolean sameSlope(Line line, double slope)
    {
        return Math.abs(line.getSlope() - slope) <= SLOPE_THRESHOLD;
    }

    //Return the set of all lines in a canny image using probalistic hough line transform
    private ArrayList<Line> getLines(Mat canny)
    {
        Mat lineMat = new Mat();
        //Use Hough Lines Transform to identify lines in frame with the following characteristics
        int threshold = 100;
        int minLineSize = 50;
        int lineGap = 10;
        Imgproc.HoughLinesP(canny, lineMat, 1, Math.PI / 180, threshold, minLineSize, lineGap);
        //Create a list of lines detected
        ArrayList<Line> lines = new ArrayList<>();
        for(int i = 0; i < lineMat.rows(); i++)
        {
            double[] vec = lineMat.get(0, i);
            Line line = new Line(new Point(vec[0], vec[1]), new Point(vec[2], vec[3]));
            if(Math.abs(line.getSlope()) < SLOPE_ABS_RANGE)
                lines.add(line);
        }
        return lines;
    }
    /*
    private List<Contour> getRects(Mat canny)
    {
        return null;
    }
    */
}