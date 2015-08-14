package com.lasarobotics.vision.detection;

/**
 * Implememts feature detection
 */
public class Features {
    /**
     * Gets a test string from JNI
     * @return Test string "Hello from JNI"
     */
    public static native String stringFromJNI();

    /**
     * Create a small circle around the detected features
     * @param addrGray Memory address of the frame's gray bitmap - acquire using CameraBridgeViewBase.CvCameraViewFrame.gray().getNativeObjAddr()
     * @param addrRGBA Memory address of the frame's RGBA bitmap - acquire using CameraBridgeViewBase.CvCameraViewFrame.rgba().getNativeObjAddr()
     */
    public static native void highlightFeatures(long addrGray, long addrRGBA);
}
