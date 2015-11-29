package com.lasarobotics.tests.camera;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.lasarobotics.vision.test.android.Camera;
import org.lasarobotics.vision.test.android.Cameras;
import org.lasarobotics.vision.test.detection.ColorBlobDetector;
import org.lasarobotics.vision.test.detection.QRDetector;
import org.lasarobotics.vision.test.detection.objects.Contour;
import org.lasarobotics.vision.test.ftc.resq.Beacon;
import org.lasarobotics.vision.test.image.Drawing;
import org.lasarobotics.vision.test.opmode.extensions.QRExtension;
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
    private QRExtension qre;
    public CameraTestActivity() {

    }

    private void initialize() {
        //GET CAMERA PROPERTIES
        Camera cam = Cameras.PRIMARY.createCamera();
        android.hardware.Camera ncam = cam.getCamera();
        if(ncam == null) { //Try again if failed
            for(int i = 0; i < 10 && ncam == null; i++) { //Try a max of 10 times
                cam = Cameras.PRIMARY.createCamera();
                ncam = cam.getCamera();
                Toast.makeText(CameraTestActivity.this, "Unable to get camera object. Attempting " + (10-i) + " more time(s).", Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    //Nobody cares
                }
            }
            if(ncam == null) {
                Toast.makeText(CameraTestActivity.this, "Unable to get camera object", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        android.hardware.Camera.Parameters pam = ncam.getParameters();
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

        mOpenCvCameraView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CameraTestActivity.this.toggleQRDetection();
                return true;
            }
        });
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
        qre = new QRExtension();
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

        ColorRGBA white = new ColorRGBA("#ffffff");
        if(detectQR) {
            if(!qre.hasInit()) {
                qre.init(null);
                qre.setShouldColorCorrect(true);
                qre.setShouldRotate(false);
                qre.setDebugInfo(true);
            }
            qre.frame(null, mRgba, mGray);
            QRExtension.FTCQRCodeInfo ftcqrci = qre.getCodeInfo();
            Drawing.drawText(mRgba, "Valid: " + ftcqrci.isValid() + " Team: " + ftcqrci.getTeam() + " Num: " + ftcqrci.getNum(),
                    new Point(0, 80), 1.0f, white);
            Drawing.drawText(mRgba, "Orientation: " + qre.getOrientation().toString(),
                    new Point(0, 50), 1.0f, white);
            Drawing.drawText(mRgba, "Text: " + qre.getText(),
                    new Point(0, 8), 1.0f, white, Drawing.Anchor.BOTTOMLEFT);
            if(qre.hasErrorReason()) {
                Drawing.drawText(mRgba, "Error: " + qre.getErrorReason(), new Point(0, 38), 1.0f, new ColorRGBA("#F44336"), Drawing.Anchor.BOTTOMLEFT);
            }
            Drawing.drawRectangle(mRgba, new Point(mRgba.width() - 80, 10), new Point(mRgba.width() - 10, 80), white);
        } else {
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
                        new Point(0, 50), 1.0f, white);

                //Transform.enlarge(mRgba, originalSize, true);
                //Transform.enlarge(mGray, originalSize, true);

                Drawing.drawText(mRgba, colorAnalysis.getStateLeft().toString() + ", " + colorAnalysis.getStateRight().toString(),
                        new Point(0, 8), 1.0f, white, Drawing.Anchor.BOTTOMLEFT);
            } catch (Exception e) {
                Drawing.drawText(mRgba, "Analysis Error", new Point(0, 8), 1.0f, new ColorRGBA("#F44336"), Drawing.Anchor.BOTTOMLEFT);
                e.printStackTrace();
            }
        }

        Drawing.drawText(mRgba, "FPS: " + fpsCounter.getFPSString(), new Point(0, 24), 1.0f, white); //"#2196F3"
        Drawing.drawText(mRgba, "QR", new Point(mRgba.width() - 67, 55), 1.0f, white);

        return mRgba;
    }

    private boolean detectQR = false;
    public void toggleQRDetection() {
        detectQR = (detectQR ? false : true);
        if(detectQR) {
            qre = new QRExtension();
        }
    }
}
