package org.lasarobotics.vision.android;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * List of available Android cameras
 */
public enum Cameras {
    //Primary (front-facing) cameraControl
    PRIMARY(0),
    //Secondary (screen-facing) cameraControl
    SECONDARY(1),
    //Other cameraControl - ID 2
    OTHER_1(2),
    //Other cameraControl - ID 3
    OTHER_2(3);

    final int id;

    Cameras(int id) {
        this.id = id;
    }

    /**
     * Checks whether the device supports cameras
     *
     * @param context Current context
     * @return True if supported, false otherwise
     */
    public static boolean isHardwareAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * Get the cameraControl ID
     *
     * @return Camera ID
     */
    public int getID() {
        return id;
    }

    /**
     * Returns a Camera instance from this Camera ID
     * @return The cameraControl instance
     */
    public Camera createCamera() {
        return new Camera(this);
    }
}
