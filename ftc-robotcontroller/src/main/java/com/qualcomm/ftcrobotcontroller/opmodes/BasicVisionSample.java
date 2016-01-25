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
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.opencv.core.Size;

/**
 * TeleOp Mode
 * <p/>
 * Enables control of the robot via the gamepad
 */
public class BasicVisionSample extends VisionOpMode {

    @Override
    public void init() {
        super.init();

        //Set the camera used for detection
        this.setCamera(Cameras.SECONDARY);
        //Set the frame size
        //Larger = sometimes more accurate, but also much slower
        //For Testable OpModes, this might make the image appear small - it might be best not to use this
        this.setFrameSize(new Size(900, 900));

        //Enable extensions. Use what you need.
        enableExtension(Extensions.BEACON);     //Beacon detection
        enableExtension(Extensions.ROTATION);   //Automatic screen rotation correction

        //UNCOMMENT THIS IF you're using a SECONDARY (facing toward screen) camera
        //or when you rotate the phone, sometimes the colors swap
        rotation.setRotationInversion(true);

        //You can do this for certain phones which switch red and blue
        //It will rotate the display and detection by 180 degrees, making it upright
        //rotation.setUnbiasedOrientation(ScreenOrientation.LANDSCAPE_WEST);

        //Set the beacon analysis method
        //Try them all and see what works!
        beacon.setAnalysisMethod(Beacon.AnalysisMethod.FAST);
    }

    @Override
    public void loop() {
        super.loop();

        telemetry.addData("Beacon Color", beacon.getAnalysis().getColorString());
        telemetry.addData("Beacon Location (Center)", beacon.getAnalysis().getLocationString());
        telemetry.addData("Beacon Confidence", beacon.getAnalysis().getConfidenceString());
        telemetry.addData("Rotation Compensation", rotation.getRotationCompensationAngle());
        telemetry.addData("Frame Rate", fps.getFPSString() + " FPS");
        telemetry.addData("Frame Size", "Width: " + width + " Height: " + height);
    }

    @Override
    public void stop() {
        super.stop();
    }
}
