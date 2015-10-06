package org.lasarobotics.vision.android;

import android.os.Environment;

/**
 * Vision utilities
 */
public class Util {

    public static String getDCIMDirectory()
    {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    }
}
