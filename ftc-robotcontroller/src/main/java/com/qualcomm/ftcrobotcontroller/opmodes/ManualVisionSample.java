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
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.opmode.ManualVisionOpMode;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.lasarobotics.vision.util.color.ColorHSV;
import org.opencv.core.Mat;
import org.opencv.core.Size;

/**
 * Manual Vision Sample
 *
 * Use when you need absolute control of each frame and want to customize
 * how Vision works for you. In a ManualVisionOpMode, you have far more control
 * and can even use the entirety of OpenCV for your own custom processing.
 *
 * Please note that you cannot use any Vision Extensions in a ManualVisionOpMode, but you
 * can still call the extensions' init(), loop(), and frame() methods if you want to use them,
 */
public class ManualVisionSample extends ManualVisionOpMode {

    private static final ColorHSV lowerBoundRed = new ColorHSV((int) (305 / 360.0 * 255.0), (int) (0.200 * 255.0), (int) (0.300 * 255.0));
    private static final ColorHSV upperBoundRed = new ColorHSV((int) ((360.0 + 5.0) / 360.0 * 255.0), 255, 255);
    private static final ColorHSV lowerBoundBlue = new ColorHSV((int) (170.0 / 360.0 * 255.0), (int) (0.200 * 255.0), (int) (0.750 * 255.0));
    private static final ColorHSV upperBoundBlue = new ColorHSV((int) (227.0 / 360.0 * 255.0), 255, 255);
    private Beacon.BeaconAnalysis colorAnalysis = new Beacon.BeaconAnalysis();
    private ColorBlobDetector detectorRed;
    private ColorBlobDetector detectorBlue;

    @Override
    public void init() {
        super.init();

        /* Initialize all detectors here */
        detectorRed = new ColorBlobDetector(lowerBoundRed, upperBoundRed);
        detectorBlue = new ColorBlobDetector(lowerBoundBlue, upperBoundBlue);

        /**
         * Set the camera used for detection
         * PRIMARY = Front-facing, larger camera
         * SECONDARY = Screen-facing, "selfie" camera :D
         **/
        this.setCamera(Cameras.PRIMARY);

        /**
         * Set the frame size
         * Larger = sometimes more accurate, but also much slower
         * After this method runs, it will set the "width" and "height" of the frame
         **/
        this.setFrameSize(new Size(900, 900));
    }

    @Override
    public void loop() {
        super.loop();

        telemetry.addData("Vision FPS", fps.getFPSString());
        telemetry.addData("Vision Color", colorAnalysis.getColorString());
        telemetry.addData("Analysis Confidence", colorAnalysis.getConfidenceString());
        telemetry.addData("Vision Size", "Width: " + width + " Height: " + height);
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public Mat frame(Mat rgba, Mat gray) {
        try {
            //Prepare beacon instance
            Beacon beacon = new Beacon(Beacon.AnalysisMethod.FAST);
            //You may need to change the Screen Orientation to your preference
            ScreenOrientation orientation = ScreenOrientation.LANDSCAPE_REVERSE;
            //Analyze the frame and return the analysis
            colorAnalysis = beacon.analyzeFrame(detectorBlue, detectorRed, rgba, gray,
                    orientation);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return rgba;
    }
}
