package org.lasarobotics.vision.detection;

import org.lasarobotics.vision.detection.objects.Rectangle;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.util.color.Color;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * Implements cascade object detection, a rapid way of locating features such as human faces
 */
public class CascadeObjectDetection {
    CascadeClassifier classifier = new CascadeClassifier();

    public CascadeObjectDetection(CascadeClassifier classifier) {
        this.classifier = classifier;
    }
    public CascadeObjectDetection(String filename) {
        classifier.load(filename);
    }

    /**
     * @param imgGray grayscale image to process
     * @return MatOfRect containing detected features
     */
    public MatOfRect detect(Mat imgGray) {
        // equalize levels to increase contrast and improve feature detection
        Imgproc.equalizeHist(imgGray, imgGray);

        // detect and return features
        MatOfRect objects = new MatOfRect();
        classifier.detectMultiScale(imgGray, objects);
        return objects;
    }

    /**
     * @param out image to draw onto
     * @param objects MatOfPoint returned by detect()
     * @param color color of rectangles to draw
     */
    public static void drawObjects(Mat out, MatOfRect objects, Color color) {
        for(Rect rect : objects.toArray()) {
            Drawing.drawRectangle(out, new Rectangle(rect), color);
        }
    }
}
