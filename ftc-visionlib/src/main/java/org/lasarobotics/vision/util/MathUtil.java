package org.lasarobotics.vision.util;

import org.opencv.core.Point;

/**
 * Math utilities
 */
public final class MathUtil {
    /**
     * Suppresses constructor for noninstantiability
     */
    private MathUtil() {
        throw new AssertionError();
    }

    /**
     * Double equality epsilon (maximum tolerance for a double)
     */
    private final static double EPSILON = 0.000001;

    /**
     * Gives a "deadzone" where any value less
     * than this would return zero.
     *
     * @param deadband Maximum value that returns zero
     * @param value    Value to test
     * @return Deadbanded value
     */
    public static double deadband(double deadband, double value) {
        return (Math.abs(value) > deadband) ? value : 0;
    }

    /**
     * Returns if two double values are equal to within epsilon.
     *
     * @param a First value
     * @param b Second value
     * @return True if the values are equal, false otherwise
     */
    public static Boolean equal(double a, double b) {
        return (Math.abs(a - b) < EPSILON);
    }

    /**
     * Returns if two double values are equal to within a distance.
     *
     * @param a        First value
     * @param b        Second value
     * @param distance Maximum distance between a and b
     * @return True if the values are equal ot within distance, false otherwise
     */
    public static Boolean equal(double a, double b, double distance) {
        return (Math.abs(a - b) < distance);
    }

    /**
     * Ignores values equal to the fail value (normally zero).
     *
     * @param value     Current value
     * @param lastvalue Previous value
     * @param fail      Filter this value, normally zero
     * @return Filtered value
     */
    public static double filter(double value, double lastvalue, double fail) {
        return (value == fail) ? lastvalue : value;
    }

    /**
     * Forces a numerical value to be between a min
     * and a max.
     *
     * @param min   If less than min, returns min
     * @param max   If greater than max, returns max
     * @param value Value to test
     * @return Coerced value
     */
    public static double coerce(double min, double max, double value) {
        return (value > max) ? max : (value < min) ? min : value;
    }

    /**
     * Tests if a number is between the bounds, exclusive.
     *
     * @param min   If less than min, returns false
     * @param max   If greater than max, returns false
     * @param value Value to test
     * @return Returns true if value is between (exclusive) min and max, false otherwise.
     */
    public static boolean inBounds(double min, double max, double value) {
        return (value < max) && (value > min);
    }

    public static double distance(double deltaX, double deltaY)
    {
        return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
    }

    /**
     * Calculate the angle between three point-vectors
     * @param pt1 Vector 1
     * @param pt2 Vector 2
     * @param pt0 Vector 0
     * @return The angle (cosine) between the three vectors
     */
    public static double angle( Point pt1, Point pt2, Point pt0 )
    {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
    }
}
