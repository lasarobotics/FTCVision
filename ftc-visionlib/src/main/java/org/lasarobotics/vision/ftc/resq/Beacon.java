package org.lasarobotics.vision.ftc.resq;

import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.detection.objects.Rectangle;
import org.lasarobotics.vision.util.MathUtil;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Beacon location and analysis
 */
public final class Beacon {

    private final AnalysisMethod method;

    public Beacon() {
        method = AnalysisMethod.FAST;
    }

    public Beacon(AnalysisMethod method) {
        this.method = method;
    }

    public BeaconAnalysis analyzeFrame(List<Contour> contoursRed, List<Contour> contoursBlue,
                                       Mat img, Mat gray) {
        return analyzeFrame(contoursRed, contoursBlue, img, gray, ScreenOrientation.DEFAULT);
    }

    public BeaconAnalysis analyzeFrame(List<Contour> contoursRed, List<Contour> contoursBlue,
                                       Mat img, Mat gray, ScreenOrientation orientation) {
        switch (method) {
            case FAST:
            case DEFAULT:
            default:
                return BeaconAnalyzer.analyze_FAST(contoursRed, contoursBlue, img, gray, orientation);
            case COMPLEX:
                return BeaconAnalyzer.analyze_COMPLEX(contoursRed, contoursBlue, img, gray, orientation);
        }
    }

    public enum AnalysisMethod {
        //Default method
        DEFAULT,
        //Fastest method - picks the two largest contours without concern
        FAST,
        //Slower and complex method - picks contours based on statistical analysis
        COMPLEX;

        public String toString() {
            switch (this) {
                case DEFAULT:
                case FAST:
                default:
                    return "FAST";
                case COMPLEX:
                    return "COMPLEX";
            }
        }
    }

    public enum BeaconColor {
        RED,
        BLUE,
        RED_BRIGHT,
        BLUE_BRIGHT,
        UNKNOWN;

        @Override
        public String toString() {
            switch (this) {
                case RED:
                    return "red";
                case BLUE:
                    return "blue";
                case RED_BRIGHT:
                    return "RED!";
                case BLUE_BRIGHT:
                    return "BLUE!";
                case UNKNOWN:
                default:
                    return "???";
            }
        }
    }

    public static class BeaconAnalysis {
        private final double confidence;
        private final BeaconColor left;
        private final BeaconColor right;
        private final Rectangle location;

        //TODO Color and CONFIDENCE should make up the results

        //TODO add Size size, Point locationTopLeft, Distance distanceApprox
        public BeaconAnalysis() {
            this.left = BeaconColor.UNKNOWN;
            this.right = BeaconColor.UNKNOWN;
            this.confidence = 0.0f;
            this.location = new Rectangle();
        }

        public BeaconAnalysis(BeaconColor left, BeaconColor right, Rectangle location, double confidence) {
            this.left = left;
            this.right = right;
            this.confidence = confidence;
            this.location = location;
        }

        public Rectangle getBoundingBox() {
            return location;
        }

        public Point getTopLeft() {
            return location.topLeft();
        }

        public Point getBottomRight() {
            return location.bottomRight();
        }

        public Point getCenter() {
            return location.center();
        }

        public double getPixelWidth() {
            return location.width();
        }

        public double getPixelHeight() {
            return location.height();
        }

        public BeaconColor getStateLeft() {
            return left;
        }

        public BeaconColor getStateRight() {
            return right;
        }

        public double getConfidence() {
            return !Double.isNaN(confidence) ? MathUtil.coerce(0, 1, confidence) : 0.0;
        }

        public String getConfidenceString() {
            final DecimalFormat format = new DecimalFormat("0.000");
            return !Double.isNaN(confidence) ? format.format(MathUtil.coerce(0, 1, getConfidence()) * 100.0f) + "%" : "n/a";
        }

        public boolean isLeftKnown() {
            return left != BeaconColor.UNKNOWN;
        }

        public boolean isLeftBlue() {
            return (left == BeaconColor.BLUE_BRIGHT || left == BeaconColor.BLUE);
        }

        public boolean isLeftRed() {
            return (left == BeaconColor.RED_BRIGHT || left == BeaconColor.RED);
        }

        public boolean isRightKnown() {
            return right != BeaconColor.UNKNOWN;
        }

        public boolean isRightBlue() {
            return (right == BeaconColor.BLUE_BRIGHT || right == BeaconColor.BLUE);
        }

        public boolean isRightRed() {
            return (right == BeaconColor.RED_BRIGHT || right == BeaconColor.RED);
        }

        public boolean isBeaconFound() {
            return isLeftKnown() && isRightKnown();
        }

        public boolean isBeaconFullyLit() {
            return (left == BeaconColor.BLUE_BRIGHT || left == BeaconColor.RED_BRIGHT) &&
                    (right == BeaconColor.BLUE_BRIGHT || right == BeaconColor.RED_BRIGHT);
        }

        public String getColorString() {
            return left.toString() + ", " + right.toString();
        }

        public String getLocationString() {
            return getCenter().toString();
        }

        @Override
        public String toString() {
            return "Color: " + getColorString() + "\r\n Location: " + getLocationString() + "\r\n Confidence: " + getConfidenceString();
        }
    }
}
