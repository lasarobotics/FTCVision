package org.lasarobotics.vision.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;

import org.lasarobotics.vision.android.Camera;

/**
 * Implements the Android camera
 */
public enum Cameras {
    PRIMARY(0),
    SECONDARY(1),
    OTHER_1(2),
    OTHER_2(3);

    int id;

    Cameras(int id)
    {
        this.id = id;
    }

    public int getID()
    {
        return id;
    }

    public Camera createCamera()
    {
        return new Camera(this);
    }

    public static boolean isHardwareAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}
