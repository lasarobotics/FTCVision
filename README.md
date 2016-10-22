<p align="center">
  <img src="https://raw.githubusercontent.com/lasarobotics/ftcvision/img/logo.png?raw=true" alt="FTC-Sees!"/>
</p>

# FTC Vision Library [![Build Status](https://travis-ci.org/lasarobotics/FTCVision.svg?branch=master)](https://travis-ci.org/lasarobotics/FTCVision) [![Documentation Status](https://img.shields.io/badge/documentation-1.0.0%20(up%20to%20date)-blue.svg)](http://ftcvision.lasarobotics.org)
Computer Vision library for FTC based on OpenCV, featuring **beacon color and position detection**, as well as an easy-to-use `VisionOpMode` format and many additional detection features planned in the future.

## Installing into FIRST Official App *(recommended)*

- Clone FTCVision into a clean directory (outside your robot controller app) using the following command: `git clone --depth=1 https://github.com/lasarobotics/ftcvision`.
- Navigate to the FTCVision directory that you just cloned and copy the `ftc-visionlib` and `opencv-java` folders into your existing robot controller app.
- Open your robot controller app in Android Studio. Make sure you have the `Project` mode selected in the project browser window (so you can see all of the files in your project).
- Find your `settings.gradle` file and append the following two lines:
```
include ':opencv-java'
include ':ftc-visionlib'
```
- Find the `AndroidManifest.xml` file in your app's `FTCRobotController/src/main` folder.
- Insert the following `uses-permission` tag in `AndroidManifest.xml` just below the `<mainfest ...>` tag and just before the `application` tag:
```
<uses-permission android:name="android.permission.CAMERA" android:required="true" />
```
- Find the `FTCRobotController/build.release.gradle` AND `TeamCode/build.release.gradle` files and insert the following lines under `dependencies` **into both files**:
```
compile project(':ftc-visionlib')
compile project(':opencv-java')
```
- Update Gradle configuration by pressing the green "Sync Project with Gradle Files" button in the header (this may take a minute)
- Copy in Vision opmodes (those that end in `VisionSample.java`, located in `[vision-root]/ftc-robotcontroller/src/main/java/com/qualcomm/ftcrobotcontroller/opmodes`) from the FTCVision directory into a folder within the `TeamCode` directory (where your place any other opmode).
- Before running the app for the first time, install the "OpenCV Manager" from the Google Play Store to enable Vision processing.
- Run and test the code! Let us know if you encounter any difficulties. *Note: depending on your version of the FIRST app, you may need to modify the OpModes slightly (such as adding `@Autonomous`) in order to get them to work for your version of the SDK.*
- You can now write your custom `VisionOpMode`!
- *(Optional)* Add Vision testing app (see pictures of it below!) by copying all files from `ftc-cameratest` into the root of your project. Then, add `include ':ftc-cameratest'` to your `settings.gradle` in the root of your project. To run the camera test app, click the green "Sync Project with Gradle Files" button to update your project, then select `ftc-cameratest` from the dropdown next to the run button.

## Installing via Git Submodule *(advanced)*
When installing via Git submodule, **every person cloning your repo will need to run `git submodule init` and `git subomodule update` for every new clone.** However, you also get the advantage of not copying all the files yourself and you can update the project to the latest version easily by navigating inside the `ftc-vision` folder then running `git pull`.

- Inside the root of your project directory, run
```
git submodule init
git submodule add https://github.com/lasarobotics/ftcvision ftc-vision
```
- Follow the guide "Installing into Existing Project" starting from the third bullet point. Please note that since everything will be in the `ftc-vision` folder and thus directories will need to be modified. Once you get to the step that modifies `settings.gradle`, add the following lines:
```
project(':opencv-java').projectDir = new File('ftc-vision/opencv-java')
project(':ftc-visionlib').projectDir = new File('ftc-vision/ftc-visionlib')
project(':ftc-cameratest').projectDir = new File('ftc-vision/ftc-cameratest') <- only if you want to enable the camera testing app
```
- You can now write your custom `VisionOpMode`!

## Installing from Scratch *(for testing only - deprecated)*

1. Clone FTCVision into a clean directory (outside your robot controller app) using the following command: `git clone --depth=1 https://github.com/lasarobotics/ftcvision`.
2. Open the FTCVision project using Android Studio
3. Copy your OpModes from your robot controller directory into the appropriate directory within `ftc-robotcontroller`. Then, modify the `FtcOpModeRegister` appropriately to add your custom OpModes.
4. Before running the app for the first time, install the "OpenCV Manager" from the Google Play Store to enable Vision processing.
5. Run and test the code! Let us know if you encounter any difficulties.
6. You can now write your own `VisionOpMode`!

## Status
This library is complete as of World Championship 2016. If you have any questions or would like to help, send a note to `smo-key` (contact info on profile) or open an issue. Thank you!

## Documentation [![Documentation Status](https://img.shields.io/badge/documentation-1.0.0%20(up%20to%20date)-blue.svg)](http://ftcvision.lasarobotics.org)

Documentation for the stable library is available at http://ftcvision.lasarobotics.org.

## Does it work?

**Yes!** FTCVision can detect a beacon 0.5-4 feet away with 90% accuracy in 0.2 seconds. Here are some pictures. :smiley:

#### Accuracy Test
![Can it detect the beacon?](https://raw.githubusercontent.com/lasarobotics/ftcvision/img/test4.png)

#### Old Accuracy Test
![Can it detect the beacon?](https://raw.githubusercontent.com/lasarobotics/ftcvision/img/test2.png)

#### Distance Test
![A test from 8 feet away](https://raw.githubusercontent.com/lasarobotics/ftcvision/img/test1.png)

#### Basic Analysis Demo
![FAST isn't the greatest](https://raw.githubusercontent.com/lasarobotics/ftcvision/img/analysisdemo.gif)

#### Ambient Analysis (Color and Rotation Detection) Test
![A test from 8 feet away](https://raw.githubusercontent.com/lasarobotics/ftcvision/img/test3.gif)

#### Analysis Methods
![FAST vs. COMPLEX](https://raw.githubusercontent.com/lasarobotics/ftcvision/img/methods.png)

- The **FAST** method analyzes frames at around 5 FPS. It looks for the primary colors in the image and correlates these to locate a beacon.
- The **COMPLEX** method analyzes frames at around 2-4 FPS. It uses statistical analysis to determine the beacon's location.
- Additionally, a **REALTIME** method exists that retrieves frames and analyzes them as fast as possible (up to 15 FPS).

## Goals
- To make it easy for teams to use the power of OpenCV on the Android platform
- Locate the lit target (the thing with two buttons) within the camera viewfield
- Move the robot to the lit target, while identifying the color status of the target
- Locate the button of the target color and activate it

## Progress
- Beacon located successfully with automated environmental and orientation tuning.
- A competition-proof `OpMode` scheme created so that the robot controller does not need to be modified to use the app.
- Now supports nearly every phone since Android 4.2, including both the ZTE Speed and Moto G.
