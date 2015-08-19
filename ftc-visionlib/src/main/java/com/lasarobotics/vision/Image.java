package com.lasarobotics.vision;

/**
 * Image manipulation and correction
 */
public class Image {
    static native void rotate(long addrImage, double angle);
}
