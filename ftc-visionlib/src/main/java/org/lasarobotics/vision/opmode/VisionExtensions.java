package org.lasarobotics.vision.opmode;

/**
 * List of Vision Extensions for the VisionOpMode
 */
public enum VisionExtensions {
    BEACON_COLOR(1);

    int id;
    VisionExtensions(int id)
    {
        this.id = id;
    }
}
