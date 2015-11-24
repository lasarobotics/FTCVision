![FTC Vision](https://raw.githubusercontent.com/lasarobotics/ftcvision/dev/img/logo-github.png)

# FTC Vision Library [![Build Status](https://travis-ci.org/lasarobotics/FTCVision.svg?branch=staging)](https://travis-ci.org/lasarobotics/FTCVision)
Computer Vision library for FTC based on OpenCV, featuring **beacon color and position detection**, as well as an easy-to-use `VisionOpMode` format and many additional detection features planned in the future.

## Installing from Scratch

1. Clone FTCVision into a clean directory (outside your robot controller app) using the following command: `git clone --depth=1 https://github.com/lasarobotics/ftcvision`.
2. Open the FTCVision project using Android Studio
3. Copy your OpModes from your robot controller directory into the appropriate directory within `ftc-robotcontroller`. Then, modify the `FtcOpModeRegister` appropriately to add your custom OpModes.
4. You can now write your own custom `VisionOpMode`!

## Installing into Existing Project

1. Clone FTCVision into a clean directory (outside your robot controller app) using the following command: `git clone --depth=1 https://github.com/lasarobotics/ftcvision`.
2. Navigate to the FTCVision directory that you just cloned and copy the `ftc-visionlib` and `opencv-java` folders into your existing robot controller app.
3. Open your robot controller app in Android Studio.
4. Find your `settings.gradle` file and append the following two lines:
```
include ':opencv-java'
include ':ftc-visionlib'
```
5. Find the `AndroidManifest.xml` under your `ftc-robotcontroller` folder, sometimes named `sample` or similar.
6. Insert the following `uses-permission` tag in the appropriate location (look at the rest of the file for context).
```
<uses-permission android:name="android.permission.CAMERA" android:required="true" />
```
7. Find your `build.gradle` in the parent folder of `AndroidManifest.xml` and insert the following line under `dependencies`:
```
compile project(':ftc-visionlib')
compile project(':opencv-java')
```
8. Copy in Vision opmodes (optional but recommended) from the FTCVision directory into your opmode directory.

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
