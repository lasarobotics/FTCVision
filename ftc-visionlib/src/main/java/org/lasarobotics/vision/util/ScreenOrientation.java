package org.lasarobotics.vision.util;

import android.view.Surface;

/**
 * Rotation relative to the horizontal positive x-axis (east) and increasing counterclockwise
 */
public enum ScreenOrientation {
    LANDSCAPE(0),
    DEFAULT(0),
    PORTRAIT(90),
    LANDSCAPE_WEST(180),
    PORTRAIT_REVERSE(270);

    private final double angle;

    ScreenOrientation(double angle) {
        this.angle = angle;
    }

    public static ScreenOrientation getFromAngle(double angle) {
        return getFromAngle((int) angle);
    }

    private static ScreenOrientation getFromAngle(int angle) {
        while (angle > 360)
            angle -= 360;
        while (angle < 0)
            angle += 360;

        switch (angle) {
            case 0:
            case 360:
                return LANDSCAPE;
            case 90:
                return PORTRAIT;
            case 180:
                return LANDSCAPE_WEST;
            case 270:
                return PORTRAIT_REVERSE;
            default:
                throw new RuntimeException("The input angle must be a multiple of 90 degrees!");
        }
    }

    public static ScreenOrientation getFromSurface(int id) {
        switch (id) {
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

    public double getAngle() {
        return angle;
    }
}
