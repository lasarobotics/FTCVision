package org.lasarobotics.vision.test.util;

import android.view.Surface;

/**
 * Rotation relative to the horizontal positive x-axis (east) and increasing counterclockwise
 */
public enum ScreenOrientation {
    LANDSCAPE(0),
    PORTRAIT(90),
    LANDSCAPE_WEST(180),
    PORTRAIT_REVERSE(270);

    private final double angle;
    ScreenOrientation(double angle)
    {
        this.angle = angle;
    }

    public static ScreenOrientation getFromSurface(int id)
    {
        switch (id)
        {
            case Surface.ROTATION_0:
                return LANDSCAPE;
            case Surface.ROTATION_90:
                return PORTRAIT;
            case Surface.ROTATION_180:
                return LANDSCAPE_WEST;
            case Surface.ROTATION_270:
                return PORTRAIT_REVERSE;
            default:
                return LANDSCAPE;
        }
    }

    public double getAngle()
    {
        return angle;
    }
}
