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

    /**
     * Instantiate a beacon that uses the default method
     */
    public Beacon() {
        method = AnalysisMethod.DEFAULT;
    }

    /**
     * Instantiate a beacon that uses a specific analysis method
     *
     * @param method Analysis method
     */
    public Beacon(AnalysisMethod method) {
        this.method = method;
    }

    /**
     * Analyze the current frame using the selected analysis method
     * @param contoursRed Red contours given by a blob detector
     * @param contoursBlue Blue contours given by a blob detector
     * @param img Image to analyze
     * @param gray Grayscale image to analyze
     * @return Beacon analysis class
     */
    public BeaconAnalysis analyzeFrame(List<Contour> contoursRed, List<Contour> contoursBlue,
                                       Mat img, Mat gray) {
        return analyzeFrame(contoursRed, contoursBlue, img, gray, ScreenOrientation.DEFAULT);
    }

    /**
     * Analyze the current frame using the selected analysis method
     * @param contoursRed Red contours given by a blob detector
     * @param contoursBlue Blue contours given by a blob detector
     * @param img Image to analyze
     * @param gray Grayscale image to analyze
     * @param orientation Screen orientation compensation, given by the android.Sensors class
     * @return Beacon analysis class
     */
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

    /**
     * Analysis method
     */
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

    /**
     * Beacon color struct
     */
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

    /**
     * Beacon analysis struct
     */
    public static class BeaconAnalysis {
        private final double confidence;
        private final BeaconColor left;
        private final BeaconColor right;
        private final Rectangle location;

        //TODO Color and CONFIDENCE should make up the results

        //TODO add Size size, Point locationTopLeft, Distance distanceApprox

        /**
         * Instantiate a blank analysis
         */
        public BeaconAnalysis() {
            this.left = BeaconColor.UNKNOWN;
            this.right = BeaconColor.UNKNOWN;
            this.confidence = 0.0f;
            this.location = new Rectangle();
        }

        BeaconAnalysis(BeaconColor left, BeaconColor right, Rectangle location, double confidence) {
            this.left = left;
            this.right = right;
            this.confidence = confidence;
            this.location = location;
        }

        /**
         * Get the bounding box surrounding the beacon
         * @return Rectangle
         */
        public Rectangle getBoundingBox() {
            return location;
        }

        /**
         * Get the top left corner of the beacon
         * @return Point
         */
        public Point getTopLeft() {
            return location.topLeft();
        }

        /**
         * Get the bottomr ight corner of the beacon
         * @return Point
         */
        public Point getBottomRight() {
            return location.bottomRight();
        }

        /**
         * Get the center of the beacon
         * @return Point
         */
        public Point getCenter() {
            return location.center();
        }

        /**
         * Get the width of the beacon
         *
         * @return Width of the beacon
         */
        public double getWidth() {
            return location.width();
        }

        /**
         * Get the height of the beacon
         *
         * @return Height of the beacon
         */
        public double getHeight() {
            return location.height();
        }

        /**
         * Get the color of the left side of the beacon
         * @return Beacon color state
         */
        public BeaconColor getStateLeft() {
            return left;
        }

        /**
         * Get the color of the right side of the beacon
         * @return Beacon color state
         */
        public BeaconColor getStateRight() {
            return right;
        }

        /**
         * Get a confidence value that the beacon analysis is correct
         *
         * This is an approximation, but can be used carefully to filter out random noise.
         *
         * Also, only certain analysis methods provide a confidence - this will then return zero.
         * @return Confidence, if applicable - zero if not applicable
         */
        public double getConfidence() {
            return !Double.isNaN(confidence) ? MathUtil.coerce(0, 1, confidence) : 0.0;
        }

        /**
         * Get a confidence string that the beacon analysis is correct
         *
         * This is an approximation, but can be used carefully to filter out random noise.
         *
         * Also, only certain analysis methods provide a confidence - this will then return zero.
         * @return Confidence, if applicable - "N/A" if not applicable
         */
        public String getConfidenceString() {
            final DecimalFormat format = new DecimalFormat("0.000");
            return !Double.isNaN(confidence) ? format.format(MathUtil.coerce(0, 1, getConfidence()) * 100.0f) + "%" : "N/A";
        }

        /**
         * Test whether if the left side has a known color
         * @return True if the left side is NOT UNKNOWN
         */
        public boolean isLeftKnown() {
            return left != BeaconColor.UNKNOWN;
        }

        /**
         * Test whether if the left side is blue
         * @return True if the left side is BLUE or BLUE_BRIGHT
         */
        public boolean isLeftBlue() {
            return (left == BeaconColor.BLUE_BRIGHT || left == BeaconColor.BLUE);
        }

        /**
         * Test whether if the left side is red
         * @return True if the left side is RED or RED_BRIGHT
         */
        public boolean isLeftRed() {
            return (left == BeaconColor.RED_BRIGHT || left == BeaconColor.RED);
        }

        /**
         * Test whether if the right side has a known color
         * @return True if the right side is NOT UNKNOWN
         */
        public boolean isRightKnown() {
            return right != BeaconColor.UNKNOWN;
        }

        /**
         * Test whether if the right side is blue
         * @return True if the right side is BLUE or BLUE_BRIGHT
        */
        public boolean isRightBlue() {
            return (right == BeaconColor.BLUE_BRIGHT || right == BeaconColor.BLUE);
        }

        /**
         * Test whether if the right side is red
         * @return True if the right side is RED or RED_BRIGHT
         */
        public boolean isRightRed() {
            return (right == BeaconColor.RED_BRIGHT || right == BeaconColor.RED);
        }

        /**
         * Test whether the beacon is found
         * @return isLeftKnown() && isRightKnown()
         */
        public boolean isBeaconFound() {
            return isLeftKnown() && isRightKnown();
        }

        /**
         * Test whether the beacon is fully lit
         *
         * Note that in this revision, brightness is not supported
         * @return True if both sides have a bright component
         */
        public boolean isBeaconFullyLit() {
            return (left == BeaconColor.BLUE_BRIGHT || left == BeaconColor.RED_BRIGHT) &&
                    (right == BeaconColor.BLUE_BRIGHT || right == BeaconColor.RED_BRIGHT);
        }

        /**
         * Get a string representing the colors of the beacon
         * @return left, right
         */
        public String getColorString() {
            return left.toString() + ", " + right.toString();
        }

        /**
         * Get the location of the beacon as a string
         * @return Center of the beacon
         */
        public String getLocationString() {
            return getCenter().toString();
        }

        @Override
        public String toString() {
            return "Color: " + getColorString() + "\r\n Location: " + getLocationString() + "\r\n Confidence: " + getConfidenceString();
        }
    }
}
