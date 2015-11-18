package org.lasarobotics.vision.detection.objects;

import org.opencv.core.Point;

import java.util.Comparator;

/**
 * Stores data on lines collected from EdgeDetection
 */
public class Line {

    //Percent difference in intercepts relative to the image size allowed
    public static final double INTERCEPT_THRESHOLD = 0.05;

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
    public static final Comparator<Line> SlopeCompare = new Comparator<Line>() {
        @Override
        public int compare(Line lhs, Line rhs) {
            return (lhs.getSlope() < rhs.getSlope()) ? -1 : (lhs.getLength() > rhs.getSlope()) ? 1 : 0;
        }
    };
    public static final Comparator<Line> InterceptCompare = new Comparator<Line>() {
        @Override
        public int compare(Line lhs, Line rhs) {
            /* Line.InterceptCompare(@param Line, @param Line) -> int
             * @return -1 if
             */
            boolean inRange = Math.abs(lhs.getYIntercept() - rhs.getYIntercept()) <= INTERCEPT_THRESHOLD;
            if(inRange)
                return (int)(lhs.getSlope() - rhs.getSlope());
            else
                return (int)(lhs.getYIntercept() - rhs.getYIntercept());
        }
    };

    public Line(Point startPoint, Point endPoint)
    {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        // TODO VALUE FOR SLOPE IF UNDEFINED
        slope = (double)(startPoint.y - endPoint.y)/(startPoint.x - endPoint.x);
        length = Math.sqrt(Math.pow(startPoint.x - endPoint.x, 2)+Math.pow(startPoint.y - endPoint.y, 2));
    }

    public static boolean slopeInterceptCompare(Line l1, Line l2, double slopeThreshold) {
        /* Line.slopeInterceptCompare(@param Line, @param Line, @param double) -> boolean
        *  @return returns true if lines have similar slopes and intercepts
        */
        return Math.abs(l1.getYIntercept() - l2.getYIntercept()) <= INTERCEPT_THRESHOLD &&
                Math.abs(l1.getSlope() - l2.getSlope()) <= slopeThreshold;
    }

    public double slopeToAngle(){
        /* Line.angle() -> double
        @return the angle in degrees with y=0 being 0 degrees
         */
        return Math.atan(slope)*180/Math.PI;
    }

    public Point intersection(Line l1){
        /* Line.intersection(@param Line l1) -> Point
        @return the intersection between Line self and Line l1
         */
        double x = (l1.getYIntercept()-this.getYIntercept())/(this.getSlope()-l1.getSlope());
        Point inters = new Point(x, this.getYIntercept()+this.getSlope()*x);
        return inters;
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
    public double distFormula(Point p1, Point p2)
    {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
}
