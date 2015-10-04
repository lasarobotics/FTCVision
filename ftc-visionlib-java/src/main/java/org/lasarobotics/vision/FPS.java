package org.lasarobotics.vision;

import org.opencv.core.Core;

import java.text.DecimalFormat;

/**
 * Allows counting and retrieving a frames per second count.
 */
public class FPS {
    private static final int FRAMES_BETWEEN_UPDATE = 20;
    private static final DecimalFormat FPS_FORMAT = new DecimalFormat("0.00");

    private int mFramesCounter;
    private double mFrequency;
    private long mprevFrameTime;
    private double mFps;

    public FPS()
    {
        mFramesCounter = 0;
        mFrequency = Core.getTickFrequency();
        mprevFrameTime = Core.getTickCount();

        mFps = 0;
    }

    /**
     * Update the FPS counter.
     *
     * Call this method EVERY FRAME!
     * @return The FPS, as a double in frames/second.
     */
    public void update() {

        mFramesCounter++;
        if (mFramesCounter % FRAMES_BETWEEN_UPDATE == 0) {
            long time = Core.getTickCount();
            mFps = FRAMES_BETWEEN_UPDATE * mFrequency / (time - mprevFrameTime);
            mprevFrameTime = time;
        }
    }

    /**
     * Get the frames per second count.
     * @return The FPS, as a double in frames/second.
     */
    public double getFPS()
    {
        return mFps;
    }

    public String getFPSString()
    {
        return FPS_FORMAT.format(mFps);
    }

    /**
     * Resets the FPS counter.
     */
    public void reset()
    {
        mFramesCounter = 0;
    }
}
