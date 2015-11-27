<p align="center">
  <img src="https://raw.githubusercontent.com/lasarobotics/ftcvision/img/logo.png?raw=true" alt="FTC-Sees!"/>
</p>

# FTC Vision Library [![Build Status](https://travis-ci.org/lasarobotics/FTCVision.svg?branch=master)](https://travis-ci.org/lasarobotics/FTCVision)
Computer Vision library for FTC based on OpenCV, featuring **beacon color and position detection**, as well as an easy-to-use `VisionOpMode` format and many additional detection features planned in the future.

## Installing from Scratch

1. Clone FTCVision into a clean directory (outside your robot controller app) using the following command: `git clone --depth=1 https://github.com/lasarobotics/ftcvision`.
2. Open the FTCVision project using Android Studio
3. Copy your OpModes from your robot controller directory into the appropriate directory within `ftc-robotcontroller`. Then, modify the `FtcOpModeRegister` appropriately to add your custom OpModes.
4. You can now write your own custom `VisionOpMode`!

## Installing into Existing Project

- Clone FTCVision into a clean directory (outside your robot controller app) using the following command: `git clone --depth=1 https://github.com/lasarobotics/ftcvision`.
- Navigate to the FTCVision directory that you just cloned and copy the `ftc-visionlib` and `opencv-java` folders into your existing robot controller app.
- Open your robot controller app in Android Studio. Make sure you have the `Project` mode selected in the project browser window (so you can see all of the files in your project).
- Find your `settings.gradle` file and append the following two lines:
```
include ':opencv-java'
include ':ftc-visionlib'
```
- Find the `AndroidManifest.xml` under your `ftc-robotcontroller` folder, sometimes named `sample` or similar.
- Insert the following `uses-permission` tag in the appropriate location (look at the rest of the file for context).
```
<uses-permission android:name="android.permission.CAMERA" android:required="true" />
```
- Find your `build.gradle` in the parent folder of `AndroidManifest.xml` and insert the following line under `dependencies`:
```
compile project(':ftc-visionlib')
compile project(':opencv-java')
```
- Update Gradle configuration by pressing the green "Sync Project with Gradle Files" button in the header (this may take a minute)
- Copy in Vision opmodes (those that end in `VisionSample.java`, located in `[vision-root]/ftc-robotcontroller/src/main/java/com/qualcomm/ftcrobotcontroller/opmodes`) from the FTCVision directory into your opmode directory.
- Run and test the code! Let us know if you encounter any difficulties.
- You can now write your custom `VisionOpMode`!

## Status
This library is currently under insanely active development. We're in the **Beta** phase right now. If you have any questions or would like to help, send a note to `smo-key` (contact info on profile). Thank you!

#### Accuracy Test
![A test from 8 feet away](https://raw.githubusercontent.com/lasarobotics/ftcvision/img/test2.png)

#### Distance Test
![A test from 8 feet away](https://raw.githubusercontent.com/lasarobotics/ftcvision/img/test1.png)

## Goals
- Locate the lit target (the thing with two buttons) within the camera viewfield
- Move the robot to the lit target, while identifying the color status of the target
- Locate the button of the target color and activate it

## Progress
- Beacon located successfully in multiple environments. Now, we are tuning detection so that it is (virtually) fail- and competition-proof.
- A competition-proof `OpMode` scheme created so that the robot controller does not need to be modified to use the app.

## ZXing
FTCVision uses the ZXing library for scanning QRCodes, and it uses an Apache 2.0 license. View the `ZXing_license` and `ZXing_notice` files for the appropriate information.
