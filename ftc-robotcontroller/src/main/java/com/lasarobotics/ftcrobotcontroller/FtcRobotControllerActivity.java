/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.lasarobotics.ftcrobotcontroller;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.FtcEventLoop;
import com.qualcomm.ftccommon.FtcRobotControllerService;
import com.qualcomm.ftccommon.FtcRobotControllerService.FtcRobotControllerBinder;
import com.qualcomm.ftccommon.LaunchActivityConstantsList;
import com.qualcomm.ftccommon.Restarter;
import com.qualcomm.ftccommon.UpdateUI;
import com.lasarobotics.ftcrobotcontroller.opmodes.FtcOpModeRegister;
import com.qualcomm.hardware.HardwareFactory;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.util.Dimmer;
import com.qualcomm.robotcore.util.ImmersiveMode;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;

import org.lasarobotics.vision.android.Camera;
import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.detection.ColorBlobDetector;
import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.image.Transform;
import org.lasarobotics.vision.util.FPS;
import org.lasarobotics.vision.util.IO;
import org.lasarobotics.vision.util.color.ColorGRAY;
import org.lasarobotics.vision.util.color.ColorHSV;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.List;

public class FtcRobotControllerActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    protected class RobotRestarter implements Restarter {
        public void requestRestart() {
            requestRobotRestart();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
            // a new USB device has been attached
            DbgLog.msg("USB Device attached; app restart may be needed");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ftc_controller);

