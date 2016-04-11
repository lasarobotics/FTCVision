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
import org.lasarobotics.vision.detection.objects.Rectangle;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.opmode.LinearVisionOpMode;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.opencv.core.Mat;
import org.opencv.core.Size;

/**
 * TeleOp Mode
 * <p/>
 * Enables control of the robot via the gamepad
 */
public class LinearVisionSample extends LinearVisionOpMode {

    //Frame counter
    int frameCount = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        //Wait for vision to initialize - this should be the first thing you do
        waitForVisionStart();

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

        //Set this to the default orientation of your program (it's probably PORTRAIT)
        //Also, it's recommended to turn OFF Auto Rotate
        //If you can't get any readings or swap red and blue, try changing this
        rotation.setDefaultOrientation(ScreenOrientation.PORTRAIT);

        //Set the beacon analysis method
        //Try them all and see what works!
        beacon.setAnalysisMethod(Beacon.AnalysisMethod.FAST);

        //Set analysis boundary
        //You should comment this to use the entire screen and uncomment only if
        //you want faster analysis at the cost of not using the entire frame.
        //This is also particularly useful if you know approximately where the beacon is
        //as this will eliminate parts of the frame which may cause problems
        //This will not work on some methods, such as COMPLEX
        //Rectangle bounds = new Rectangle(new Point(width / 2, height / 2), width - 200, 200);
        //beacon.setAnalysisBounds(bounds);
        //Or you can just use the entire screen
        beacon.setAnalysisBounds(new Rectangle(getFrameSize()));

        //Wait for the match to begin
        waitForStart();

        //Main loop
        //Camera frames and OpenCV analysis will be delivered to this method as quickly as possible
        //This loop will exit once the opmode is closed
        while (opModeIsActive()) {
            //Log a few things
            telemetry.addData("Beacon Color", beacon.getAnalysis().getColorString());
            telemetry.addData("Beacon Location (Center)", beacon.getAnalysis().getLocationString());
            telemetry.addData("Beacon Confidence", beacon.getAnalysis().getConfidenceString());
            telemetry.addData("Rotation Compensation", rotation.getRotationCompensationAngle());
            telemetry.addData("Frame Rate", fps.getFPSString() + " FPS");
            telemetry.addData("Frame Size", "Width: " + width + " Height: " + height);
            telemetry.addData("Frame Counter", frameCount);

            //You can access the most recent frame data and modify it here using getFrameRgba() or getFrameGray()
            //Vision will run asynchronously (parallel) to any user code so your programs won't hang
            //You can use hasNewFrame() to test whether vision processed a new frame
            //Once you copy the frame, discard it immediately with discardFrame()
            if (hasNewFrame()) {
                //Get the frame
                Mat rgba = getFrameRgba();
                Mat gray = getFrameGray();

                //Discard the current frame to allow for the next one to render
                discardFrame();

                //Do all of your custom frame processing here
                //For this demo, let's just add to a frame counter
                frameCount++;
            }

            //Wait for a hardware cycle to allow other processes to run
            waitOneFullHardwareCycle();
        }
    }
}
