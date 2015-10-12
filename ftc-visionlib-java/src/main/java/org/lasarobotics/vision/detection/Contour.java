package org.lasarobotics.vision.detection;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Implements a single contour (MatOfPoint) with advanced measurement utilities
 */
public class Contour extends MatOfPoint {

    Contour(MatOfPoint data)
    {
        super(data);
    }

    public double area()
    {
        return Imgproc.contourArea(this);
    }

    public boolean isConvex()
    {
        return Imgproc.isContourConvex(this);
    }

    public Point center()
    {
        //TODO this is an UNWEIGHTED CENTER - which means that for unusual shapes, it can be inaccurate
        Point sum = new Point(0, 0);
        Point[] points = this.getPoints();

        for(Point p : points)
            sum = new Point(sum.x + p.x, sum.y + p.y);
        return new Point(sum.x / points.length, sum.y / points.length);
    }

    /**
     * Gets the top-left corner of the contour
     * @return The top left corner of the contour
     */
    public Point origin()
    {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        Point[] points = getPoints();
        for (Point p : points)
        {
            if (p.x < minX) { minX = p.x; }
            if (p.y < minY) { minY = p.y; }
        }

        return new Point(minX, minY);
    }

    public Size size()
    {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        Point[] points = getPoints();
        for (Point p : points)
        {
            if (p.x < minX) { minX = p.x; }
            if (p.y < minY) { minY = p.y; }
            if (p.x > maxX) { maxX = p.x; }
            if (p.y > maxY) { maxY = p.y; }
        }

        return new Size(maxX - minX, maxY - minY);
    }

    public Point[] getPoints()
    {
        return this.toArray();
    }
}
