package org.lasarobotics.vision.opmode.extensions;

import android.hardware.Camera;
import android.test.PerformanceTestCase;
import android.util.Log;

import org.lasarobotics.vision.opmode.VisionOpMode;
import org.lasarobotics.vision.util.MathUtil;
import org.lasarobotics.vision.util.color.Color;
import org.opencv.core.Mat;

/**
 * Camera control extension
 *
 * Allows manual control of white balance, exposure, etc.
 */
@SuppressWarnings("deprecation")
public class CameraControlExtension implements VisionExtension {

    private boolean paramsSet = false;
    private ColorTemperature colorTemp = ColorTemperature.AUTO;
    private int expoComp = 0;
    private int minExpo = 0;
    private int maxExpo = 0;
    private boolean autoExpoComp = true;

    @SuppressWarnings("deprecation")
    public enum ColorTemperature
    {
        AUTO(Camera.Parameters.WHITE_BALANCE_AUTO, false),
        K10000_TWILIGHT(Camera.Parameters.WHITE_BALANCE_TWILIGHT, 10000),
        K8000_SHADE(Camera.Parameters.WHITE_BALANCE_SHADE, 8000),
        K3000_WARM_FLOURESCENT(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT, 3000),
        K5000_FLOURESCENT(Camera.Parameters.WHITE_BALANCE_FLUORESCENT, 5000),
        K2400_INCANDESCENT(Camera.Parameters.WHITE_BALANCE_INCANDESCENT, 2400),
        K6500_DAYLIGHT(Camera.Parameters.WHITE_BALANCE_DAYLIGHT, 6500),
        K4000_CLOUDY_DAYLIGHT(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT, 4000);

        String s;
        boolean lock;
        int kelvin;
        ColorTemperature(String s, int kelvin)
        {
            this.s = s;
            this.lock = true;
            this.kelvin = kelvin;
        }
        ColorTemperature(String s, boolean lock)
        {
            this.s = s;
            this.lock = lock;
            this.kelvin = 0;
        }

        public int getApproxTemperatureKelvin()
        {
            return kelvin;
        }
        public boolean isAutomatic()
        {
            return this.kelvin == 0;
        }
    }

    public void setAutoExposureCompensation()
    {
        this.expoComp = 0;
        this.autoExpoComp = true;
        paramsSet = false;
    }

    public void setManualExposureCompensation(int expoComp)
    {
        this.expoComp = expoComp;
        this.autoExpoComp = false;
        paramsSet = false;
    }

    public void setColorTemperature(ColorTemperature colorTemp)
    {
        this.colorTemp = colorTemp;
        paramsSet = false;
    }


    public int getMinExposureCompensation()
    {
        return minExpo;
    }

    public int getMaxExposureCompensation()
    {
        return maxExpo;
    }

    public ColorTemperature getColorTemp() {
        return colorTemp;
    }

    public int getExposureCompensation() {
        return expoComp;
    }

    public boolean isAutomaticExposureCompensation()
    {
        return autoExpoComp;
    }

    @Override
    public void init(VisionOpMode opmode) {
        paramsSet = false;
        colorTemp = ColorTemperature.AUTO;
        expoComp = 0;
        autoExpoComp = true;
    }

    @SuppressWarnings("AccessStaticViaInstance")
    @Override
    public void loop(VisionOpMode opmode) {
        if (paramsSet) return;

        Camera.Parameters p = opmode.openCVCamera.getCamera().getParameters();

        //Get cameraControl info
        this.minExpo = p.getMinExposureCompensation();
        this.maxExpo = p.getMaxExposureCompensation();
        expoComp = (int)MathUtil.coerce(minExpo, maxExpo, expoComp);

        //Set white balance
        p.setWhiteBalance(colorTemp.s);
        if (p.isAutoWhiteBalanceLockSupported())
            p.setAutoWhiteBalanceLock(colorTemp.lock);
        else
            Log.w("Vision","Manual white balance not supported.");

        //Set exposure compensation
        if (!autoExpoComp)
            p.setExposureCompensation(expoComp);
        if (p.isAutoExposureLockSupported())
            p.setAutoExposureLock(!autoExpoComp);
        else
            Log.w("Vision","Manual exposure compensation not supported.");

        //Update cameraControl parameters
        try {
            opmode.openCVCamera.getCamera().setParameters(p);
        } catch (RuntimeException e)
        {
            //Sometimes, we fail to set the parameters
            e.printStackTrace();
            paramsSet = false;
            return;
        }
        paramsSet = true;
    }

    @Override
    public Mat frame(VisionOpMode opmode, Mat rgba, Mat gray) {
        return rgba;
    }

    @SuppressWarnings("AccessStaticViaInstance")
    @Override
    public void stop(VisionOpMode opmode) {
        if (opmode.openCVCamera == null)
            return;

        Camera.Parameters p = opmode.openCVCamera.getCamera().getParameters();

        if (p.isAutoWhiteBalanceLockSupported())
            p.setAutoWhiteBalanceLock(false);
        if (p.isAutoExposureLockSupported())
            p.setAutoExposureLock(false);

        //Update cameraControl parameters
        try {
            opmode.openCVCamera.getCamera().setParameters(p);
        } catch (RuntimeException e)
        {
            //Sometimes, we fail to set the parameters
            e.printStackTrace();
        }
    }
}
