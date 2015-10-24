package org.lasarobotics.vision.detection;

import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.util.MathUtil;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements Canny edge detection in order to find breaks in the course wall.
 */
public class EdgeDetection {

    //Valid wall height to break height ratio
    public static final double BREAK_THRESHOLD = 0;
    public static final double SLOPE_THRESHOLD = 0.125;

    /*
    * Returns a list of all contour breaks in course wall that meet a certain ratio threshold.
    * Mat gray argument should be a single channel grayscale mat of the frame.
    */
    public List<Contour> getBreaks(Mat gray)
    {
        List<Contour> breaks = new ArrayList<>();
        //TODO Establish lowTreshold and highThreshold
        //Convert grayscale image to Canny Image. Canny(Input, Output, LowThresh, HighTresh, KernelSize)
        Imgproc.Canny(gray, gray, 30, 90, 3, true);
        Mat lineMat = new Mat();
        //Use Hough Lines Transform to identify lines in frame with the following characteristics
        int threshold = 100;
        int minLineSize = 100;
        int lineGap = 10;
        Imgproc.HoughLinesP(gray, lineMat, 1, Math.PI/180, threshold, minLineSize, lineGap);
        //Create a list of lines detected
        ArrayList<Line> lines = new ArrayList<>();
        for(int i = 0; i < lineMat.cols(); i++)
        {
            double[] vec = lineMat.get(0, i);
            lines.add(new Line(new Point(vec[0], vec[1]), new Point(vec[2], vec[3])));
        }
        //Filter lines to only keep those that are part of wall with a break
        breaks = filterLines(lines);
        return breaks;
    }

    private ArrayList<Contour> filterLines(ArrayList<Line> origLines) {
        //Make a copy of original lines for editing
        ArrayList<Line> lines = new ArrayList<>(origLines);
        //Sort lines based on slope
        Collections.sort(lines, Line.SlopeCompare);
        //Remove lines with no pairs
        for(int i = 0; i < lines.size(); i++)
        {
            if(findPairs(i, lines).isEmpty())
                lines.remove(i);
        }
        //Remove slope chunks that do not have at least 4 lines in them and group lines into slope chunks
        ArrayList<ArrayList<Line>> slopeChunks =  new ArrayList<>();
        int count = 1;
        for(int i = 0; i < lines.size(); i += count)
        {
            count = 1;
            double lineSlope = lines.get(i).getSlope();
            for(int x = 1; i+x < lines.size(); x++)
            {
                if(Math.abs(lineSlope - lines.get(i+x).getSlope()) <= SLOPE_THRESHOLD)
                    count++;
                else
                    break;
            }
            //If count is below 4, remove lines
            if(count < 4)
            {
                for(int x = 0; x < count; x++)
                {
                    lines.remove(i+x);
                }
            }
            else //if count is >= 4, create a list of lines out of the slope chunk and add them to slopeChunks
            {
                ArrayList<Line> tmpSlopeChunk = new ArrayList<>();
                for(int x = 0; x < count; x++)
                {
                    tmpSlopeChunk.add(lines.get(i+x));
                }
                slopeChunks.add(tmpSlopeChunk);
            }
        }
        //return null;
    }

    private ArrayList<Integer> findPairs(int lineIndex, ArrayList<Line> lines) {
        ArrayList<Integer> possiblePairs = new ArrayList<>();
        Line line = lines.get(lineIndex);
        //Look for possible pairs near indexes above lineIndex
        for(int i = lineIndex; Math.abs(line.getSlope() - lines.get(i).getSlope()) <= SLOPE_THRESHOLD
                && i < lines.size(); i++)
        {
            if(i != lineIndex)
            {
                double pairSlope = (line.getStartPoint().y - lines.get(i).getStartPoint().y)/
                        (line.getStartPoint().x - lines.get(i).getStartPoint().x);
                if(Math.abs(line.getSlope() - pairSlope) <= SLOPE_THRESHOLD)
                {
                    possiblePairs.add(i);
                }
            }
        }
        //Look for possible pairs near indexes below lineIndex
        for(int i = lineIndex; Math.abs(line.getSlope() - lines.get(i).getSlope()) <= SLOPE_THRESHOLD
                && i >= 0; i--)
        {
            if(i != lineIndex)
            {
                double pairSlope = (line.getStartPoint().y - lines.get(i).getStartPoint().y)/
                        (line.getStartPoint().x - lines.get(i).getStartPoint().x);
                if(Math.abs(line.getSlope() - pairSlope) <= SLOPE_THRESHOLD)
                {
                    possiblePairs.add(i);
                }
            }
        }
        return possiblePairs;
    }
}