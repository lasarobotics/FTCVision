package com.lasarobotics.camera.detection;

/**
 * Advanced object detection methods
 */
public class Detection {
    /**
     * Find an object within a specific scene
     * @param matGrayObject Grayscale target object image
     * @param matGrayScene Grayscale scene to search for an object
     */
    public native static void findObject(long matGrayObject, long matGrayScene);
}
