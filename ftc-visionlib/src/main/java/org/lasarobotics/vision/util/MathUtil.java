package org.lasarobotics.vision.util;

/**
 * Math utilities
 */
public final class MathUtil {
    public static double distance(double deltaX, double deltaY)
    {
        return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
    }
}
