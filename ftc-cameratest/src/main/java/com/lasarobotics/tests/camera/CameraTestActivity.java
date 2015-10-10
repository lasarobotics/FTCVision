package com.lasarobotics.tests.camera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

public class CameraTestActivity extends Activity implements CvCameraViewListener2, View.OnTouchListener {

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
                    mOpenCvCameraView.setOnTouchListener(CameraTestActivity.this);
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

    private boolean              mIsColorSelected = false;
    private ColorRGBA            mBlobColorRgba;
    private ColorHSV             mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private ColorRGBA            CONTOUR_COLOR;

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);

        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new ColorRGBA(255, 255, 255, 255);
        mBlobColorHsv = new ColorHSV(255, 255, 255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new ColorRGBA(255,0,0,255);
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

        //ObjectDetection detection = new ObjectDetection(ObjectDetection.FeatureDetectorType.ORB,
        //        ObjectDetection.DescriptorExtractorType.ORB,
        //        ObjectDetection.DescriptorMatcherType.BRUTEFORCE_HAMMING);

        try {
            //ObjectDetection.SceneAnalysis sceneAnalysis = detection.analyzeScene(mGray, objectAnalysis, mRgba);
            //ObjectDetection.drawKeypoints(mRgba, sceneAnalysis);
            //ObjectDetection.drawDebugInfo(mRgba, sceneAnalysis);
            //ObjectDetection.drawObjectLocation(mRgba, objectAnalysis, sceneAnalysis);

            mRgba = inputFrame.rgba();

            if (mIsColorSelected) {
                mDetector.process(mRgba);
                List<MatOfPoint> contours = mDetector.getContours();
                Log.e("CameraTester", "Contours count: " + contours.size());
                Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR.getScalarRGBA());

                Mat colorLabel = mRgba.submat(4, 68, 4, 68);
                colorLabel.setTo(mBlobColorRgba.getScalarRGBA());

                Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
                mSpectrum.copyTo(spectrumLabel);
            }
        }
        catch (Exception e)
        {
            Drawing.drawText(mRgba, "Analysis Error", new Point(0, 8), 1.0f, new ColorRGBA("#F44336"), Drawing.Anchor.BOTTOMLEFT);
            e.printStackTrace();
        }

        Drawing.drawText(mRgba, "FPS: " + fpsCounter.getFPSString(), new Point(0, 24), 1.0f, new ColorRGBA("#2196F3"));

        //Features.highlightFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());

        return mRgba;
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i("CameraTester", "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);
        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = new ColorHSV(Core.sumElems(touchedRegionHsv));
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.getScalar().val.length; i++)
            mBlobColorHsv.getScalar().val[i] /= pointCount;

        mBlobColorRgba = (ColorRGBA)mBlobColorHsv.convertColor(ColorSpace.RGBA);

        Log.i("CameraTester", "Touched rgba color: (" + mBlobColorRgba.red() + ", " + mBlobColorRgba.green() +
                ", " + mBlobColorRgba.blue() + ", " + mBlobColorRgba.alpha()+ ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }
}
