package com.lasarobotics.tests.camera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import org.lasarobotics.vision.android.Camera;
import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.android.Util;
import org.lasarobotics.vision.detection.ColorBlobDetector;
import org.lasarobotics.vision.detection.ObjectDetection;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.util.FPS;
import org.lasarobotics.vision.util.color.ColorHSV;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.lasarobotics.vision.util.color.ColorSpace;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;

import java.io.File;

public class CameraTestActivity extends Activity implements CvCameraViewListener2 {

    private Mat mRgba; //RGBA scene image
    private Mat mGray; //Grayscale scene image

    private float focalLength; //Camera lens focal length

    private CameraBridgeViewBase mOpenCvCameraView;

    private ObjectDetection.ObjectAnalysis objectAnalysis;
    private FPS fpsCounter;

    private void initialize()
    {
        //GET CAMERA PROPERTIES
        Camera cam = Cameras.getPrimaryCamera();
        assert cam != null;
        android.hardware.Camera.Parameters pam = cam.getCamera().getParameters();
        focalLength = pam.getFocalLength();
        cam.getCamera().release();

        //GET OBJECT IMAGE
        //Read the target image file
        String dir = Util.getDCIMDirectory();
        File file = new File(dir + "/beacon.png");

        if (!file.exists())
        {
            // print error and abort execution
            Log.e("CameraTester", "FAILED TO FIND IMAGE FILE!");
            System.exit(1);
        }
        Mat mTarget = Highgui.imread(file.getAbsolutePath(), Highgui.IMREAD_GRAYSCALE);
        if (mTarget.empty())
        {
            // print error and abort execution
            Log.e("CameraTester", "FAILED TO LOAD IMAGE FILE!");
            System.exit(1);
        }

        //ANALYZE OBJECT
        //ObjectDetection detection = new ObjectDetection(ObjectDetection.FeatureDetectorType.GFTT,
        //        ObjectDetection.DescriptorExtractorType.ORB,
        //        ObjectDetection.DescriptorMatcherType.BRUTEFORCE_HAMMING);
        //objectAnalysis = detection.analyzeObject(mTarget);

        //UPDATE COUNTER
        fpsCounter = new FPS();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    // OpenCV loaded successfully!
                    // Load native library AFTER OpenCV initialization

                    initialize();

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_cameratest);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.surfaceView);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            // Internal OpenCV library not found. Using OpenCV Manager for initialization
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_12, this, mLoaderCallback);
        } else {
            // OpenCV library found inside package. Using it!
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private ColorBlobDetector detectorRed;
    private ColorBlobDetector detectorBlue;

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);

        detectorRed  = new ColorBlobDetector();
        detectorBlue = new ColorBlobDetector();
        detectorRed.setHsvColor((ColorHSV)new ColorRGBA(251, 122, 164).convertColor(ColorSpace.HSV));
        detectorBlue.setHsvColor((ColorHSV)new ColorRGBA(75, 142, 255).convertColor(ColorSpace.HSV));
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // input frame has RGBA format
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        fpsCounter.update();

        //ObjectDetection detection = new ObjectDetection(ObjectDetection.FeatureDetectorType.ORB,
        //        ObjectDetection.DescriptorExtractorType.ORB,
        //        ObjectDetection.DescriptorMatcherType.BRUTEFORCE_HAMMING);

        try {
            //ObjectDetection.SceneAnalysis sceneAnalysis = detection.analyzeScene(mGray, objectAnalysis, mRgba);
            //ObjectDetection.drawKeypoints(mRgba, sceneAnalysis);
            //ObjectDetection.drawDebugInfo(mRgba, sceneAnalysis);
            //ObjectDetection.drawObjectLocation(mRgba, objectAnalysis, sceneAnalysis);

            mRgba = inputFrame.rgba();

            detectorRed.process(mRgba);
            detectorBlue.process(mRgba);

            Drawing.drawContours(mRgba, detectorRed.getContours(), new ColorRGBA(255, 0, 0), 3);
            Drawing.drawContours(mRgba, detectorBlue.getContours(), new ColorRGBA(0, 0, 255), 3);
        }
        catch (Exception e)
        {
            Drawing.drawText(mRgba, "Analysis Error", new Point(0, 8), 1.0f, new ColorRGBA("#F44336"), Drawing.Anchor.BOTTOMLEFT);
            e.printStackTrace();
        }

        Drawing.drawText(mRgba, "FPS: " + fpsCounter.getFPSString(), new Point(0, 24), 1.0f, new ColorRGBA("#2196F3"));

        return mRgba;
    }
}
