package org.lasarobotics.vision.ftc.resq;

import org.lasarobotics.vision.detection.Contour;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.util.color.ColorGRAY;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.List;

/**
 * Beacon location and analysis
 */
public final class Beacon {

    public enum BeaconColor
    {
        RED,
        BLUE,
        RED_BRIGHT,
        BLUE_BRIGHT,
        UNKNOWN
    }

    public static class BeaconColorAnalysis
    {
        BeaconColor left;
        BeaconColor right;

        //TODO Color and CONFIDENCE should make up the results

        //TODO add Size size, Point locationTopLeft, Distance distanceApprox
        BeaconColorAnalysis(BeaconColor left, BeaconColor right)
        {
            this.left = left;
            this.right = right;
        }

        public BeaconColor getStateLeft()
        {
            return left;
        }

        public BeaconColor getStateRight()
        {
            return right;
        }

        public boolean isLeftKnown()
        {
            return left != BeaconColor.UNKNOWN;
        }
        public boolean isLeftBlue()
        {
            return (left == BeaconColor.BLUE_BRIGHT || left == BeaconColor.BLUE);
        }
        public boolean isLeftRed()
        {
            return (left == BeaconColor.RED_BRIGHT || left == BeaconColor.RED);
        }
        public boolean isRightKnown()
        {
            return right != BeaconColor.UNKNOWN;
        }
        public boolean isRightBlue()
        {
            return (right == BeaconColor.BLUE_BRIGHT || right == BeaconColor.BLUE);
        }
        public boolean isRightRed()
        {
            return (right == BeaconColor.RED_BRIGHT || right == BeaconColor.RED);
        }
        public boolean isBeaconFound()
        {
            return isLeftKnown() && isRightKnown();
        }
        public boolean isBeaconFullyLit()
        {
            return (left == BeaconColor.BLUE_BRIGHT || left == BeaconColor.RED_BRIGHT ) &&
                    (right == BeaconColor.BLUE_BRIGHT || right == BeaconColor.RED_BRIGHT );
        }
    }

    public static BeaconColorAnalysis analyzeColor(List<Contour> contoursRed, List<Contour> contoursBlue, Mat img)
    {
        //Get the largest contour in each - we're calling this one the main light
        int largestIndexRed = findLargestIndex(contoursRed);
        int largestIndexBlue = findLargestIndex(contoursBlue);
        Contour largestRed = (largestIndexRed != -1) ? contoursRed.get(largestIndexRed) : null;
        Contour largestBlue = (largestIndexBlue != -1) ? contoursBlue.get(largestIndexBlue) : null;

        //If we don't have a main light for one of the colors, we know both colors are the same
        //TODO we should re-filter the contours by size to ensure that we get at least a decent size
        if (largestRed == null && largestBlue == null)
            return new BeaconColorAnalysis(BeaconColor.UNKNOWN, BeaconColor.UNKNOWN);
        if (largestRed == null)
            return new BeaconColorAnalysis(BeaconColor.BLUE_BRIGHT, BeaconColor.BLUE_BRIGHT);
        if (largestBlue == null)
            return new BeaconColorAnalysis(BeaconColor.RED_BRIGHT, BeaconColor.RED_BRIGHT);

        //Look at the locations of the largest contours
        //Check to see if the largest red contour is more left-most than the largest right contour
        //If it is, then we know that the left beacon is red and the other blue, and vice versa

        Point[] largestRedPoints = largestRed.toArray();
        Point[] largestBluePoints = largestBlue.toArray();

        Point largestRedCenter = findCenter(largestRedPoints);
        Point largestBlueCenter = findCenter(largestBluePoints);

        Drawing.drawText(img, "R", largestRedCenter, 1.0f, new ColorRGBA(255, 0, 0));
        Drawing.drawText(img, "B", largestBlueCenter, 1.0f, new ColorRGBA(0, 0, 255));

        final int xMinDistance = 0 / img.width(); //percent of image width
        if (largestRedCenter.x + xMinDistance < largestBlueCenter.x) {
            Drawing.drawText(img, "RED, BLUE", new Point(0, 8), 1.0f, new ColorGRAY(255), Drawing.Anchor.BOTTOMLEFT);
            return new BeaconColorAnalysis(BeaconColor.RED, BeaconColor.BLUE);
        }
        if (largestBlueCenter.y + xMinDistance < largestRedCenter.x) {
            Drawing.drawText(img, "BLUE, RED", new Point(0, 8), 1.0f, new ColorGRAY(255), Drawing.Anchor.BOTTOMLEFT);
            return new BeaconColorAnalysis(BeaconColor.BLUE, BeaconColor.RED);
        }

        //Remove the largest index and look for the next largest - this will identify the center
        //If it's above a certain size, we have a center! -> so neither light is on BRIGHT mode
        return null;
    }

    private static Point findCenter(Point[] points)
    {
        Point sum = new Point(0, 0);

        for(Point p : points)
            sum = new Point(sum.x + p.x, sum.y + p.y);
        return new Point(sum.x / points.length, sum.y / points.length);
    }

    private static int findLargestIndex(List<Contour> contours)
    {
        if (contours.size() < 1)
            return -1;
        int largestIndex = 0;
        double maxArea = 0.0f;
        for(int i = 0; i<contours.size(); i++)
        {
            Contour c = contours.get(i);
            if (c.area() > maxArea) {
                largestIndex = i;
                maxArea = c.area();
            }
        }
        return largestIndex;
    }
}
