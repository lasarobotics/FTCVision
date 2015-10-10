package org.lasarobotics.vision.util;

import org.opencv.core.Scalar;

/**
 * Implements a single RGBA/RGB color.
 */
public class Color {
    private int r, g, b, a;

    public Color(int r, int g, int b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
    }
    public Color(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    public Color(Scalar scalar)
    {
        if (scalar.val.length < 3)
            throw new IllegalArgumentException("Scalar must have a length of 3 or 4.");
        this.r = (int)scalar.val[0];
        this.g = (int)scalar.val[1];
        this.b = (int)scalar.val[2];
        this.a = (scalar.val.length >= 4) ? (int)scalar.val[3] : 255;
    }
    public Color(String hexCode)
    {
        //remove hex key #
        if (!hexCode.startsWith("#"))
            hexCode = "#" + hexCode;
        //ensure that the length is correct
        if (hexCode.length() != 7 && hexCode.length() != 9)
            throw new IllegalArgumentException("Hex code must be of length 6 or 8 characters.");
        //get the integer representation
        int color = android.graphics.Color.parseColor(hexCode);
        //get the r,g,b,a values
        this.r = android.graphics.Color.red(color);
        this.g = android.graphics.Color.green(color);
        this.b = android.graphics.Color.blue(color);
        this.a = android.graphics.Color.alpha(color);
    }

    public int getR()
    {
        return r;
    }
    public int getG()
    {
        return g;
    }
    public int getB()
    {
        return b;
    }
    public int getA()
    {
        return a;
    }
    public Scalar getScalar()
    {
        return new Scalar(r, g, b, a);
    }
    public int getInteger()
    {
        return android.graphics.Color.argb(a, r, g, b);
    }
}
