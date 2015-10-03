package com.lasarobotics.tests.camera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import org.lasarobotics.vision.Camera;
import org.lasarobotics.vision.Cameras;
import org.lasarobotics.vision.Util;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

import java.io.File;

public class CameraTestActivity extends Activity implements CvCameraViewListener2 {

    private Mat mRgba; //RGBA image matrix
    private Mat mGray; //Grayscale image matrix
    private Mat mTarget; //Target image grayscale

    private float focalLength; //Camera lens focal length

    private CameraBridgeViewBase mOpenCvCameraView;

    //private FeatureDetection.ObjectAnalysis analysis;

    private void initialize()
    {
        //CAMERA PROPERTIES TEST
        Camera cam = Cameras.getPrimaryCamera();
        assert cam != null;
        android.hardware.Camera.Parameters pam = cam.getCamera().getParameters();
        focalLength = pam.getFocalLength();
        cam.getCamera().release();

        //GET TARGET IMAGE
        //Read the target image file
        String dir = Util.getDCIMDirectory();
        File file = new File(dir + "/Object-FTCLogo.png");

        if (!file.exists())
        {
            // print error and abort execution
            Log.e("CameraTester", "FAILED TO FIND IMAGE FILE!");
            System.exit(1);
        }
        mTarget = Highgui.imread(file.getAbsolutePath(), Highgui.IMREAD_GRAYSCALE);
        if (mTarget.empty())
        {
            // print error and abort execution
            Log.e("CameraTester", "FAILED TO LOAD IMAGE FILE!");
            System.exit(1);
        }

        detector = FeatureDetector.create(FeatureDetector.FAST);
        extractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        MatOfKeyPoint keypoints = new MatOfKeyPoint();

        detector.detect(mTarget, keypoints);

        descriptors_object = new Mat();
        extractor.compute(mTarget, keypoints, descriptors_object);
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

        //ANALYZE OBJECT
        /*FeatureDetection detection = new FeatureDetection(FeatureDetection.FeatureDetectorType.GFTT,
                                                          FeatureDetection.DescriptorExtractorType.BRIEF,
                                                          FeatureDetection.DescriptorMatcherType.BRUTEFORCE);
        analysis = detection.analyzeObject(mTarget);*/
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

    FeatureDetector detector;
    DescriptorExtractor extractor;
    DescriptorMatcher matcher;

    private Mat descriptors_object;

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // input frame has RBGA format
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        MatOfKeyPoint keypoints = new MatOfKeyPoint();

        detector.detect(mGray, keypoints);

        Mat descriptors = new Mat();
        extractor.compute(mGray, keypoints, descriptors);

        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptors, descriptors_object, matches);

        Log.d("Camera Test", "Keypoint count: " + keypoints.rows());
        Log.d("Camera Test", "Descriptor count: " + descriptors.rows());
        Log.d("Camera Test", "Match count: " + matches.rows());

        /*FeatureDetection detection = new FeatureDetection(FeatureDetection.FeatureDetectorType.FAST,
                FeatureDetection.DescriptorExtractorType.BRIEF,
                FeatureDetection.DescriptorMatcherType.BRUTEFORCE_HAMMING);
        detection.locateObject(mGray, mTarget, analysis, mRgba);*/

        //Features.highlightFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());

        return mRgba;
    }
}
