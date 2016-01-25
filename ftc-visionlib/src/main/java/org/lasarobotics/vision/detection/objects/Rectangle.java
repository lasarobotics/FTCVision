package org.lasarobotics.vision.detection.objects;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;

/**
 * Implements a single rectangle object with advanced measurement capabilities
 */
public class Rectangle extends Detectable {
    private RotatedRect rect = new RotatedRect();

    /**
     * Create a null rectangle
     */
    public Rectangle() {
        this.rect = new RotatedRect();
    }

    /**
     * Create a rectangle based on an OpenCV rotated rectangle
     *
     * @param rect OpenCV rotated rectangle
     */
    public Rectangle(RotatedRect rect) {
        this.rect = rect;
    }

    /**
     * Create a rectangle based on an OpenCV rectangle
     * @param rect OpenCV rectangle
     */
    public Rectangle(Rect rect) {
        setRect(rect);
    }

    /**
     * Create a rectangle based on a set of points
     * @param points Set of points (at least 4) defining the rectangle
     */
    public Rectangle(Point[] points) {
        //Find top-left and bottom-right
        Point min = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
        Point max = new Point(Double.MIN_VALUE, Double.MIN_VALUE);
        for (Point p : points) {
            if (p.x < min.x) {
                min.x = p.x;
            }
            if (p.y < min.y) {
                min.y = p.y;
            }
            if (p.x > max.x) {
                max.x = p.x;
            }
            if (p.y > max.y) {
                max.y = p.y;
            }
        }
        setRect(new Rect(min, max));
    }

    private void setRect(Rect rect) {
        this.rect = new RotatedRect(new Point(rect.tl().x + rect.size().width / 2,
                rect.tl().y + rect.size().height / 2), rect.size(), 0.0);
    }

    /**
     * Get the OpenCV rotated rectangle
     * @return OpenCV rotated rectangle
     */
    public RotatedRect getRotatedRect() {
        return rect;
    }

    /**
     * Get the OpenCV rectangle
     * @return OpenCV rectangle
     */
    public Rect getBoundingRect() {
        return rect.boundingRect();
    }

    private Size size() {
        return rect.size;
    }

    public double height() {
        return size().height;
    }

    public double width() {
        return size().width;
    }

    /**
     * Get the angle of inclination of the rectangle
     * @return Angle of inclination
     */
    public double angle() {
        return rect.angle;
    }

    /**
     * Get the center of the rectangle
     * @return Center of the rectangle
     */
    public Point center() {
        return rect.center;
    }

    public double left() {
        return center().x - (width() / 2);
    }

    public double right() {
        return center().x + (width() / 2);
    }

    public double top() {
        return center().y - (height() / 2);
    }

    public double bottom() {
        return center().y + (height() / 2);
    }

    /**
     * Get the area of the rectangle
     * @return Area of the rectangle = w * h
     */
    public double area() {
        return width() * height();
    }

    /**
     * Returns true if the ellipse is ENTIRELY inside the contour
     *
     * @param contour The contour to test against
     * @return True if the ellipse is entirely inside the contour, false otherwise
     */
    public boolean isInside(Contour contour) {
        //TODO this algorithm checks for entirety; make an isEntirelyInside() and isPartiallyInside()
        return left() >= contour.left() && right() <= contour.right() &&
                top() >= contour.top() && bottom() <= contour.bottom();
    }
}
