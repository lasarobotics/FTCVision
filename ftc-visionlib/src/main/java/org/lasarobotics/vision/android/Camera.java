package org.lasarobotics.vision.android;

import android.content.pm.PackageManager;

import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements a single Android cameraControl
 * Used to get information about an Android native cameraControl, as well as perform a few critical functions
 */
@SuppressWarnings("deprecation")
public class Camera {
    private android.hardware.Camera c;
    private int id;

    /**
     * Instantiate an instance of a cameraControl
     * WARNING! Each cameraControl can only be instantiated in once place at a time - be sure to release() when you are done.
     *
     * @param camera Camera ID
     */
    public Camera(Cameras camera) {
        makeCamera(camera.getID());
    }

    /**
     * Checks if the hardware supports a cameraControl. It should.
     *
     * @return True if hardware supports cameraControl, false otherwise
     */
    public static boolean isHardwareAvailable() {
        return Util.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * Get the number of cameras available
     * @return Number of native cameras
     */
    public static int getCameraCount() {
        return android.hardware.Camera.getNumberOfCameras();
    }

    /**
     * Get an instance of a cameraControl
     * @return The native cameraControl instance
     */
    public android.hardware.Camera getCamera() {
        return c;
    }

    private void makeCamera(int id) {
        this.id = id;
        this.c = android.hardware.Camera.open(id);
    }

    /**
     * Unlock the cameraControl for use by other applications
     */
    public void unlock() {
        c.unlock();
    }

    /**
     * Lock the cameraControl so the current application can use it - be sure to unlock()
     */
    public void lock() {
        c.lock();
    }

    /**
     * Unlock and release the cameraControl instance.
     * After calling this method, destory the Camera instance and recreate it if you need to use it again
     */
    public void release() {
        c.release();
    }

    /**
     * Get the internal ID of the cameraControl
     * @return The cameraControl's ID
     */
    public int getID() {
        return id;
    }

    /**
     * Tests whether the cameraControl exists (i.e. is not null)
     * @return True if cameraControl is not null, false otherwise
     */
    public boolean doesExist() {
        return (c != null);
    }

    /**
     * Get the largest frame size supported by the cameraControl
     * @return Largest frame size in pixels
     */
    public Size getLargestFrameSize() {
        android.hardware.Camera.Size s = c.getParameters().getPreviewSize();
        return new Size(s.width, s.height);
    }

    /**
     * Get the best frame size for real-time video
     * @return Best frame size, generally significantly smaller than the largest frame size
     */
    public Size getBestFrameSize() {
        android.hardware.Camera.Size s = c.getParameters().getPreferredPreviewSizeForVideo();
        return new Size(s.width, s.height);
    }

    /**
     * Get all frame sizes supported by the cameraControl
     * @return A list of supported frame sizes
     */
    public List<Size> getAllFrameSizes() {
        List<Size> l = new ArrayList<>();
        for (android.hardware.Camera.Size s : c.getParameters().getSupportedPreviewSizes())
            l.add(new Size(s.width, s.height));
        return l;
    }
}
