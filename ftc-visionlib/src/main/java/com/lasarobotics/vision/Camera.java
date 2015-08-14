package com.lasarobotics.vision;

/**
 * Implements a single Android camera
 */
@SuppressWarnings("deprecation")
public class Camera {
    android.hardware.Camera c;

    public Camera(android.hardware.Camera camera)
    {
        this.c = camera;
    }

    public android.hardware.Camera getCamera() { return c; }
}
