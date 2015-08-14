package com.lasarobotics.ftc.camera;

import android.os.Environment;

/**
 * Created by Ehsan on 8/14/2015.
 */
public class Util {
    public static String getDCIMDirectory()
    {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    }
}
