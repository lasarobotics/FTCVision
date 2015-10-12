package org.lasarobotics.vision.ftc.resq;

import org.lasarobotics.vision.detection.Contour;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.util.color.ColorGRAY;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.ArrayList;
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

    public static BeaconColorAnalysis analyzeColor(List<Contour> contoursR, List<Contour> contoursB, Mat img)
    {
        List<Contour> contoursRed = new ArrayList<>(contoursR);
        List<Contour> contoursBlue= new ArrayList<>(contoursB);

        //Get the largest contour in each - we're calling this one the main light
        int largestIndexRed = findLargestIndex(contoursRed);
        int largestIndexBlue = findLargestIndex(contoursBlue);
        Contour largestRed = (largestIndexRed != -1) ? contoursRed.get(largestIndexRed) : null;
        Contour largestBlue = (largestIndexBlue != -1) ? contoursBlue.get(largestIndexBlue) : null;

        //If we don't have a main light for one of the colors, we know both colors are the same
        //TODO we should re-filter the contours by size to ensure that we get at least a decent size
        if (largestRed == null && largestBlue == null)
            return new BeaconColorAnalysis(BeaconColor.UNKNOWN, BeaconColor.UNKNOWN);

        //TODO rotate image based on camera rotation here

        //The height of the beacon on screen is the height of the largest contour
        Contour largestHeight = ((largestRed != null ? largestRed.size().height : 0) >
                                (largestBlue != null ? largestBlue.size().height : 0)) ? largestRed : largestBlue;
        assert largestHeight != null;
        double beaconHeight = largestHeight.size().height;
        //Get beacon width on screen by extrapolating from height
        final double beaconActualHeight = 14.5; //cm, only the lit up portion
        final double beaconActualWidth =  21.0; //cm
        final double beaconWidthHeightRatio = beaconActualWidth / beaconActualHeight;
        double beaconWidth = beaconHeight * beaconWidthHeightRatio;
        Size beaconSize = new Size(beaconWidth, beaconHeight);
        //Get the left-most largest contour
        Contour leftMostContour = ((largestRed != null ? largestRed.origin().x : Integer.MAX_VALUE) <
                (largestBlue != null ? largestBlue.origin().x : Integer.MAX_VALUE)) ? largestRed : largestBlue;
        assert leftMostContour != null;
        Point beaconOrigin = leftMostContour.origin();

        //Draw the rectangle containing the beacon
        Point beaconBottomRight = new Point(beaconOrigin.x + beaconSize.width, beaconOrigin.y + beaconSize.height);
        Drawing.drawRectangle(img, beaconOrigin, beaconBottomRight, new ColorRGBA(0, 255, 0), 4);

        //Tell us the height of the beacon
        //TODO later we can get the distance away from the beacon based on its height and position
        Drawing.drawText(img, "Height: " + beaconHeight, new Point(img.width()-256, 8), 1.0f, new ColorGRAY(255), Drawing.Anchor.BOTTOMLEFT);

        //If the largest part of the non-null color is wider than a certain distance, then both are bright
        //Otherwise, only one may be lit
        //If only one is lit, and is wider than a certain distance, it is bright
        if (largestRed == null)
            return new BeaconColorAnalysis(BeaconColor.BLUE_BRIGHT, BeaconColor.BLUE_BRIGHT);
        else if (largestBlue == null)
            return new BeaconColorAnalysis(BeaconColor.RED_BRIGHT, BeaconColor.RED_BRIGHT);

        //Look at the locations of the largest contours
        //Check to see if the largest red contour is more left-most than the largest right contour
        //If it is, then we know that the left beacon is red and the other blue, and vice versa

        Point largestRedCenter = largestRed.center();
        Point largestBlueCenter = largestBlue.center();

        Drawing.drawText(img, "R", largestRedCenter, 1.0f, new ColorRGBA(255, 0, 0));
        Drawing.drawText(img, "B", largestBlueCenter, 1.0f, new ColorRGBA(0, 0, 255));

        final int xMinDistance = (int)(0.05 * beaconSize.width); //percent of beacon width
        boolean leftIsRed = false;
        if (largestRedCenter.x + xMinDistance < largestBlueCenter.x) {
            leftIsRed = true;
            Drawing.drawText(img, "RED, BLUE", new Point(0, 8), 1.0f, new ColorGRAY(255), Drawing.Anchor.BOTTOMLEFT);
        }
        else if (largestBlueCenter.y + xMinDistance < largestRedCenter.x) {
            leftIsRed = false;
            Drawing.drawText(img, "BLUE, RED", new Point(0, 8), 1.0f, new ColorGRAY(255), Drawing.Anchor.BOTTOMLEFT);
        }
        else
        {
            return new BeaconColorAnalysis(BeaconColor.UNKNOWN, BeaconColor.UNKNOWN);
        }

        //Remove the largest index and look for the next largest - this will identify the center
        //If it's above a certain size, we have a center! -> so neither light is on BRIGHT mode

        contoursRed.remove(largestIndexRed);
        contoursBlue.remove(largestIndexBlue);
        int secondLargestIndexRed = findLargestIndex(contoursRed);
        int secondLargestIndexBlue = findLargestIndex(contoursBlue);
        Contour secondLargestRed = (secondLargestIndexRed != -1) ? contoursRed.get(secondLargestIndexRed) : null;
        Contour secondLargestBlue = (secondLargestIndexBlue != -1) ? contoursBlue.get(secondLargestIndexBlue) : null;

        //If we couldn't find the center that way, check if the size of one of the largest contours extends
        //To the other, so that one is larger than the other

        return null;
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
