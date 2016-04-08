package com.lasarobotics.tests.camera;

import android.graphics.Color;
import android.hardware.Camera;
import android.os.Debug;
import android.util.Log;

//import org.lasarobotics.vision.opmode.VisionOpModeCore;
import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.detection.objects.Rectangle;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.opmode.TestableVisionOpMode;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.lasarobotics.vision.util.color.ColorGRAY;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;

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
        Camera.Parameters p = openCVCamera.getCamera().getParameters();
        p.setWhiteBalance("twilight");
        String output = Arrays.toString(p.getSupportedWhiteBalance().toArray());
        Log.e("valid white: ", output);
        //Set the frame size
        //Larger = sometimes more accurate, but also much slower
        //For Testable OpModes, this might make the image appear small - it might be best not to use this
        //After this method runs, it will set the "width" and "height" of the frame
        this.setFrameSize(new Size(900, 900));

        //Enable extensions. Use what you need.
        enableExtension(Extensions.BEACON);     //Beacon detection
        //enableExtension(Extensions.ROTATION);   //Automatic screen rotation correction

        //UNCOMMENT THIS IF you're using a SECONDARY (facing toward screen) camera
        //or when you rotate the phone, sometimes the colors swap
        //rotation.setRotationInversion(true);

        //Set this to the default orientation of your program
        rotation.setUnbiasedOrientation(ScreenOrientation.PORTRAIT);

        //Set the beacon analysis method
        //Try them all and see what works!
        beacon.setAnalysisMethod(Beacon.AnalysisMethod.FAST);

        //Set analysis boundary
        //You should comment this to use the entire screen and uncomment only if
        //you want faster analysis at the cost of not using the entire frame.
        //This is also particularly useful if you know approximately where the beacon is
        //as this will eliminate parts of the frame which may cause problems
        //This will not work on some methods, such as COMPLEX
        //Rectangle bounds = new Rectangle(new Point(width / 2, 330), width, 75);
        Rectangle bounds = new Rectangle(new Point(330.0/480 * 800, height / 2), 250, height);
        beacon.setAnalysisBounds(bounds);
        //Or you can just use the entire screen
        //beacon.setAnalysisBounds(new Rectangle(0, 0, width, height));

        //Debug drawing
        //Enable this only if you're running test app - otherwise, you should turn it off
        //(Although it doesn't harm anything if you leave it on, only slows down processing a tad)
        beacon.enableDebug();
    }

    @Override
    public void loop() {
        super.loop();
        //openCVCamera.getCamera().getParameters().setAutoExposureLock(true);
        //openCVCamera.disconnectCamera();
        Camera.Parameters p = openCVCamera.getCamera().getParameters();
        p.setWhiteBalance("daylight");
        p.setAutoWhiteBalanceLock(true);
        openCVCamera.getCamera().setParameters(p);
        Log.e("allow whitebalance? ", openCVCamera.getCamera().getParameters().isAutoWhiteBalanceLockSupported() + "");
        Log.e("current whitebalance: ", openCVCamera.getCamera().getParameters().getWhiteBalance());

        //Telemetry won't work here, but you can still do logging
    }

    @Override
    public Mat frame(Mat rgba, Mat gray) {
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

        //Display center of the beacon
        Drawing.drawText(rgba, "Center: " + beaconAnalysis.getCenter().toString(), new Point(0, 75), 1.0f, new ColorRGBA("#ffffff")); //"#2196F3"

        //Display height and width of the image
        Drawing.drawText(rgba, "Height: " + height + " Width: " + width, new Point(0, 100), 1.0f, new ColorRGBA("#ffffff")); //"#2196F3"

        //Display analysis method
        Drawing.drawText(rgba, beacon.getAnalysisMethod().toString() + " Analysis",
                new Point(width - 300, 40), 1.0f, new ColorRGBA("#FFC107"));

        //Display a Grid-system every 50 pixels
        /*int max = Math.max(width, height);
        for(int pixel = 0; pixel < max; pixel += 50)
        {
            if(pixel < width)
                Drawing.drawLine(rgba, new Point(pixel, 0), new Point(pixel, height), new ColorRGBA("#888888"), 2);
            if(pixel < height)
                Drawing.drawLine(rgba, new Point(0, pixel), new Point(width, pixel), new ColorRGBA("#888888"), 2);
        }*/

        //Display rotation sensor compensation
        Drawing.drawText(rgba, "Rot: " + sensors.getScreenOrientationCompensation() + "("
                + sensors.getActivityScreenOrientation().getAngle() + " act, "
                + sensors.getScreenOrientation().getAngle() + " sen)", new Point(0, 50), 1.0f, new ColorRGBA("#ffffff"), Drawing.Anchor.BOTTOMLEFT); //"#2196F3"

        return rgba;
    }
}