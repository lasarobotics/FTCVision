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

package com.lasarobotics.ftcrobotcontroller.opmodes;

import android.content.Context;

import com.lasarobotics.ftcrobotcontroller.FtcRobotControllerActivity;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.lasarobotics.vision.android.Util;
import org.lasarobotics.vision.detection.ColorBlobDetector;
import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.image.Transform;
import org.lasarobotics.vision.ui.VisionEnabledActivity;
import org.lasarobotics.vision.util.FPS;
import org.lasarobotics.vision.util.color.ColorHSV;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.List;

/**
 * TeleOp Mode
 * <p/>
 * Enables control of the robot via the gamepad
 */
public class VisionTest1 extends OpMode implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    Beacon.BeaconColorAnalysis colorAnalysis = new Beacon.BeaconColorAnalysis();
    Mat mRgba, mGray;
    int width, height;
    boolean initialized = false;

    private FPS fpsCounter;
    private ColorBlobDetector detectorRed;
    private ColorBlobDetector detectorBlue;
    private static final ColorHSV lowerBoundRed = new ColorHSV((int) (305 / 360.0 * 255.0), (int) (0.200 * 255.0), (int) (0.300 * 255.0));
    private static final ColorHSV upperBoundRed = new ColorHSV((int) ((360.0 + 5.0) / 360.0 * 255.0), 255, 255);
    private static final ColorHSV lowerBoundBlue = new ColorHSV((int) (170.0 / 360.0 * 255.0), (int) (0.200 * 255.0), (int) (0.750 * 255.0));
    private static final ColorHSV upperBoundBlue = new ColorHSV((int) (227.0 / 360.0 * 255.0), 255, 255);

    /*
     * Code to run when the op mode is first enabled goes here
     * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#start()
     */
    @Override
    public void init() {

        //Initialize camera view
        mOpenCvCameraView = VisionEnabledActivity.openCVCamera;
        mOpenCvCameraView.setCameraIndex(0); //SET BACK (MAIN) CAMERA
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableView();

        //Initialize FPS counter
        fpsCounter = new FPS();

        //Initialize all detectors here
        detectorRed = new ColorBlobDetector(lowerBoundRed, upperBoundRed);
        detectorBlue = new ColorBlobDetector(lowerBoundBlue, upperBoundBlue);

        initialized = true;
    }

    /*
     * This method will be called repeatedly in a loop
     * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#loop()
     */
    @Override
    public void loop() {
        telemetry.addData("Vision Color", colorAnalysis.toString());
    }

    /**
     * This method is invoked when camera preview has started. After this method is invoked
     * the frames will start to be delivered to client via the onCameraFrame() callback.
     *
     * @param width  -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * This method is invoked when camera preview has been stopped for some reason.
     * No frames will be delivered via onCameraFrame() callback after this method is called.
     */
    @Override
    public void onCameraViewStopped() {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    /**
     * This method is invoked when delivery of the frame needs to be done.
     * The returned values - is a modified frame which needs to be displayed on the screen.
     * TODO: pass the parameters specifying the format of the frame (BPP, YUV or RGB and etc)
     *
     * @param inputFrame
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (!initialized) { return inputFrame.rgba(); }

        // input frame has RGBA format
        mRgba = inputFrame.rgba();
        Mat mRgbaOriginal = inputFrame.rgba().clone();
        mGray = inputFrame.gray();
        //Size originalSize = mRgba.size();

        //DEBUG for the Nexus
        //Transform.flip(mRgba, Transform.FlipType.FLIP_BOTH);
        //Transform.flip(mGray, Transform.FlipType.FLIP_BOTH);

        //Transform.shrink(mRgba, new Size(480, 480), true);
        //Transform.shrink(mGray, new Size(480, 480), true);

        //DEBUG for the Moto G
        Transform.rotate(mGray, -90);
        Transform.rotate(mRgba, -90);

        fpsCounter.update();

        try {
            //Process the frame for the color blobs
            detectorRed.process(mRgba);
            detectorBlue.process(mRgba);

            //Get the list of contours
            List<Contour> contoursRed = detectorRed.getContours();
            List<Contour> contoursBlue = detectorBlue.getContours();

            //Get color analysis
            Beacon beacon = new Beacon(mRgba.size());
            colorAnalysis = beacon.analyzeColor(contoursRed, contoursBlue, mRgba, mGray);

        } catch (Exception e) {
            telemetry.addData("Vision Status", "Analysis Error");
            e.printStackTrace();
        }

        telemetry.addData("Vision FPS", fpsCounter.getFPSString());
        return mRgba;
    }

    @Override
    public void stop() {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
}
