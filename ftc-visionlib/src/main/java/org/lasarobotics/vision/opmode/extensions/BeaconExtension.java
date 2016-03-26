package org.lasarobotics.vision.opmode.extensions;

import org.lasarobotics.vision.detection.objects.Rectangle;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.opencv.core.Mat;

/**
 * Extension that supports finding and reading beacon color data
 */
public class BeaconExtension implements VisionExtension {
    private Beacon beacon;

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

    public void setAnalysisBounds(Rectangle bounds) {
        beacon.setAnalysisBounds(bounds);
    }

    public void enableDebug() {
        beacon.enableDebug();
    }

    public void disableDebug() {
        beacon.disableDebug();
    }

    public void init(VisionOpMode opmode) {
        //Initialize all detectors here
        beacon = new Beacon(analysisMethod);
    }

    public void loop(VisionOpMode opmode) {

    }

    public Mat frame(VisionOpMode opmode, Mat rgba, Mat gray) {
        try {
            //Get screen orientation data
            ScreenOrientation orientation = ScreenOrientation.getFromAngle(
                    VisionOpMode.rotation.getRotationCompensationAngleBiased());

            //Get color analysis
            this.analysis = beacon.analyzeFrame(rgba, gray, orientation);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rgba;
    }

    @Override
    public void stop(VisionOpMode opmode) {

    }
}