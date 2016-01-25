package org.lasarobotics.vision.opmode.extensions;

import org.lasarobotics.vision.detection.ColorBlobDetector;
import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.ftc.resq.Constants;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.opencv.core.Mat;

import java.util.List;

/**
 * Extension that supports finding and reading beacon color data
 */
public class BeaconExtension implements VisionExtension {
    private ColorBlobDetector detectorRed;
    private ColorBlobDetector detectorBlue;

    private Beacon.BeaconAnalysis analysis = new Beacon.BeaconAnalysis();
    private Beacon.AnalysisMethod analysisMethod = Beacon.AnalysisMethod.DEFAULT;

    public Beacon.BeaconAnalysis getAnalysis() {
        return analysis;
    }

    public Beacon.AnalysisMethod getAnalysisMethod() {
        return analysisMethod;
    }

    public void setAnalysisMethod(Beacon.AnalysisMethod method) {
        this.analysisMethod = method;
    }

    public void init(VisionOpMode opmode) {
        //Initialize all detectors here
        detectorRed = new ColorBlobDetector(Constants.COLOR_RED_LOWER, Constants.COLOR_RED_UPPER);
        detectorBlue = new ColorBlobDetector(Constants.COLOR_BLUE_LOWER, Constants.COLOR_BLUE_UPPER);
    }

    public void loop(VisionOpMode opmode) {

    }

    public Mat frame(VisionOpMode opmode, Mat rgba, Mat gray) {
        try {
            //Process the frame for the color blobs
            detectorRed.process(rgba);
            detectorBlue.process(rgba);

            //Get the list of contours
            List<Contour> contoursRed = detectorRed.getContours();
            List<Contour> contoursBlue = detectorBlue.getContours();

            //Get screen orientation data
            ScreenOrientation orientation = ScreenOrientation.getFromAngle(
                    VisionOpMode.rotation.getRotationCompensationAngleBiased());

            //Get color analysis
            Beacon beacon = new Beacon(analysisMethod);
            this.analysis = beacon.analyzeFrame(contoursRed, contoursBlue, rgba, gray, orientation);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rgba;
    }

    @Override
    public void stop(VisionOpMode opmode) {

    }
}