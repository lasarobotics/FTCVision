package com.lasarobotics.vision.detection;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.features2d.KeyPoint;

/**
 * An object detection method that looks at features on a target object and an image to identify the location of the object
 */
public class FeatureDetector {

    public enum FeatureDetectorFlags
    {
        NORMAL(0),
        GRID(1000),
        PYRAMID(2000),
        DYNAMIC(3000);

        private int m;
        FeatureDetectorFlags(int type) { m = type; }

        public int val() { return m; }
    }

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
        FeatureDetectorType(int type, FeatureDetectorFlags flag) { m = type + flag.val(); }

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
        FLANNBASED(1),
        BRUTEFORCE(2),
        BRUTEFORCE_L1(3),
        BRUTEFORCE_HAMMING(4),
        BRUTEFORCE_HAMMINGLUT(5),
        BRUTEFORCE_SL2(6);

        private int m;
        DescriptorMatcherType(int type) { m = type; }

        public int val() { return m; }
    }

    FeatureDetectorType detector;
    DescriptorExtractorType extractor;
    DescriptorMatcherType matcher;

    public FeatureDetector()
    {
        detector = FeatureDetectorType.FAST;
        extractor = DescriptorExtractorType.BRIEF;
        matcher = DescriptorMatcherType.BRUTEFORCE_HAMMING;
    }
    public FeatureDetector(FeatureDetectorType detector, DescriptorExtractorType extractor, DescriptorMatcherType matcher)
    {
        this.detector = detector;
        this.extractor = extractor;
        this.matcher = matcher;
    }

    /**
     * Find features (keypoints) on an object or scene
     * @param img The input image
     * @return Returns an array of keypoints
     */
    public KeyPoint[] findKeypoints(Mat img)
    {

    }

    /**
     * Draw keypoints directly onto an image
     * @param keypoints List of keypoints obtained from findKeypoints()
     * @param output The image the keypoints should be drawn onto
     */
    public void drawKeypoints(KeyPoint[] keypoints, Mat output)
    {

    }

    Mat analyzeObject(Mat object)
    {

    }

    void locateObject(Mat scene, Mat object, Mat object_descriptors, Mat output) //TODO return object - for now, draw
    {

    }
    //void drawObject(Size size, Point point);


}