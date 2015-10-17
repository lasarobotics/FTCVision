package org.lasarobotics.vision.detection.objects;

import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;

/**
 * Implements a single ellipse (acts like RotatedRect) with advanced measurement utilities
 */
public class Ellipse {
    private RotatedRect rect;

    public Ellipse(RotatedRect rect)
    {
        this.rect = rect;
    }

    public RotatedRect getRect() {
        return rect;
    }

    public Size size()
    {
        return rect.size;
    }
    public double height()
    {
        return size().height;
    }
    public double width()
    {
        return size().width;
    }
    public double angle()
    {
        return rect.angle;
    }
    public Point center()
    {
        return rect.center;
    }
    public double majorAxis()
    {
        return Math.max(height(), width());
    }
    public double minorAxis()
    {
        return Math.min(height(), width());
    }
    public double semiMajorAxis()
    {
        return majorAxis() / 2;
    }
    public double semiMinorAxis()
    {
        return minorAxis() / 2;
    }
    /**
     * Gets the first flattening ratio for the ellipse
     * @return First flattening ratio = (a-b)/a, where a=semi-major axis and b=semi-minor axis)
     */
    public double flattening()
    {
        return (semiMajorAxis() - semiMinorAxis()) / semiMajorAxis();
    }

    /**
     * Gets the eccentricity of the ellipse, between 0 (inclusive) and 1 (exclusive)
     * @return e = sqrt(1-(b^2/a^2)), where a=semi-major axis and b=semi-minor axis
     */
    public double eccentricity()
    {
        return Math.sqrt(1 - (semiMinorAxis()*semiMinorAxis())/(semiMajorAxis()*semiMajorAxis()));
    }
}
