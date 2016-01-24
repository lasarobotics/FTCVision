package org.lasarobotics.vision.ftc.resq;

import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.detection.objects.Rectangle;
import org.lasarobotics.vision.util.MathUtil;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Beacon location and analysis
 */
public final class Beacon {

    private AnalysisMethod method;

    public Beacon() {
        method = AnalysisMethod.FAST;
    }

    public Beacon(AnalysisMethod method) {
        this.method = method;
    }

    public BeaconAnalysis analyzeFrame(List<Contour> contoursRed, List<Contour> contoursBlue,
                                       Mat img, Mat gray, BeaconAnalysis prevAnalysis) {
        return analyzeFrame(contoursRed, contoursBlue, img, gray, prevAnalysis, ScreenOrientation.DEFAULT);
    }

    public BeaconAnalysis analyzeFrame(List<Contour> contoursRed, List<Contour> contoursBlue,
                                       Mat img, Mat gray, BeaconAnalysis prevAnalysis, ScreenOrientation orientation) {
        switch (method) {
            case FAST:
            case DEFAULT:
            default:
                return BeaconAnalyzer.analyze_FAST(contoursRed, contoursBlue, img, gray, prevAnalysis, orientation);
            case COMPLEX:
                return BeaconAnalyzer.analyze_COMPLEX(contoursRed, contoursBlue, img, gray, prevAnalysis, orientation);
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
        private BeaconDistanceAnalysis distanceAnalyzer;
        private double confidence;
        private BeaconColor left;
        private BeaconColor right;
        private Rectangle location;

        //TODO add Size size, Point locationTopLeft, Distance distanceApprox
        public BeaconAnalysis() {
            this.left = BeaconColor.UNKNOWN;
            this.right = BeaconColor.UNKNOWN;
            this.confidence = 0.0f;
            this.location = new Rectangle();
            distanceAnalyzer = null;
        }

        public BeaconAnalysis(BeaconAnalysis prev, double distance)
        {
            this.left = prev.left;
            this.right = prev.right;
            this.location = prev.location;
            this.confidence = prev.confidence;
            this.distanceAnalyzer = prev.distanceAnalyzer;
            this.distanceAnalyzer.distance = distance;
        }

        public BeaconAnalysis(BeaconColor left, BeaconColor right, Rectangle location, double confidence) {
            this.left = left;
            this.right = right;
            this.confidence = confidence;
            this.location = location;
            this.distanceAnalyzer = null;
        }

        public BeaconAnalysis(BeaconColor left, BeaconColor right, Rectangle location, double confidence, BeaconDistanceAnalysis analyzer) {
            this.left = left;
            this.right = right;
            this.confidence = confidence;
            this.location = location;
            this.distanceAnalyzer = analyzer;
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

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public String getConfidenceString() {
            final DecimalFormat format = new DecimalFormat("0.000");
            return !Double.isNaN(confidence) ? format.format(MathUtil.coerce(0, 1, getConfidence()) * 100.0f) + "%" : "N/A";
        }

        /**
         * Get the approximate distance to the object in feet
         *
         * @return Distance in feet
         */
        public double getDistance() {
            return distanceAnalyzer == null ? 0.0 : distanceAnalyzer.distance;
        }

        /**
         * Get the approximate distance to the object in feet
         *
         * @return Distance in feet
         */
        public String getDistanceString() {
            return distanceAnalyzer == null ? "N/A" : distanceAnalyzer.distance + " ft";
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

    /**
     * Baacon distance analyzer
     */
    public static class BeaconDistanceAnalysis {
        //Position analysis
        //double phi; //Stores are angle relative to the wall to the right of the beacon
        //double theta; //Stores are angular offset relative to when we are looking straight at the beacon

        Size imageSize = new Size();
        double distance = 0; //The distance (radius) from us to the beacon
        //Are we looking at the same beacon as previously?
        boolean sameBeacon = false;
        //Physical properties of the beacon, on screen, in pixels
        Size beaconPixelsSize = new Size();
        double buttonHeight = 0.0;
        //Derivative of width of beacon in pixels with respect to theta
        double dPixelsWdTheta = 0.0;

        public BeaconDistanceAnalysis() {

        }

        public BeaconDistanceAnalysis(BeaconDistanceAnalysis prev, double distance) {

        }

        //If no variables are changing call this method so the system can get a sense of the variance due to noise
        public void stationaryNoiseUpdate(double currWidth, double currHeight, boolean useButton) {
            //If no position was changed, average the current values
            beaconPixelsSize.width = (beaconPixelsSize.width + currWidth) / 2;
            beaconPixelsSize.height = (beaconPixelsSize.height + currHeight) / 2;
            //Recalculate distance
            calculateDistance(useButton);
        }

        //Call if robot is moving
        public void nonStationaryUpdate(double currHeight, double currButtonHeight, boolean useButton) {
            beaconPixelsSize.height = currHeight;
            buttonHeight = (currButtonHeight != 0) ? currButtonHeight : buttonHeight;
            calculateDistance(useButton);
        }

        //Call this each time theta is changed to get real time update of the derivative of width
        public void updatedPixelsWdTheta(double currWidth, boolean wasdThetaPositive) {
            dPixelsWdTheta = (wasdThetaPositive) ? currWidth - beaconPixelsSize.width : beaconPixelsSize.width - currWidth;
            beaconPixelsSize.width = currWidth;
        }

        private void calculateDistance(boolean useButton) {
            double tempRadius;
            if (useButton)
                tempRadius = Constants.BEACON_BUTTON_HEIGHT / (2 * Math.tan((Constants.CAMERA_VERT_VANGLE * buttonHeight) / (2 * imageSize.height)));
            else
                tempRadius = Constants.BEACON_HEIGHT / (2 * Math.tan((Constants.CAMERA_VERT_VANGLE * beaconPixelsSize.height) / (2 * imageSize.height)));
            if (sameBeacon)
                tempRadius = Math.abs(tempRadius - distance) * Constants.CM_FT_SCALE < Constants.DIST_CHANGE_THRESHOLD ? tempRadius : distance;
            distance = (tempRadius * Constants.CM_FT_SCALE < Constants.MAX_DIST_FROM_BEACON) ? tempRadius : distance;
        }
    }
}
