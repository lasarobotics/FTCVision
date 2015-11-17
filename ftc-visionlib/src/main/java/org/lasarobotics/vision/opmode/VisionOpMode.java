package org.lasarobotics.vision.opmode;

import org.lasarobotics.vision.opmode.VisionOpModeCore;
import org.lasarobotics.vision.opmode.extensions.VisionExtension;
import org.lasarobotics.vision.opmode.extensions.VisionExtensions;
import org.opencv.core.Mat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Easy-to-use vision op mode
 * For more custom implementations, use ManualVisionOpMode or create extensions (see opmode.extensions.VisionExtension)
 */
public abstract class VisionOpMode extends VisionOpModeCore {

    HashMap<VisionExtensions, VisionExtension> extensions;

    final protected void enableExtension(VisionExtensions extension)
    {
        if (!extensions.containsKey(extension)) {

            Class<? extends VisionExtension> c = extension.getExtension();
            VisionExtension v;

            try {
                v = c.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            v.init(width, height);

            extensions.put(extension, v);
        }
    }

    final protected void resetExtension(VisionExtensions extension)
    {

    }

    final protected void disableExtension(VisionExtensions extension)
    {
        if (extensions.containsKey(extension)) {
            extensions.remove(extension).deinit();
        }
    }

    final protected VisionExtension getExtension(VisionExtension extension)
    {

    }

    @Override
    public final void init(int width, int height) {
        super.init();

        init();
    }

    @Override
    public void loop() {

    }

    @Override
    public void stop(boolean success) {

    }

    @Override
    public Mat frame(Mat rgba, Mat gray) {
        return null;
    }
    
    public abstract void init();
}
