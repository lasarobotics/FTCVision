package org.lasarobotics.vision.opmode.extensions;

import org.opencv.core.Mat;

/**
 * Vision extension
 */
public interface VisionExtension {
    void init(int width, int height);
    void run(Mat rgba, Mat gray, Mat output);
    void deinit();
}