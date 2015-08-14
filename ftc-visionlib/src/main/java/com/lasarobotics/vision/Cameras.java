package com.lasarobotics.vision;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Implements the Android camera
 */
@SuppressWarnings("deprecation")
public class Cameras {
    public static boolean isHardwareAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getPrimaryCamera(){
        try {
            return new Camera(android.hardware.Camera.open()); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            return null;
        }
    }

    public static Camera getSecondaryCamera(){
        try {
            if (android.hardware.Camera.getNumberOfCameras() >= 2) {
                return new Camera(android.hardware.Camera.open(1)); // attempt to get a Camera instance
            } else
            {
                return null;
            }
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            return null;
        }
    }
}
