package org.lasarobotics.vision;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Image manipulation and correction
 */
public class Image {
    /**
     * Rotate an image by an angle (counterclockwise)
     * @param image Image matrix
     * @param angle Angle to rotate by (counterclockwise) from -360 to 360
     */
    static void rotate(Mat image, double angle)
    {
        //Find the center of the image
        int len = Math.max(image.rows(), image.cols());
        Point center = new Point(len/2, len/2);

        //Retrieve the rotation matrix
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);

        //Warp the image by the rotation matrix
        Imgproc.warpAffine(image, image, rotationMatrix, new Size(len, len));
    }
}
