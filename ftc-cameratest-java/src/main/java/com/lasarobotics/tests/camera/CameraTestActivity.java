package com.lasarobotics.tests.camera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import org.lasarobotics.vision.Camera;
import org.lasarobotics.vision.Cameras;
import org.lasarobotics.vision.Drawing;
import org.lasarobotics.vision.FPS;
import org.lasarobotics.vision.Util;
import org.lasarobotics.vision.detection.ObjectDetection;
import org.lasarobotics.vision.util.Color;
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
        File file = new File(dir + "/Object-FTCLogo.png");

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
        ObjectDetection detection = new ObjectDetection(ObjectDetection.FeatureDetectorType.GFTT,
                ObjectDetection.DescriptorExtractorType.BRIEF,
                ObjectDetection.DescriptorMatcherType.BRUTEFORCE_HAMMING);
        objectAnalysis = detection.analyzeObject(mTarget);

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

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // input frame has RBGA format
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        fpsCounter.update();

        ObjectDetection detection = new ObjectDetection(ObjectDetection.FeatureDetectorType.FAST,
                ObjectDetection.DescriptorExtractorType.BRIEF,
                ObjectDetection.DescriptorMatcherType.BRUTEFORCE_HAMMING);

        try {
            ObjectDetection.SceneAnalysis sceneAnalysis = detection.analyzeScene(mGray, objectAnalysis, mRgba);
            ObjectDetection.drawKeypoints(mRgba, sceneAnalysis);
            ObjectDetection.drawDebugInfo(mRgba, sceneAnalysis);
        }
        catch (Exception e)
        {
            Drawing.drawText(mRgba, "Analysis Error", new Point(0, 8), 1.0f, new Color("#F44336"), Drawing.Anchor.BOTTOMLEFT);
            e.printStackTrace();
        }

        Drawing.drawText(mRgba, "FPS: " + fpsCounter.getFPSString(), new Point(0, 24), 1.0f, new Color("#2196F3"));

        //Features.highlightFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());

        return mRgba;
    }
}
