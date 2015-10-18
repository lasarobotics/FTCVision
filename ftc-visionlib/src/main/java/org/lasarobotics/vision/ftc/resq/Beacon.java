package org.lasarobotics.vision.ftc.resq;

import org.lasarobotics.vision.detection.PrimitiveDetection;
import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.detection.objects.Ellipse;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.util.color.ColorGRAY;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
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
        UNKNOWN;

        @Override
        public String toString() {
            switch (this)
            {
                case RED:
                    return "red";
                case BLUE:
                    return "blue";
                case RED_BRIGHT:
                    return "RED!";
                case BLUE_BRIGHT:
                    return "BLUE!";
                case UNKNOWN:
                default:
                    return "???";

            }
        }
    }

    public static class BeaconColorAnalysis
    {
        BeaconColor left;
        BeaconColor right;

        //TODO Color and CONFIDENCE should make up the results

        //TODO add Size size, Point locationTopLeft, Distance distanceApprox
        BeaconColorAnalysis(BeaconColor left, BeaconColor right)
        {
            assert left != null;
            assert right != null;
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

    private static Ellipse findBestEllipse(Contour contour, List<Ellipse> ellipses)
    {
        Ellipse best = null;
        double bestArea = Double.MAX_VALUE;
        Rect boundingRect = contour.getBoundingRect();
        for (Ellipse ellipse : ellipses)
        {
            //Point diff = new Point(Math.abs(contour.center().x - e.center().x),
            //        Math.abs(contour.center().y - e.center().y));
            //double distance = diff.inside(contour)


            //Check whether ellipse is within the contour
            if (ellipse.isInside(contour))
            {
                //If this is the SMALLEST ellipse, make it the best!
                if (ellipse.area() < bestArea)
                {
                    bestArea = ellipse.area();
                    best = ellipse;
                }
            }
        }

        return best;
    }

    private static Contour getBestContour(List<Contour> contours, List<Ellipse> ellipses)
    {
        if (contours.size() == 0 || ellipses.size() == 0)
            return null;

        //Start with the largest contour
        //Contour best = contours.get(findLargestIndex(contours));
        //Contour first = new Contour((MatOfPoint)best.clone());

        while (contours.size() > 0){
            int testIndex = findLargestIndex(contours);
            Contour test = contours.get(testIndex);

            //Find the best ellipse
            Ellipse bestEllipse = findBestEllipse(test, ellipses);

            //If this ellipse exists, then return it because it is the largest!
            if (bestEllipse != null)
            {
                return test;
            }
            contours.remove(testIndex);
        }

        //Return the best if we have one, otherwise just return the largest
        //TODO return first or null?
        //return (contours.size() > 0) ? best : null;
        return null;
    }

    public static BeaconColorAnalysis analyzeColor(List<Contour> contoursR, List<Contour> contoursB, Mat img, Mat gray)
    {
        List<Contour> contoursRed = new ArrayList<>(contoursR);
        List<Contour> contoursBlue= new ArrayList<>(contoursB);

        //Locate ellipses in the image to process contours against
        //Each contour must have an ellipse of correct specification
        PrimitiveDetection primitiveDetection = new PrimitiveDetection();
        PrimitiveDetection.EllipseLocationResult ellipseLocationResult = primitiveDetection.locateEllipses_fit(gray);

        //Get the best contour in each (starting with the largest) if any contours exist
        //We're calling this one the main light
        Contour bestRed = (contoursRed.size() > 0) ? getBestContour(contoursRed, ellipseLocationResult.getEllipses()) : null;
        Contour bestBlue = (contoursBlue.size() > 0) ? getBestContour(contoursBlue, ellipseLocationResult.getEllipses()) : null;

        //If we don't have a main light for one of the colors, we know both colors are the same
        //TODO we should re-filter the contours by size to ensure that we get at least a decent size
        if (bestRed == null && bestBlue == null)
            return new BeaconColorAnalysis(BeaconColor.UNKNOWN, BeaconColor.UNKNOWN);

        //Test contour


        //TODO rotate image based on camera rotation here

        //The height of the beacon on screen is the height of the best contour
        Contour largestHeight = ((bestRed != null ? bestRed.size().height : 0) >
                                (bestBlue != null ? bestBlue.size().height : 0)) ? bestRed : bestBlue;
        assert largestHeight != null;
        double beaconHeight = largestHeight.size().height;
        //Get beacon width on screen by extrapolating from height
        final double beaconActualHeight = 12.5; //cm, only the lit up portion - 14.0 for entire
        final double beaconActualWidth =  21.0; //cm
        final double beaconWidthHeightRatio = beaconActualWidth / beaconActualHeight;
        double beaconWidth = beaconHeight * beaconWidthHeightRatio;
        Size beaconSize = new Size(beaconWidth, beaconHeight);
        //Get the left-most best contour
        Contour leftMostContour = ((bestRed != null ? bestRed.topLeft().x : Integer.MAX_VALUE) <
                (bestBlue != null ? bestBlue.topLeft().x : Integer.MAX_VALUE)) ? bestRed : bestBlue;
        assert leftMostContour != null;
        Point beaconOrigin = leftMostContour.topLeft();

        //Draw the rectangle containing the beacon
        Point beaconBottomRight = new Point(beaconOrigin.x + beaconSize.width, beaconOrigin.y + beaconSize.height);
        Drawing.drawRectangle(img, beaconOrigin, beaconBottomRight, new ColorRGBA(0, 255, 0), 4);

        //Tell us the height of the beacon
        //TODO later we can get the distance away from the beacon based on its height and position
        Drawing.drawText(img, "Height: " + beaconHeight, new Point(img.width()-256, 8), 1.0f, new ColorGRAY(255), Drawing.Anchor.BOTTOMLEFT);

        //If the largest part of the non-null color is wider than a certain distance, then both are bright
        //Otherwise, only one may be lit
        //If only one is lit, and is wider than a certain distance, it is bright
        if (bestRed == null)
            return new BeaconColorAnalysis(BeaconColor.BLUE_BRIGHT, BeaconColor.BLUE_BRIGHT);
        else if (bestBlue == null)
            return new BeaconColorAnalysis(BeaconColor.RED_BRIGHT, BeaconColor.RED_BRIGHT);

        //Look at the locations of the largest contours
        //Check to see if the largest red contour is more left-most than the largest right contour
        //If it is, then we know that the left beacon is red and the other blue, and vice versa

        Point largestRedCenter = bestRed.center();
        Point largestBlueCenter = bestBlue.center();

        Drawing.drawText(img, "R", largestRedCenter, 1.0f, new ColorRGBA(255, 0, 0));
        Drawing.drawText(img, "B", largestBlueCenter, 1.0f, new ColorRGBA(0, 0, 255));

        final int xMinDistance = (int)(0.05 * beaconSize.width); //percent of beacon width
        boolean leftIsRed;
        if (largestRedCenter.x + xMinDistance < largestBlueCenter.x) {
            leftIsRed = true;
        }
        else if (largestBlueCenter.y + xMinDistance < largestRedCenter.x) {
            leftIsRed = false;
        }
        else
        {
            return new BeaconColorAnalysis(BeaconColor.UNKNOWN, BeaconColor.UNKNOWN);
        }

        //Remove the largest index and look for the next largest
        //If the next largest is (mostly) within the area of the box, then merge it in with the largest

        /*contoursRed.remove(largestIndexRed);
        contoursBlue.remove(largestIndexBlue);
        int secondLargestIndexRed = findLargestIndex(contoursRed);
        int secondLargestIndexBlue = findLargestIndex(contoursBlue);
        Contour secondLargestRed = (secondLargestIndexRed != -1) ? contoursRed.get(secondLargestIndexRed) : null;
        Contour secondLargestBlue = (secondLargestIndexBlue != -1) ? contoursBlue.get(secondLargestIndexBlue) : null;
        */
        //Check if the size of the largest contour(s) is about twice the size of the other
        //This would indicate one is brightly lit and the other is not

        //If this is not true, then neither part of the beacon is highly lit
        if (leftIsRed)
            return new BeaconColorAnalysis(BeaconColor.RED, BeaconColor.BLUE);
        else
            return new BeaconColorAnalysis(BeaconColor.BLUE, BeaconColor.RED);
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
