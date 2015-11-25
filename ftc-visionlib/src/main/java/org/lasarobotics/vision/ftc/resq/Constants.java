package org.lasarobotics.vision.ftc.resq;

/**
 * Res-Q field and object constants
 */
public abstract class Constants {
    public static final double BEACON_WIDTH = 21.8;     //entire beacon width
    public static final double BEACON_HEIGHT = 14.5;    //entire beacon height
    public static final double BEACON_WH_RATIO = BEACON_WIDTH/BEACON_HEIGHT; //entire beacon ratio
    public static final double BEACON_BUTTON_HEIGHT = 2; //Height of button

    public static final double CM_FT_SCALE = 1.0/30.48; //Conversion ratio for cm to ft

    public static final double MAX_DIST_FROM_BEACON = 10; //Maximum possible distance from beacon in feet
    public static final double DIST_CHANGE_THRESHOLD = 2; //Maximum change per 1/4 second of distance from beacon in feet

    public static double CAMERA_HOR_VANGLE = 0; //Horizontal view angle of the camera
    public static double CAMERA_VERT_VANGLE = 0; //Vertical view angle of the camera
}
