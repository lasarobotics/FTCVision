package org.lasarobotics.vision.image;

import org.lasarobotics.vision.util.color.Color;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Methods for drawing shapes onto images
 */
public class Drawing {
    public static void drawCircle(Mat img, Point center, int diameter, Color color)
    {
        Core.circle(img, center, diameter, color.getScalarRGBA());
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
        Core.putText(img, text, origin, Core.FONT_HERSHEY_SIMPLEX, scale, color.getScalarRGBA(), 2, Core.LINE_8,
                (locationOnImage == Anchor.BOTTOMLEFT || locationOnImage == Anchor.BOTTOMLEFT_UNFLIPPED_Y));
        if (locationOnImage == Anchor.BOTTOMLEFT)
            Image.flip(img, Image.FlipType.FLIP_ACROSS_Y);
    }

    public static void drawLine(Mat img, Point point1, Point point2, Color color)
    {
        drawLine(img, point1, point2, color, 2);
    }
    public static void drawLine(Mat img, Point point1, Point point2, Color color, int thickness)
    {
        Core.line(img, point1, point2, color.getScalarRGBA(), thickness);
    }

    public static void drawContours(Mat img, List<MatOfPoint> contours, Color color)
    {
        Imgproc.drawContours(img, contours, -1, color.getScalarRGBA(), 2);
    }
    public static void drawContours(Mat img, List<MatOfPoint> contours, Color color, int thickness)
    {
        Imgproc.drawContours(img, contours, -1, color.getScalarRGBA(), thickness);
    }
}
