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

    @Override
    protected Scalar parseScalar(Scalar s)
    {
        if (s.val.length < 3)
            throw new IllegalArgumentException("Scalar must have 3 dimensions.");
        return new Scalar(s.val[0], s.val[1], s.val[2]);
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
