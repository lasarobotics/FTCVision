package org.lasarobotics.vision.detection.objects;

import org.opencv.core.Point;

import java.util.Comparator;

/**
 * Stores data on lines collected from EdgeDetection
 */
public class Line {
    private double slope;
    private double length;
    private Point startPoint;
    private Point endPoint;

    public static final Comparator<Line> LengthCompare = new Comparator<Line>() {
        @Override
        public int compare(Line lhs, Line rhs) {
            return (int)(lhs.getLength() - rhs.getLength());
        }
    };

    public Line(Point startPoint, Point endPoint)
    {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        slope = (double)(startPoint.y - endPoint.y)/(startPoint.x - endPoint.x);
        length = Math.sqrt(Math.pow(startPoint.x - endPoint.x, 2)+Math.pow(startPoint.y - endPoint.y, 2));
    }

    public double getSlope()
    {
        return slope;
    }
    public double getYIntercept()
    {
        return slope*(-startPoint.x) + startPoint.y;
    }
    public double getLength()
    {
        return length;
    }
    public Point getStartPoint()
    {
        return startPoint;
    }
    public Point getEndPoint()
    {
        return endPoint;
    }

    public double evaluateX(double x)
    {
        return slope*(x - startPoint.x) + startPoint.y;
    }
}
