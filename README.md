![FTC Vision](https://raw.githubusercontent.com/lasarobotics/ftcvision/dev/img/logo-github.png)

# FTC Vision Library [![Build Status](https://travis-ci.org/lasarobotics/FTCVision.svg?branch=staging)](https://travis-ci.org/lasarobotics/FTCVision)
Computer Vision library for FTC based on OpenCV, featuring **beacon color and position detection**, as well as an easy-to-use `VisionOpMode` format and many additional detection features planned in the future.

## Installing from Scratch

1. Clone FTCVision into a clean directory (outside your robot controller app) using the following command: `git clone --depth=1 https://github.com/lasarobotics/ftcvision`.
2. Open the FTCVision project using Android Studio
3. Copy your OpModes from your robot controller directory into the appropriate directory within `ftc-robotcontroller`. Then, modify the `FtcOpModeRegister` appropriately to add your custom OpModes.
4. You can now write your own custom `VisionOpMode`!

## Installing into Existing Project

*Info on installing into existing project coming soon. For now, following the directions in `Installing from Scratch`.*

## Status
This library is currently under insanely active development. We're in the **Beta** phase right now. If you have any questions or would like to help, send a note to `smo-key` (contact info on profile). Thank you!

![A test from 8 feet away](https://raw.githubusercontent.com/lasarobotics/ftcvision/dev/img/test3.png)

## Goals
- Locate the lit target (the thing with two buttons) within the camera viewfield
- Move the robot to the lit target, while identifying the color status of the target
- Locate the button of the target color and activate it

## Progress
- Beacon located successfully in multiple environments. Now, we are tuning detection so that it is (virtually) fail- and competition-proof.
- A competition-proof `OpMode` scheme created so that the robot controller does not need to be modified to use the app.
