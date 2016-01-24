package org.lasarobotics.vision.opmode;

import org.lasarobotics.vision.opmode.extensions.BeaconExtension;
import org.lasarobotics.vision.opmode.extensions.DistanceLinearizationExtension;
import org.lasarobotics.vision.opmode.extensions.ImageRotationExtension;
import org.lasarobotics.vision.opmode.extensions.QRExtension;
import org.lasarobotics.vision.opmode.extensions.VisionExtension;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

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
    public static BeaconExtension beacon = new BeaconExtension();
    public static QRExtension qr = new QRExtension();
    public static ImageRotationExtension rotation = new ImageRotationExtension();
    public static DistanceLinearizationExtension distance = new DistanceLinearizationExtension();

    private boolean enableOpenCV = true;
    /**
     * END OF CUSTOM EXTENSION INITIALIZATION
     */

    private int extensions = 0;
    private boolean extensionsInitialized = false;

    public VisionOpMode() {
        super();
    }

    protected VisionOpMode(boolean enableOpenCV) {
        super();
        this.enableOpenCV = enableOpenCV;
    }

    protected boolean isEnabled(Extensions extension) {
        return (extensions & extension.id) > 0;
    }

    protected void enableExtension(Extensions extension) {
        //Don't initialize extension if we haven't ever called init() yet
        if (extensionsInitialized)
            extension.instance.init(this);

        extensions = extensions | extension.id;
    }

    protected void disableExtension(Extensions extension) {
        extensions -= extensions & extension.id;

        extension.instance.stop(this);
    }

    @Override
    public void init() {
        if (enableOpenCV) super.init();

        for (Extensions extension : Extensions.values())
            if (isEnabled(extension))
                extension.instance.init(this);

        extensionsInitialized = true;
    }

    @Override
    public void loop() {
        if (enableOpenCV) super.loop();

        for (Extensions extension : Extensions.values())
            if (isEnabled(extension))
                extension.instance.loop(this);
    }

    @Override
    public Mat frame(Mat rgba, Mat gray) {
        for (Extensions extension : Extensions.values())
            if (isEnabled(extension)) {
                //Pipe the rgba of the previous point into the gray of the next
                Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY);
                extension.instance.frame(this, rgba, gray);
            }

        return rgba;
    }

    @Override
    public void stop() {
        super.stop();

        for (Extensions extension : Extensions.values())
            if (isEnabled(extension))
                disableExtension(extension); //disable and stop
    }

    public double getFPS() {
        return fps.getFPS();
    }

    public BeaconExtension getBeaconState() {
        return beacon;
    }

    public enum Extensions {
        BEACON(2, beacon),
        DISTANCE(4, distance),
        QR(8, qr),             //low priority
        ROTATION(1, rotation); //high priority - image must rotate prior to analysis

        final int id;
        VisionExtension instance;

        Extensions(int id, VisionExtension instance) {
            this.id = id;
            this.instance = instance;
        }
    }
}
