package org.lasarobotics.vision;

import org.lasarobotics.vision.util.Color;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;

/**
 * Methods for drawing shapes onto images
 */
public class Drawing {
    public static void drawCircle(Mat img, Point center, int diameter, Color color)
    {
        Core.circle(img, center, diameter, color.getScalar());
    }

    public enum Anchor
    {
        TOPLEFT,
        BOTTOMLEFT,
        BOTTOMLEFT_UNFLIPPED_Y
    }
    public static void drawText(Mat img, String text, Point origin, float scale, Color color)
    {
        drawText(img, text, origin, scale, color, Anchor.TOPLEFT);
    }
    public static void drawText(Mat img, String text, Point origin, float scale, Color color, Anchor locationOnImage)
    {
        if (locationOnImage == Anchor.BOTTOMLEFT)
            Image.flip(img, Image.FlipType.FLIP_ACROSS_Y);
        Core.putText(img, text, origin, Core.FONT_HERSHEY_SIMPLEX, scale, color.getScalar(), 2, Core.LINE_8,
                (locationOnImage == Anchor.BOTTOMLEFT || locationOnImage == Anchor.BOTTOMLEFT_UNFLIPPED_Y));
        if (locationOnImage == Anchor.BOTTOMLEFT)
            Image.flip(img, Image.FlipType.FLIP_ACROSS_Y);
    }
}
