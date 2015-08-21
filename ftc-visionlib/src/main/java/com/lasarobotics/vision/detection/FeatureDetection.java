package com.lasarobotics.vision.detection;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;

/**
 * An object detection method that looks at features on a target object and an image to identify the location of the object
 */
public class FeatureDetection {
    public enum FeatureDetectorType
    {
        FAST(1),
        STAR(2),
        SIFT(3),
        SURF(4),
        ORB(5),
        MSER(6),
        GFTT(7),
        HARRIS(8),
        SIMPLEBLOB(9),
        DENSE(10),
        BRISK(11);

        private int m;
        FeatureDetectorType(int type) { m = type; }
        FeatureDetectorType(int type, boolean dynamic) { m = type + (dynamic ? 3000 : 0); }

        public int val() { return m; }
    }

    public enum DescriptorExtractorType
    {
        SIFT(1),
        SURF(2),
        ORB(3),
        BRIEF(4),
        BRISK(5),
        FREAK(6);

        private int m;
        DescriptorExtractorType(int type) { m = type; }
        DescriptorExtractorType(int type, boolean opponent) { m = type + (opponent ? 1000 : 0); }

        public int val() { return m; }
    }

    public enum DescriptorMatcherType
    {
        FLANN(1),
        BRUTEFORCE(2),
        BRUTEFORCE_L1(3),
        BRUTEFORCE_HAMMING(4),
        BRUTEFORCE_HAMMINGLUT(5),
        BRUTEFORCE_SL2(6);

        private int m;
        DescriptorMatcherType(int type) { m = type; }

        public int val() { return m; }
    }

    FeatureDetector detector;
    DescriptorExtractor extractor;
    DescriptorMatcher matcher;

    public FeatureDetection()
    {
        detector = FeatureDetector.create(FeatureDetectorType.FAST.val());
        extractor = DescriptorExtractor.create(DescriptorExtractorType.BRIEF.val());
        matcher = DescriptorMatcher.create(DescriptorMatcherType.BRUTEFORCE_HAMMING.val());
    }
    public FeatureDetection(FeatureDetectorType detector, DescriptorExtractorType extractor, DescriptorMatcherType matcher)
    {
        this.detector = FeatureDetector.create(detector.val());
        this.extractor = DescriptorExtractor.create(extractor.val());
        this.matcher = DescriptorMatcher.create(matcher.val());
    }

    /**
     * Find features (keypoints) on an object or scene
     * @param img The input image
     * @return Returns an array of keypoints
     */
    public KeyPoint[] findKeypoints(Mat img)
    {
        MatOfKeyPoint keys = new MatOfKeyPoint();
        detector.detect(img, keys);
        return keys.toArray();
    }

    /**
     * Draw keypoints directly onto an image - red circles indicate keypoints
     * @param keypoints List of keypoints obtained from findKeypoints()
     * @param output The image the keypoints should be drawn onto
     */
    public void drawKeypoints(KeyPoint[] keypoints, Mat output)
    {
        MatOfKeyPoint keys = new MatOfKeyPoint();
        keys.fromArray(keypoints);
        drawKeypoints(output.getNativeObjAddr(), keys.getNativeObjAddr());
    }

    public final class ObjectAnalysis
    {
        MatOfKeyPoint keypoints;
        Mat descriptors;

        ObjectAnalysis(MatOfKeyPoint keypoints, Mat descriptors)
        {
            this.keypoints = keypoints;
            this.descriptors = descriptors;
        }
    }

    /**
     * Analyzes an object in preparation to search for the object in a frame.
     *
     * It is recommended to use a GFTT (Good Features To Track) detector for this phase.
     * @param object Object image
     * @return The object descriptor matrix to be piped into locateObject() later
     */
    public ObjectAnalysis analyzeObject(Mat object)
    {
        Mat descriptors = new Mat();
        MatOfKeyPoint keys = new MatOfKeyPoint();
        analyzeObject(detector.getNativeObj(), extractor.getNativeObj(), object.getNativeObjAddr(), descriptors.getNativeObjAddr(), keys.getNativeObjAddr());
        return new ObjectAnalysis(keys, descriptors);
    }

    /**
     *
     * @param scene
     * @param object
     * @param analysis
     * @param output
     */
    public void locateObject(Mat scene, Mat object, ObjectAnalysis analysis, Mat output) //TODO return object - for now, draw
    {
        locateObject(detector.getNativeObj(), extractor.getNativeObj(), matcher.getNativeObj(),
                     analysis.descriptors.getNativeObjAddr(), analysis.keypoints.getNativeObjAddr(),
                     scene.getNativeObjAddr(), output.getNativeObjAddr());
    }

    //void drawObject(Size size, Point point);

    private static native void drawKeypoints(long outMat, long keypointMat);
    private static native void analyzeObject(long detector, long extractor, long objMat, long descriptorMat, long keypointsMat);
    private static native void locateObject(long detector, long extractor, long matcher, long descriptorMat, long keypointsObject, long sceneMat, long outMat); //TODO long for returning points or an object instance
}