        utility = new Utility(this);
        context = this;
        entireScreenLayout = (LinearLayout) findViewById(R.id.entire_screen);
        buttonMenu = (ImageButton) findViewById(R.id.menu_buttons);
        buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOptionsMenu();
            }
        });
        buttonRobotRestart = (ImageButton) findViewById(R.id.buttonRobotRestart);
        buttonRobotRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Restarting Robot", Toast.LENGTH_SHORT).show();
                requestRobotRestart();
            }
        });

        ImageButton buttonWifiSettings = (ImageButton) findViewById(R.id.buttonWifiSettings);
        buttonWifiSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                startActivity(intent);
            }
        });

        ImageButton buttonConfig = (ImageButton) findViewById(R.id.buttonConfig);
        buttonConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent("com.qualcomm.ftccommon.FtcRobotControllerSettingsActivity.intent.action.Launch");
                startActivityForResult(settingsIntent, LaunchActivityConstantsList.FTC_ROBOT_CONTROLLER_ACTIVITY_CONFIGURE_ROBOT);
            }
        });

        ImageButton buttonViewLog = (ImageButton) findViewById(R.id.buttonViewLog);
        final Context ctx = this;
        buttonViewLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewLogsIntent = new Intent("com.qualcomm.ftccommon.ViewLogsActivity.intent.action.Launch");
                viewLogsIntent.putExtra(LaunchActivityConstantsList.VIEW_LOGS_ACTIVITY_FILENAME, RobotLog.getLogFilename(ctx));
                startActivity(viewLogsIntent);
            }
        });

        textDeviceName = (TextView) findViewById(R.id.textDeviceName);
        textWifiDirectStatus = (TextView) findViewById(R.id.textWifi);
        textRobotStat = (TextView) findViewById(R.id.textRobot);
        textOpMode = (TextView) findViewById(R.id.textOp);
        textErrorMessage = (TextView) findViewById(R.id.textRobotError);
        textGamepad[0] = (TextView) findViewById(R.id.textGamepad1);
        textGamepad[1] = (TextView) findViewById(R.id.textGamepad2);
        imageWifi = (ImageView) findViewById(R.id.imageWifi);
        imageRobot = (ImageView) findViewById(R.id.imageRobot);
        immersion = new ImmersiveMode(getWindow().getDecorView());
        imageConfig = (ImageView) findViewById(R.id.imageConfig);
        imageOp = (ImageView) findViewById(R.id.imageOp);
        gamepad1 = (ImageView) findViewById(R.id.gamepad1);
        gamepad2 = (ImageView) findViewById(R.id.gamepad2);
        textActiveFilename = (TextView) findViewById(R.id.active_filename);
        textConfig = (TextView) findViewById(R.id.textConfig);
        dimmer = new Dimmer(this);
        dimmer.longBright();
        Restarter restarter = new RobotRestarter();

        updateUI = new UpdateUI(this, dimmer);
        updateUI.setRestarter(restarter);
        updateUI.setTextViews(textWifiDirectStatus, textRobotStat,
                textGamepad, textOpMode, textErrorMessage, textDeviceName);

        callback = updateUI.new Callback() {
            @Override
            public void wifiDirectUpdate(WifiDirectAssistant.Event event) {
                super.wifiDirectUpdate(event);
                String s = "WiFi Direct\n";
                switch(event.ordinal())
                {
                    case 0:
                    case 1:
                    case 2:
                        s += "Enabled"; break;
                    case 3:
                        s += "Connecting..."; break;
                    case 4:
                    case 5:
                        s += "Connected"; break;
                    case 7:
                        s += textDeviceName.getText(); break;
                    case 6:
                        s += "Disconnected"; break;
                    case 8:
                        s += "Error"; break;
                    default:
                        s += "Enabled"; break;
                }
                switch(event.ordinal())
                {
                    case 4:
                    case 5:
                    case 7:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageWifi.setImageResource(R.mipmap.icon_wifion);
                                imageWifi.setColorFilter(getResources().getColor(R.color.green));
                            }
                        });
                        break;
                    case 8:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageWifi.setImageResource(R.mipmap.icon_wifioff);
                                imageWifi.setColorFilter(getResources().getColor(R.color.error_orange));
                            }
                        });
                        break;
                    default:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageWifi.setImageResource(R.mipmap.icon_wifion);
                                imageWifi.setColorFilter(getResources().getColor(R.color.button));
                            }
                        });
                        break;
                }
                final String k = s;
                runOnUiThread(new Runnable() {
                    public void run() {
                        textWifiDirectStatus.setText(k);
                    }
                });
            }

            @Override
            public void robotUpdate(String status) {
                status = status.replaceAll("Robot Status: ", "");
                status = String.valueOf(status.charAt(0)).toUpperCase().concat(status.substring(1));
                final String s = status;
                final String n = "Robot Status\n" + status;
                runOnUiThread(new Runnable() {
                    public void run() {
                        textRobotStat.setText(n);
                    }
                });

                runOnUiThread(new Runnable() {
                    public void run() {
                        textErrorMessage.setText(RobotLog.getGlobalErrorMsg());

                        if (RobotLog.getGlobalErrorMsg().trim().length() > 0)
                        {
                            textErrorMessage.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            textErrorMessage.setVisibility(View.GONE);
                        }

                        if (s.equalsIgnoreCase("Scanning for USB devices") || s.equalsIgnoreCase("Init") || s.equalsIgnoreCase("Starting robot") || s.equalsIgnoreCase("Waiting on network"))
                        {
                            imageRobot.setColorFilter(getResources().getColor(R.color.button));
                            imageRobot.setImageResource(R.mipmap.icon_updating);
                        }
                        else if (RobotLog.getGlobalErrorMsg().trim().length() > 0 || s.equalsIgnoreCase("Dropped connection") || s.equalsIgnoreCase("Null") || s.equalsIgnoreCase("Abort due to interrupt"))
                        {
                            imageRobot.setColorFilter(getResources().getColor(R.color.error_orange));
                            imageRobot.setImageResource(R.mipmap.icon_warning);
                        }
                        else
                        {
                            imageRobot.setColorFilter(getResources().getColor(R.color.button));
                            imageRobot.setImageResource(R.mipmap.icon_done);
                        }
                    }
                });
            }

            @Override
            public void updateUi(String opModeName, Gamepad[] gamepads) {
                //super.updateUi(opModeName, gamepads);
                final String o = opModeName;
                final String n = "Op Mode\n" + opModeName;
                runOnUiThread(new Runnable() {
                    public void run() {
                        textOpMode.setText(n);

                        if (o.equalsIgnoreCase("Not selected")) {
                            imageOp.setColorFilter(getResources().getColor(R.color.error_orange));
                        }
                        else if (o.equalsIgnoreCase("Stop Robot"))
                        {
                            imageOp.setColorFilter(getResources().getColor(R.color.button));
                        }
                        else
                        {
                            imageOp.setColorFilter(getResources().getColor(R.color.green));
                        }
                    }
                });

                final Gamepad g1 = gamepads[0];
                final Gamepad g2 = gamepads[1];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (g1.toString().trim().length() == 0) {
                            gamepad1.setColorFilter(getResources().getColor(R.color.error_orange));
                        } else if (!g1.atRest()) {
                            gamepad1.setColorFilter(getResources().getColor(R.color.green));
                        }
                        else
                        {
                            gamepad1.setColorFilter(getResources().getColor(R.color.button));
                        }

                        if (g2.toString().trim().length() == 0) {
                            gamepad2.setColorFilter(getResources().getColor(R.color.error_orange));
                        } else if (!g2.atRest()) {
                            gamepad2.setColorFilter(getResources().getColor(R.color.green));
                        }
                        else
                        {
                            gamepad2.setColorFilter(getResources().getColor(R.color.button));
                        }
                    }
                });
            }
        };

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        hittingMenuButtonBrightensScreen();

        if (USE_DEVICE_EMULATION) {
            HardwareFactory.enableDeviceEmulation(); }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.framePreview);
        mOpenCvCameraView.setCameraIndex(0); //SET BACK (MAIN) CAMERA
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // save 4MB of logcat to the SD card
        RobotLog.writeLogcatToDisk(this, 4 * 1024);

        Intent intent = new Intent(this, FtcRobotControllerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        updateHeader();

        callback.wifiDirectUpdate(WifiDirectAssistant.Event.DISCONNECTED);

        entireScreenLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dimmer.handleDimTimer();
                return false;
            }
        });

    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_restart_robot:
                dimmer.handleDimTimer();
                Toast.makeText(context, "Restarting Robot", Toast.LENGTH_SHORT).show();
                requestRobotRestart();
                return true;
            case R.id.action_settings:
                // The string to launch this activity must match what's in AndroidManifest of FtcCommon for this activity.
                Intent settingsIntent = new Intent("com.qualcomm.ftccommon.FtcRobotControllerSettingsActivity.intent.action.Launch");
                startActivityForResult(settingsIntent, LaunchActivityConstantsList.FTC_ROBOT_CONTROLLER_ACTIVITY_CONFIGURE_ROBOT);
                return true;
            case R.id.action_about:
                // The string to launch this activity must match what's in AndroidManifest of FtcCommon for this activity.
                Intent intent = new Intent("com.qualcomm.ftccommon.configuration.AboutActivity.intent.action.Launch");
                startActivity(intent);
                return true;
            case R.id.action_exit_app:
                finish();
                return true;
            case R.id.action_view_logs:
                // The string to launch this activity must match what's in AndroidManifest of FtcCommon for this activity.
                Intent viewLogsIntent = new Intent("com.qualcomm.ftccommon.ViewLogsActivity.intent.action.Launch");
                viewLogsIntent.putExtra(LaunchActivityConstantsList.VIEW_LOGS_ACTIVITY_FILENAME, RobotLog.getLogFilename(this));
                startActivity(viewLogsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    @Override
    protected void onActivityResult(int request, int result, Intent intent) {
        if (request == REQUEST_CONFIG_WIFI_CHANNEL) {
            if (result == RESULT_OK) {
                Toast toast = Toast.makeText(context, "Configuration Complete", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                showToast(toast);
            }
        }
        if (request == LaunchActivityConstantsList.FTC_ROBOT_CONTROLLER_ACTIVITY_CONFIGURE_ROBOT) {
            if (result == RESULT_OK) {
                Serializable extra = intent.getSerializableExtra(FtcRobotControllerActivity.CONFIGURE_FILENAME);
                if (extra != null) {
                    utility.saveToPreferences(extra.toString(), R.string.pref_hardware_config_filename);
                    updateHeader();
                }
            }
        }
    }

    public void onServiceBind(FtcRobotControllerService service) {
        DbgLog.msg("Bound to Ftc Controller Service");
        controllerService = service;
        updateUI.setControllerService(controllerService);

        callback.wifiDirectUpdate(controllerService.getWifiDirectStatus());
        callback.robotUpdate(controllerService.getRobotStatus());
        requestRobotSetup();
    }

    protected void hittingMenuButtonBrightensScreen() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
                @Override
                public void onMenuVisibilityChanged(boolean isVisible) {
                    if (isVisible) {
                        dimmer.handleDimTimer();
                    }
                }
            });
        }
    }

    /*** QUESTIONABLE SECTION ***/

    protected TextView textRobotStatus;

    /*** PRIMARY SECTION ***/

    public static final String CONFIGURE_FILENAME = "CONFIGURE_FILENAME";
    private static final int REQUEST_CONFIG_WIFI_CHANNEL = 1;
    private static final boolean USE_DEVICE_EMULATION = false;
    private static final int NUM_GAMEPADS = 2;
    protected SharedPreferences preferences;

    protected UpdateUI.Callback callback;
    protected Context context;
    protected ImageButton buttonMenu;
    protected ImageButton buttonRobotRestart;
    protected TextView textDeviceName;
    protected TextView textWifiDirectStatus;
    protected TextView textRobotStat;
    protected TextView[] textGamepad = new TextView[NUM_GAMEPADS];
    protected ImageView gamepad1;
    protected ImageView gamepad2;
    protected TextView textOpMode;
    protected TextView textConfig;
    protected TextView textActiveFilename;
    protected TextView textErrorMessage;
    protected ImageView imageWifi;
    protected ImageView imageRobot;
    protected ImageView imageConfig;
    protected ImageView imageOp;
    protected ImmersiveMode immersion;
    protected UpdateUI updateUI;
    protected Dimmer dimmer;
    protected LinearLayout entireScreenLayout;
    protected FtcRobotControllerService controllerService;
    protected FtcEventLoop eventLoop;

    private Utility utility;
    protected ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FtcRobotControllerBinder binder = (FtcRobotControllerBinder) service;
            onServiceBind(binder.getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            controllerService = null;
        }
    };

    //CAMERAS
    //private Camera mCamera;
    //private CameraPreview mPreview;

    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

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

    private void updateHeader()
    {
        utility.updateHeader("Not configured", R.string.pref_hardware_config_filename, R.id.active_filename, R.id.header);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textConfig.setText("Configuration\n" + textActiveFilename.getText());

                if (textActiveFilename.getText().toString().equalsIgnoreCase("Not configured") || textActiveFilename.getText().toString().equalsIgnoreCase("No current file!")) {
                    imageConfig.setColorFilter(getResources().getColor(R.color.error_orange));
                } else {
                    imageConfig.setColorFilter(getResources().getColor(R.color.button));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (controllerService != null) unbindService(connection);

        RobotLog.cancelWriteLogcatToDisk(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        // When the window loses focus (e.g., the action overflow is shown),
        // cancel any pending hide action. When the window gains focus,
        // hide the system UI.
        if (hasFocus) {
            if (ImmersiveMode.apiOver19()){
                // Immersive flag only works on API 19 and above.
                immersion.hideSystemUI();
            }
        } else {
            immersion.cancelSystemUIHide();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ftc_robot_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // The string to launch this activity must match what's in AndroidManifest of FtcCommon for this activity.
                Intent settingsIntent = new Intent("com.qualcomm.ftccommon.FtcRobotControllerSettingsActivity.intent.action.Launch");
                startActivityForResult(settingsIntent, LaunchActivityConstantsList.FTC_ROBOT_CONTROLLER_ACTIVITY_CONFIGURE_ROBOT);
                return true;
            case R.id.action_about:
                // The string to launch this activity must match what's in AndroidManifest of FtcCommon for this activity.
                Intent intent = new Intent("com.qualcomm.ftccommon.configuration.AboutActivity.intent.action.Launch");
                startActivity(intent);
                return true;
            case R.id.action_exit_app:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // don't destroy assets on screen rotation
    }

    private void requestRobotSetup() {
        if (controllerService == null) return;

        FileInputStream fis = fileSetup();
        // if we can't find the file, don't try and build the robot.
        if (fis == null) { return; }

        HardwareFactory factory;

        // Modern Robotics Factory for use with Modern Robotics hardware
        HardwareFactory modernRoboticsFactory = new HardwareFactory(context);
        modernRoboticsFactory.setXmlInputStream(fis);
        factory = modernRoboticsFactory;

        eventLoop = new FtcEventLoop(factory, new FtcOpModeRegister(), callback, this);

        controllerService.setCallback(callback);
        controllerService.setupRobot(eventLoop);
    }

    private FileInputStream fileSetup() {

        final String filename = Utility.CONFIG_FILES_DIR
                + utility.getFilenameFromPrefs(R.string.pref_hardware_config_filename, Utility.NO_FILE) + Utility.FILE_EXT;

        FileInputStream fis;
        try {
            fis = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            String msg = "Cannot open robot configuration file - " + filename;
            utility.complainToast(msg, context);
            DbgLog.msg(msg);
            utility.saveToPreferences(Utility.NO_FILE, R.string.pref_hardware_config_filename);
            fis = null;
        }
        updateHeader();
        return fis;
    }

    private void requestRobotShutdown() {
        if (controllerService == null) return;
        controllerService.shutdownRobot();
    }

    private void requestRobotRestart() {
        requestRobotShutdown();
        requestRobotSetup();
    }

    public void showToast(final Toast toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast.show();
            }
        });
    }

    /** VISION **/

    private ColorBlobDetector detectorRed;
    private ColorBlobDetector detectorBlue;
    private static final ColorHSV colorRadius = new ColorHSV(50, 75, 127);

    private static final ColorHSV lowerBoundRed = new ColorHSV( (int)(305         / 360.0 * 255.0), (int)(0.200 * 255.0), (int)(0.300 * 255.0));
    private static final ColorHSV upperBoundRed = new ColorHSV( (int)((360.0+5.0) / 360.0 * 255.0), 255                 , 255);

    private static final ColorHSV lowerBoundBlue = new ColorHSV((int)(170.0       / 360.0 * 255.0), (int)(0.200 * 255.0), (int)(0.750 * 255.0));
    private static final ColorHSV upperBoundBlue = new ColorHSV((int)(227.0       / 360.0 * 255.0), 255                 , 255);

    private Mat mGray;
    private Mat mRgba;
    private FPS fpsCounter;

    private float focalLength;

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private void initialize()
    {
        //GET CAMERA PROPERTIES
        /*Camera cam = Cameras.getPrimaryCamera();
        assert cam != null;
        android.hardware.Camera.Parameters pam = cam.getCamera().getParameters();
        focalLength = pam.getFocalLength();
        cam.getCamera().release();*/

        //UPDATE COUNTER
        fpsCounter = new FPS();

        //Initialize all detectors here
        detectorRed  = new ColorBlobDetector(lowerBoundRed, upperBoundRed);
        detectorBlue = new ColorBlobDetector(lowerBoundBlue, upperBoundBlue);
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // input frame has RGBA format
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        //Size originalSize = mRgba.size();

        //DEBUG for the Nexus
        //Transform.flip(mRgba, Transform.FlipType.FLIP_BOTH);
        //Transform.flip(mGray, Transform.FlipType.FLIP_BOTH);

        //Transform.shrink(mRgba, new Size(480, 480), true);
        //Transform.shrink(mGray, new Size(480, 480), true);

        //DEBUG for the Moto G
        Transform.rotate(mGray, -90);
        Transform.rotate(mRgba, -90);

        fpsCounter.update();

        try {
            //Process the frame for the color blobs
            detectorRed.process(mRgba);
            detectorBlue.process(mRgba);

            //Get the list of contours
            List<Contour> contoursRed = detectorRed.getContours();
            List<Contour> contoursBlue = detectorBlue.getContours();

            //Get color analysis
            Beacon beacon = new Beacon(mRgba.size());
            Beacon.BeaconColorAnalysis colorAnalysis = beacon.analyzeColor(contoursRed, contoursBlue, mRgba, mGray);

            //Draw red and blue contours
            Drawing.drawContours(mRgba, contoursRed, new ColorRGBA(255, 0, 0), 2);
            Drawing.drawContours(mRgba, contoursBlue, new ColorRGBA(0, 0, 255), 2);

            //Transform.enlarge(mRgba, originalSize, true);
            //Transform.enlarge(mGray, originalSize, true);

            //Draw text
            Drawing.drawText(mRgba, colorAnalysis.toString(),
                    new Point(0, 8), 1.0f, new ColorGRAY(255), Drawing.Anchor.BOTTOMLEFT);

            //Write status text file
            IO.writeTextFile("/FTCVision/", "rc_colors.txt", colorAnalysis.toString(), true);

        }
        catch (Exception e)
        {
            Drawing.drawText(mRgba, "Analysis Error", new Point(0, 8), 1.0f, new ColorRGBA("#F44336"), Drawing.Anchor.BOTTOMLEFT);
            e.printStackTrace();
        }

        Drawing.drawText(mRgba, "FPS: " + fpsCounter.getFPSString(), new Point(0, 24), 1.0f, new ColorRGBA("#ffffff")); //"#2196F3"

        return mRgba;
    }
}
