package org.lasarobotics.vision.util.color;

import org.opencv.core.Scalar;

/**
 * Implements a color in the HSV color space
 */
public class ColorHSV extends Color {

    public ColorHSV(Scalar s)
    {
        super(s);
    }

    public ColorHSV(int h, int s, int v)
    {
        super(new Scalar(h, s, v));
    }

    public ColorSpace getColorSpace() {
        return ColorSpace.HSV;
    }

    public int hue()
    {
        return (int)scalar.val[0];
    }
    public int saturation()
    {
        return (int)scalar.val[1];
    }
    public int value()
    {
        return (int)scalar.val[2];
    }
}
