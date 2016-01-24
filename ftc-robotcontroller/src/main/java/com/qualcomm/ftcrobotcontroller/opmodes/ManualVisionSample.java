/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.qualcomm.ftcrobotcontroller.opmodes;

import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.detection.ColorBlobDetector;
import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.opmode.ManualVisionOpMode;
import org.lasarobotics.vision.util.color.ColorGRAY;
import org.lasarobotics.vision.util.color.ColorHSV;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.List;

/**
 * TeleOp Mode
 * <p/>
 * Enables control of the robot via the gamepad
 */
public class ManualVisionSample extends ManualVisionOpMode {

    private static final ColorHSV lowerBoundRed = new ColorHSV((int) (305 / 360.0 * 255.0), (int) (0.200 * 255.0), (int) (0.300 * 255.0));
    private static final ColorHSV upperBoundRed = new ColorHSV((int) ((360.0 + 5.0) / 360.0 * 255.0), 255, 255);
    private static final ColorHSV lowerBoundBlue = new ColorHSV((int) (170.0 / 360.0 * 255.0), (int) (0.200 * 255.0), (int) (0.750 * 255.0));
    private static final ColorHSV upperBoundBlue = new ColorHSV((int) (227.0 / 360.0 * 255.0), 255, 255);
    private Beacon.BeaconAnalysis colorAnalysis = new Beacon.BeaconAnalysis();
    private ColorBlobDetector detectorRed;
    private ColorBlobDetector detectorBlue;
    private boolean noError = true;

    @Override
    public void init() {
        super.init();

        //Initialize all detectors here
        detectorRed = new ColorBlobDetector(lowerBoundRed, upperBoundRed);
        detectorBlue = new ColorBlobDetector(lowerBoundBlue, upperBoundBlue);

        //Set the camera used for detection
        this.setCamera(Cameras.SECONDARY);
        //Set the frame size
        //Larger = sometimes more accurate, but also much slower
        //For Testable OpModes, this might make the image appear small - it might be best not to use this
        this.setFrameSize(new Size(900, 900));
    }

    @Override
    public void loop() {
        super.loop();

        telemetry.addData("Vision FPS", fps.getFPSString());
        telemetry.addData("Vision Color", colorAnalysis.toString());
        telemetry.addData("Analysis Confidence", colorAnalysis.getConfidenceString());
        telemetry.addData("Vision Size", "Width: " + width + " Height: " + height);
        telemetry.addData("Vision Status", noError ? "OK!" : "ANALYSIS ERROR!");
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public Mat frame(Mat rgba, Mat gray) {
        try {
            //Process the frame for the color blobs
            detectorRed.process(rgba);
            detectorBlue.process(rgba);

            //Get the list of contours
            List<Contour> contoursRed = detectorRed.getContours();
            List<Contour> contoursBlue = detectorBlue.getContours();

            //Get color analysis
            Beacon beacon = new Beacon(Beacon.AnalysisMethod.DEFAULT);
            colorAnalysis = beacon.analyzeFrame(contoursRed, contoursBlue, rgba, gray);

            //Draw red and blue contours
            Drawing.drawContours(rgba, contoursRed, new ColorRGBA(255, 0, 0), 2);
            Drawing.drawContours(rgba, contoursBlue, new ColorRGBA(0, 0, 255), 2);

            //Transform.enlarge(mRgba, originalSize, true);
            //Transform.enlarge(mGray, originalSize, true);

            //Draw text
            Drawing.drawText(rgba, colorAnalysis.toString(),
                    new Point(0, 8), 1.0f, new ColorGRAY(255), Drawing.Anchor.BOTTOMLEFT);

            noError = true;
        }
        catch (Exception e)
        {
            Drawing.drawText(rgba, "Analysis Error", new Point(0, 8), 1.0f, new ColorRGBA("#F44336"), Drawing.Anchor.BOTTOMLEFT);
            noError = false;
            e.printStackTrace();
        }

        Drawing.drawText(rgba, "FPS: " + fps.getFPSString(), new Point(0, 24), 1.0f, new ColorRGBA("#ffffff")); //"#2196F3"

        return rgba;
    }
}
