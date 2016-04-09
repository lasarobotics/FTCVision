package com.lasarobotics.tests.camera;

import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.detection.objects.Rectangle;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.opmode.TestableVisionOpMode;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.lasarobotics.vision.util.color.ColorGRAY;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Vision OpMode run by the Camera Test Activity
 * Use TestableVisionOpModes in testing apps ONLY (but you can easily convert between opmodes just by changingt t
 */
public class CameraTestVisionOpMode extends TestableVisionOpMode {

    @Override
    public void init() {
        super.init();

        //Set the camera used for detection
        this.setCamera(Cameras.PRIMARY);

        //Set the frame size
        //Larger = sometimes more accurate, but also much slower
        //For Testable OpModes, this might make the image appear small - it might be best not to use this
        //After this method runs, it will set the "width" and "height" of the frame
        this.setFrameSize(new Size(900, 900));

        //Enable extensions. Use what you need.
        enableExtension(Extensions.BEACON);     //Beacon detection
        enableExtension(Extensions.ROTATION);   //Automatic screen rotation correction

        //UNCOMMENT THIS IF you're using a SECONDARY (facing toward screen) camera
        //or when you rotate the phone, sometimes the colors swap
        //rotation.setRotationInversion(true);

        //Set this to the default orientation of your program
        rotation.setDefaultOrientation(ScreenOrientation.LANDSCAPE);

        //Set the beacon analysis method
        //Try them all and see what works!
        beacon.setAnalysisMethod(Beacon.AnalysisMethod.FAST);

        //Debug drawing
        //Enable this only if you're running test app - otherwise, you should turn it off
        //(Although it doesn't harm anything if you leave it on, only slows down processing a tad)
        beacon.enableDebug();
    }

    @Override
    public void loop() {
        super.loop();

        //Telemetry won't work here, but you can still do logging
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public Mat frame(Mat rgba, Mat gray) {
        /*
          We set the Analysis boundary in the frame loop just in case we couldn't get it
          during init(). This happens when another app is using OpenCV simulataneously.
         */
        //Set analysis boundary
        //You should comment this to use the entire screen and uncomment only if
        //you want faster analysis at the cost of not using the entire frame.
        //This is also particularly useful if you know approximately where the beacon is
        //as this will eliminate parts of the frame which may cause problems
        //This will not work on some methods, such as COMPLEX
        Rectangle bounds = new Rectangle(new Point(width / 2, height / 2), width - 200, 200);
        beacon.setAnalysisBounds(bounds);
        //Or you can just use the entire screen
        //beacon.setAnalysisBounds(new Rectangle(0, 0, height, width));

        //Run all extensions, then get matrices
        rgba = super.frame(rgba, gray);
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY);

        //Get beacon analysis
        Beacon.BeaconAnalysis beaconAnalysis = beacon.getAnalysis();

        //Display confidence
        Drawing.drawText(rgba, "Confidence: " + beaconAnalysis.getConfidenceString(),
                new Point(0, 50), 1.0f, new ColorGRAY(255));

        //Display beacon color
        Drawing.drawText(rgba, beaconAnalysis.getColorString(),
                new Point(0, 8), 1.0f, new ColorGRAY(255), Drawing.Anchor.BOTTOMLEFT);

        //Display FPS
        Drawing.drawText(rgba, "FPS: " + fps.getFPSString(), new Point(0, 24), 1.0f, new ColorRGBA("#ffffff")); //"#2196F3"

        //Display analysis method
        Drawing.drawText(rgba, beacon.getAnalysisMethod().toString() + " Analysis",
                new Point(width - 300, 40), 1.0f, new ColorRGBA("#FFC107"));

        //Display rotation sensor compensation
        Drawing.drawText(rgba, "Rot: " + sensors.getScreenOrientationCompensation()
                + " (" + sensors.getScreenOrientation() + ")", new Point(0, 50), 1.0f, new ColorRGBA("#ffffff"), Drawing.Anchor.BOTTOMLEFT); //"#2196F3"

        return rgba;
    }
}