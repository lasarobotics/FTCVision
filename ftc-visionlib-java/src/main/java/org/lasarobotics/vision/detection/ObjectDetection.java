package org.lasarobotics.vision.detection;

import android.util.Log;

import org.lasarobotics.vision.Drawing;
import org.lasarobotics.vision.Image;
import org.lasarobotics.vision.util.Color;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;

/**
 * Designed to detect a single object at a time in an image
 */
public class ObjectDetection {
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

    public ObjectDetection()
    {
        detector = FeatureDetector.create(FeatureDetectorType.FAST.val());
        extractor = DescriptorExtractor.create(DescriptorExtractorType.BRIEF.val());
        matcher = DescriptorMatcher.create(DescriptorMatcherType.BRUTEFORCE_HAMMING.val());
    }
    public ObjectDetection(FeatureDetectorType detector, DescriptorExtractorType extractor, DescriptorMatcherType matcher)
    {
        this.detector = FeatureDetector.create(detector.val());
        this.extractor = DescriptorExtractor.create(extractor.val());
        this.matcher = DescriptorMatcher.create(matcher.val());
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

    public final class SceneAnalysis
    {
        MatOfKeyPoint keypoints;
        Mat descriptors;
        MatOfDMatch matches;

        SceneAnalysis(MatOfKeyPoint keypoints, Mat descriptors, MatOfDMatch matches)
        {
            this.keypoints = keypoints;
            this.descriptors = descriptors;
            this.matches = matches;
        }
    }

    /**
     * Analyzes an object in preparation to search for the object in a frame.
     *
     * This method should be called in an initialize() method.
     * Calling the analyzeObject method twice will overwrite the previous objectAnalysis.
     *
     * It is recommended to use a GFTT (Good Features To Track) detector for this phase.
     * @param object Object image
     * @return The object descriptor matrix to be piped into locateObject() later
     */
    public ObjectAnalysis analyzeObject(Mat object)
    {
        Mat descriptors = new Mat();
        MatOfKeyPoint keypoints = new MatOfKeyPoint();

        Log.d("FTCVision", "Analyzing object...");

        if (object == null || object.empty())
        {
            throw new IllegalArgumentException("Object image cannot be empty!");
        }

        //Detect object keypoints
        detector.detect(object, keypoints);

        //Extract object keypoints
        extractor.compute(object, keypoints, descriptors);

        return new ObjectAnalysis(keypoints, descriptors);
    }

    /**
     * Analyzes a scene for a target object.
     * @param scene The scene to be analyzed as a GRAYSCALE matrix
     * @param analysis The target object's analysis from analyzeObject
     * @param output The output matrix, typically the rgba form of the scene matrix
     * @return A complete scene analysis
     */
    public SceneAnalysis analyzeScene(Mat scene, ObjectAnalysis analysis, Mat output) //TODO return object - for now, draw
    {
        MatOfKeyPoint keypointsScene = new MatOfKeyPoint();

        //DETECT KEYPOINTS in scene
        detector.detect(scene, keypointsScene);

        //EXTRACT KEYPOINT INFO from scene
        Mat descriptorsScene = new Mat();
        extractor.compute(scene, keypointsScene, descriptorsScene);

        if (analysis == null) {
            throw new IllegalArgumentException("Analysis must not be null!");
        }

        if(analysis.descriptors.cols() != descriptorsScene.cols() || analysis.descriptors.type() != descriptorsScene.type()) {
            throw new IllegalArgumentException("Object and scene descriptors do not match in cols() or type().");
        }

        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(analysis.descriptors, descriptorsScene, matches);

        //FILTER KEYPOINTS
         /*double max_dist = 0, min_dist = 100;

        for(int i = 0; i < objectAnalysis.descriptors.rows(); i++) {
            double dist = matches.get;
            if(dist < )
        }*/

        //STORE SCENE ANALYSIS
        return new SceneAnalysis(keypointsScene, descriptorsScene, matches);
    }

    /**
     * Draw keypoints directly onto an scene image - red circles indicate keypoints
     * @param output The scene matrix
     * @param sceneAnalysis Analysis of the scene, as given by analyzeScene()
     */
    public static void drawKeypoints(Mat output, SceneAnalysis sceneAnalysis)
    {
        KeyPoint[] keypoints = sceneAnalysis.keypoints.toArray();
        for (KeyPoint kp : keypoints) {
            Drawing.drawCircle(output, new Point(kp.pt.x, kp.pt.y), 4, new Color(255, 0, 0));
        }
    }

    public static void drawDebugInfo(Mat output, SceneAnalysis sceneAnalysis)
    {
        Image.flip(output, Image.FlipType.FLIP_ACROSS_Y);
        Drawing.drawText(output, "Keypoints: " + sceneAnalysis.keypoints.rows(), new Point(0, 8), 1.0f, new Color(255, 255, 255), Drawing.Anchor.BOTTOMLEFT_UNFLIPPED_Y);
        Image.flip(output, Image.FlipType.FLIP_ACROSS_Y);
    }


}
