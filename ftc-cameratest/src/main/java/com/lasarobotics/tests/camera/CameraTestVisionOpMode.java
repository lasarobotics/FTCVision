package com.lasarobotics.tests.camera;

import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.opmode.TestableVisionOpMode;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.lasarobotics.vision.util.color.ColorGRAY;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.core.Mat;
import org.opencv.core.Point;
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
        //this.setFrameSize(new Size(900, 900));

        //Enable extensions. Use what you need.
        enableExtension(VisionOpMode.Extensions.BEACON);     //Beacon detection
        //enableExtension(VisionOpMode.Extensions.QR);         //QR Code detection
        enableExtension(VisionOpMode.Extensions.ROTATION);   //Automatic screen rotation correction

        //You can do this for certain phones which switch red and blue
        //It will rotate the display and detection by 180 degrees, making it upright
        rotation.setUnbiasedOrientation(ScreenOrientation.LANDSCAPE_WEST);
    }

    @Override
    public void loop() {
        super.loop();

        //Telemetry won't work here
        /*telemetry.addData("Beacon Color", beacon.getAnalysis().getColorString());
        telemetry.addData("Beacon Location (Center)", beacon.getAnalysis().getLocationString());
        telemetry.addData("Beacon Confidence", beacon.getAnalysis().getConfidenceString());
        telemetry.addData("QR Error", qr.getErrorReason());
        telemetry.addData("QR String", qr.getText());
        telemetry.addData("Rotation Compensation", rotation.getRotationAngle());
        telemetry.addData("Frame Rate", fps.getFPSString() + " FPS");
        telemetry.addData("Frame Size", "Width: " + width + " Height: " + height);*/
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

        //Display rotation sensor compensation
        Drawing.drawText(rgba, "Rot: " + sensors.getScreenOrientationCompensation() + "("
                + sensors.getActivityScreenOrientation().getAngle() + " act, "
                + sensors.getScreenOrientation().getAngle() + " sen)", new Point(0, 50), 1.0f, new ColorRGBA("#ffffff"), Drawing.Anchor.BOTTOMLEFT); //"#2196F3"

        return rgba;
    }
}