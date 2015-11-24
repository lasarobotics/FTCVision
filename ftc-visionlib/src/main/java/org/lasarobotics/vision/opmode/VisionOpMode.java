package org.lasarobotics.vision.opmode;

import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.opmode.extensions.BeaconColorExtension;
import org.opencv.core.Mat;
import org.opencv.core.Size;

/**
 * Easy-to-use, extensible vision op mode
 * For more custom implementations, use ManualVisionOpMode or modify core extensions in opmode.extensions.*
 */
public abstract class VisionOpMode extends VisionOpModeCore {

    private int extensions = 0;
    private boolean initialized = false;

    protected boolean isEnabled(VisionExtensions extension)
    {
        return (extensions & extension.id) > 0;
    }

    /*** EXTENSION-SPECIFIC CODE ***/
    private BeaconColorExtension beaconColorExtension = new BeaconColorExtension();
    public Beacon.BeaconAnalysis beaconColor = new Beacon.BeaconAnalysis(new Size());

    protected void enableExtension(VisionExtensions extension)
    {
        extensions = extensions | extension.id;

        //Don't initialize extension if we haven't ever called init() yet
        if (!initialized)
            return;

        if (extension == VisionExtensions.BEACON_COLOR)
            beaconColorExtension.init(this);
    }

    protected void disableExtension(VisionExtensions extension)
    {
        extensions -= extensions & extension.id;

        if (extension == VisionExtensions.BEACON_COLOR)
            beaconColorExtension.stop(this);
    }

    @Override
    public void init() {
        super.init();

        if (isEnabled(VisionExtensions.BEACON_COLOR))
            beaconColorExtension.init(this);

        initialized = true;
    }

    @Override
    public void loop() {
        super.loop();

        if (isEnabled(VisionExtensions.BEACON_COLOR))
            beaconColorExtension.loop(this);
    }

    @Override
    public Mat frame(Mat rgba, Mat gray) {
        if (isEnabled(VisionExtensions.BEACON_COLOR))
            beaconColorExtension.frame(this, rgba, gray);

        return rgba;
    }

    @Override
    public void stop() {
        super.stop();

        if (isEnabled(VisionExtensions.BEACON_COLOR))
            beaconColorExtension.stop(this);
    }
}
