package com.lasarobotics.tests.camera;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import org.lasarobotics.vision.test.android.Camera;
import org.lasarobotics.vision.test.android.Cameras;
import org.lasarobotics.vision.test.detection.ColorBlobDetector;
import org.lasarobotics.vision.test.detection.objects.Contour;
import org.lasarobotics.vision.test.ftc.resq.Beacon;
import org.lasarobotics.vision.test.image.Drawing;
import org.lasarobotics.vision.test.util.FPS;
import org.lasarobotics.vision.test.util.color.ColorGRAY;
import org.lasarobotics.vision.test.util.color.ColorHSV;
import org.lasarobotics.vision.test.util.color.ColorRGBA;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.List;

public class CameraTestActivity extends Activity implements CvCameraViewListener2 {

    private static final ColorHSV colorRadius = new ColorHSV(50, 75, 127);
    private static final ColorHSV lowerBoundRed = new ColorHSV((int) (305 / 360.0 * 255.0), (int) (0.200 * 255.0), (int) (0.300 * 255.0));
    private static final ColorHSV upperBoundRed = new ColorHSV((int) ((360.0 + 5.0) / 360.0 * 255.0), 255, 255);
    private static final ColorHSV lowerBoundBlue = new ColorHSV((int) (170.0 / 360.0 * 255.0), (int) (0.200 * 255.0), (int) (0.750 * 255.0));
    private static final ColorHSV upperBoundBlue = new ColorHSV((int) (227.0 / 360.0 * 255.0), 255, 255);
    private Mat mRgba; //RGBA scene image
    private Mat mGray; //Grayscale scene image
    private CameraBridgeViewBase mOpenCvCameraView;
    private float focalLength; //Camera lens focal length
    //private ObjectDetection.ObjectAnalysis objectAnalysis;
    private FPS fpsCounter;
    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    // OpenCV loaded successfully!
                    // Load native library AFTER OpenCV initialization

                    initialize();

                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private ColorBlobDetector detectorRed;
    private ColorBlobDetector detectorBlue;
    public CameraTestActivity() {

    }

    private void initialize() {
        //GET CAMERA PROPERTIES
        Camera cam = Cameras.PRIMARY.createCamera();
        android.hardware.Camera.Parameters pam = cam.getCamera().getParameters();
        focalLength = pam.getFocalLength();
        cam.release();

        //GET OBJECT IMAGE
        //Read the target image file
        /*String dir = Util.getDCIMDirectory();
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
        }*/

        //ANALYZE OBJECT
        //ObjectDetection detection = new ObjectDetection(ObjectDetection.FeatureDetectorType.GFTT,
        //        ObjectDetection.DescriptorExtractorType.ORB,
        //        ObjectDetection.DescriptorMatcherType.BRUTEFORCE_HAMMING);
        //objectAnalysis = detection.analyzeObject(mTarget);

        //UPDATE COUNTER
        fpsCounter = new FPS();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_cameratest);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.surfaceView);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            // Internal OpenCV library not found. Using OpenCV Manager for initialization
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
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

        //Initialize all detectors here
        detectorRed = new ColorBlobDetector(lowerBoundRed, upperBoundRed);
        detectorBlue = new ColorBlobDetector(lowerBoundBlue, upperBoundBlue);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // input frame has RGBA format
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        //Size originalSize = mRgba.size();

        //Transform.flip(mRgba, Transform.FlipType.FLIP_BOTH);
        //Transform.flip(mGray, Transform.FlipType.FLIP_BOTH);

        //Transform.shrink(mRgba, new Size(480, 480), true);
        //Transform.shrink(mGray, new Size(480, 480), true);

        fpsCounter.update();

        try {
            //Process the frame for the color blobs
            detectorRed.process(mRgba);
            detectorBlue.process(mRgba);

            //Get the list of contours
            List<Contour> contoursRed = detectorRed.getContours();
            List<Contour> contoursBlue = detectorBlue.getContours();

            //Get color analysis
            Beacon beacon = new Beacon();
            Beacon.BeaconAnalysis colorAnalysis = beacon.analyzeColor(contoursRed, contoursBlue, mRgba, mGray);


            //DEBUG confidence output
            Drawing.drawText(mRgba, "Confidence: " + colorAnalysis.getConfidenceString(),
                    new Point(0, 50), 1.0f, new ColorGRAY(255));

            //Transform.enlarge(mRgba, originalSize, true);
            //Transform.enlarge(mGray, originalSize, true);

            Drawing.drawText(mRgba, colorAnalysis.getStateLeft().toString() + ", " + colorAnalysis.getStateRight().toString(),
                    new Point(0, 8), 1.0f, new ColorGRAY(255), Drawing.Anchor.BOTTOMLEFT);
        } catch (Exception e) {
            Drawing.drawText(mRgba, "Analysis Error", new Point(0, 8), 1.0f, new ColorRGBA("#F44336"), Drawing.Anchor.BOTTOMLEFT);
            e.printStackTrace();
        }

        Drawing.drawText(mRgba, "FPS: " + fpsCounter.getFPSString(), new Point(0, 24), 1.0f, new ColorRGBA("#ffffff")); //"#2196F3"

        return mRgba;
    }
}
