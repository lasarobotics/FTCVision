package org.lasarobotics.vision.image;

import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.detection.objects.Ellipse;
import org.lasarobotics.vision.util.color.Color;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Methods for drawing shapes onto images
 */
public class Drawing {
    public static void drawCircle(Mat img, Point center, int diameter, Color color)
    {
        drawCircle(img, center, diameter, color, 2);
    }
    public static void drawCircle(Mat img, Point center, int diameter, Color color, int thickness)
    {
        Core.circle(img, center, diameter, color.getScalarRGBA(), thickness);
    }
    public static void drawEllipse(Mat img, Ellipse ellipse, Color color)
    {
        drawEllipse(img, ellipse, color, 2);
    }
    public static void drawEllipse(Mat img, Ellipse ellipse, Color color, int thickness)
    {
        Core.ellipse(img, ellipse.getRect(), color.getScalarRGBA(), thickness);
    }
    public static void drawEllipses(Mat img, List<Ellipse> ellipses, Color color)
    {
        drawEllipses(img, ellipses, color, 2);
    }
    public static void drawEllipses(Mat img, List<Ellipse> ellipses, Color color, int thickness)
    {
        for (Ellipse ellipse : ellipses)
            Core.ellipse(img, ellipse.getRect(), color.getScalarRGBA(), thickness);
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
            Transform.flip(img, Transform.FlipType.FLIP_ACROSS_Y);
        Core.putText(img, text, origin, Core.FONT_HERSHEY_SIMPLEX, scale, color.getScalarRGBA(), 2, Core.LINE_8,
                (locationOnImage == Anchor.BOTTOMLEFT || locationOnImage == Anchor.BOTTOMLEFT_UNFLIPPED_Y));
        if (locationOnImage == Anchor.BOTTOMLEFT)
            Transform.flip(img, Transform.FlipType.FLIP_ACROSS_Y);
    }

    public static void drawLine(Mat img, Point point1, Point point2, Color color)
    {
        drawLine(img, point1, point2, color, 2);
    }
    public static void drawLine(Mat img, Point point1, Point point2, Color color, int thickness)
    {
        Core.line(img, point1, point2, color.getScalarRGBA(), thickness);
    }

    public static void drawContour(Mat img, Contour contour, Color color)
    {
        drawContour(img, contour, color, 2);
    }
    public static void drawContour(Mat img, Contour contour, Color color, int thickness)
    {
        List<MatOfPoint> contoursOut = new ArrayList<>();
        contoursOut.add(contour.getData());
        Imgproc.drawContours(img, contoursOut, -1, color.getScalarRGBA(), thickness);
    }
    public static void drawContours(Mat img, List<Contour> contours, Color color)
    {
        drawContours(img, contours, color, 2);
    }
    public static void drawContours(Mat img, List<Contour> contours, Color color, int thickness)
    {
        List<MatOfPoint> contoursOut = new ArrayList<>();
        for (Contour contour : contours)
            contoursOut.add(contour.getData());
        Imgproc.drawContours(img, contoursOut, -1, color.getScalarRGBA(), thickness);
    }

    public static void drawRectangle(Mat img, Point topLeft, Point bottomRight, Color color)
    {
        drawRectangle(img, topLeft, bottomRight, color, 2);
    }
    public static void drawRectangle(Mat img, Point topLeft, Point bottomRight, Color color, int thickness)
    {
        Core.rectangle(img, topLeft, bottomRight, color.getScalarRGBA(), thickness);
    }
}
