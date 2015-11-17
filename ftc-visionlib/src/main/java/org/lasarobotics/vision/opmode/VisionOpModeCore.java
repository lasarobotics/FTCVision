package org.lasarobotics.vision.opmode;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.util.FPS;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;

/**
 * Core OpMode class containing most OpenCV functionality
 */
abstract class VisionOpModeCore extends OpMode implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static JavaCameraView openCVCamera;
    protected int width, height;
    private static boolean initialized = false;
    protected FPS fps;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(hardwareMap.appContext) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    //Woohoo!
                    Log.d("OpenCV", "OpenCV Manager connected!");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    protected final void setCamera(Cameras camera)
    {
        openCVCamera.setCameraIndex(camera.getID());
    }

    protected final void setFrameSize(Size frameSize)
    {
        openCVCamera.setMaxFrameSize((int) frameSize.width, (int) frameSize.height);
        openCVCamera.setMinimumWidth((int) frameSize.width);
        openCVCamera.setMinimumHeight((int) frameSize.height);
    }

    @Override
    public void init() {
        //Initialize camera view
        final Activity activity = (Activity)hardwareMap.appContext;
        final VisionOpModeCore t = this;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!OpenCVLoader.initDebug()) {
                    Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, hardwareMap.appContext, mLoaderCallback);
                } else {
                    Log.d("OpenCV", "OpenCV library found inside package. Using it!");
                    mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                }

                LinearLayout layout = new LinearLayout(activity);
                layout.setOrientation(LinearLayout.VERTICAL);

                layout.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                openCVCamera = new JavaCameraView(hardwareMap.appContext, 0);

                layout.addView(openCVCamera);
                layout.setVisibility(View.VISIBLE);

                openCVCamera.setCvCameraViewListener(t);
                if (openCVCamera != null)
                    openCVCamera.disableView();
                openCVCamera.enableView();
                openCVCamera.setMinimumWidth(900);
                openCVCamera.setMinimumHeight(900);
                openCVCamera.connectCamera(900, 900);

                //Initialize FPS counter
                fps = new FPS();

                //Done!
                initialized = true;
            }
        });
    }

    @Override
    public void stop() {
        super.stop();

        if (openCVCamera != null)
            openCVCamera.disableView();

        stop(true);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (!initialized) {
            return inputFrame.rgba();
        }

        fps.update();
        return frame(inputFrame.rgba(), inputFrame.gray());
    }

    public abstract void init(int width, int height);
    @Override
    public abstract void loop();
    public abstract void stop(boolean success);
    public abstract Mat frame(Mat rgba, Mat gray);
}
