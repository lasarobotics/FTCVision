package org.lasarobotics.vision.opmode.extensions;

import org.lasarobotics.vision.opmode.extensions.BeaconDetectorExtension;

/**
 * List of Vision extensions
 */
public enum VisionExtensions {
    BEACON_COLOR(1, BeaconDetectorExtension.class);

    int i;
    Class<? extends VisionExtension> c;

    VisionExtensions(int i, Class<? extends VisionExtension> c)
    {
        this.i = i;
        this.c = c;
    }

    public int getID()
    {
        return i;
    }
    public Class<? extends VisionExtension> getExtension()
    {
        return c;
    }
}