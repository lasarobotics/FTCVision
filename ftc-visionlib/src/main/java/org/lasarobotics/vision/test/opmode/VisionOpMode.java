package org.lasarobotics.vision.test.opmode;

import org.lasarobotics.vision.test.ftc.resq.Beacon;
import org.lasarobotics.vision.test.opmode.extensions.BeaconExtension;
import org.lasarobotics.vision.test.opmode.extensions.QRExtension;
import org.lasarobotics.vision.test.opmode.extensions.VisionExtension;
import org.opencv.core.Mat;

import java.util.HashMap;

/**
 * Easy-to-use, extensible vision op mode
 * For more custom implementations, use ManualVisionOpMode or modify core extensions in opmode.extensions.*
 */
public abstract class VisionOpMode extends VisionOpModeCore {

    /***
     * CUSTOM EXTENSION INITIALIZATION
     *
     * Add your extension here and in the Extensions class below!
     */
    protected static BeaconExtension beacon = new BeaconExtension();
    protected static QRExtension qr = new QRExtension();

    public enum Extensions {
        BEACON(1, beacon),
        QR(2, qr);

        final int id;
        VisionExtension instance;

        Extensions(int id, VisionExtension instance) {
            this.id = id;
            this.instance = instance;
        }
    }

    private int extensions = 0;
    private boolean initialized = false;

    protected boolean isEnabled(Extensions extension) {
        return (extensions & extension.id) > 0;
    }

    protected void enableExtension(Extensions extension) {
        //Don't initialize extension if we haven't ever called init() yet
        if (initialized)
            extension.instance.init(this);

        extensions = extensions | extension.id;
    }

    protected void disableExtension(Extensions extension) {
        extensions -= extensions & extension.id;

        extension.instance.stop(this);
    }

    @Override
    public void init() {
        super.init();

        for (Extensions extension : Extensions.values())
            if (isEnabled(extension))
                extension.instance.init(this);

        initialized = true;
    }

    @Override
    public void loop() {
        super.loop();

        for (Extensions extension : Extensions.values())
            if (isEnabled(extension))
                extension.instance.loop(this);
    }

    @Override
    public Mat frame(Mat rgba, Mat gray) {
        for (Extensions extension : Extensions.values())
            if (isEnabled(extension))
                extension.instance.frame(this, rgba, gray);

        return rgba;
    }

    @Override
    public void stop() {
        super.stop();

        for (Extensions extension : Extensions.values())
            if (isEnabled(extension))
                disableExtension(extension); //disable and stop
    }
}
