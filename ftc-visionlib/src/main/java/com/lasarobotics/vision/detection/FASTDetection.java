package com.lasarobotics.vision.detection;

/**
 * FAST object detection
 */
public class FASTDetection {
    /**
     * Analyze and store an object instance
     * @param matGrayObject Grayscale target object image in grayscale
     */
    public native static void analyzeObject(long matGrayObject);

    /**
     * Find an object within a specific scene
     * @param matGrayObject Grayscale target object image
     * @param matGrayScene Grayscale scene to search for an object
     */
    public native static void findObject(long matGrayObject, long matGrayScene, long matOutput);
}
