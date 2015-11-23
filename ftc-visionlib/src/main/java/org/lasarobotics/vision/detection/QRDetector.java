package org.lasarobotics.vision.detection;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Russell on 11/23/2015.
 */
public class QRDetector {
    public static final int CV_QR_NORTH = 0;
    public static final int CV_QR_EAST = 1;
    public static final int CV_QR_SOUTH = 2;
    public static final int CV_QR_WEST = 3;

    protected class FourInts {
        public final int one;
        public final int two;
        public final int three;
        public final int four;
        public FourInts(int one, int two, int three, int four) {
            this.one = one;
            this.two = two;
            this.three = three;
            this.four = four;
        }
    }

    public QRDetector(Mat image, Mat gray) {
        if(image.empty()) {
            throw new RuntimeException("Image passed to QRDetector is empty");
        }

        // Creation of Intermediate 'Image' Objects required later
        Mat edges = new Mat(image.size(), CvType.makeType(image.depth(), 1)); // To hold edge image
        Mat traces = new Mat(image.size(), CvType.CV_8UC3);

        Mat qr, qr_raw, qr_gray, qr_thres;

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        int mark, A, B, C, top, right, bottom, median1, median2, outlier;
        float AB, BC, CA, dist, slope, area, arear, areab, large, padding;

        int align, orientation;

        int DBG = 1; //Debug flag

        int key = 0;

        traces.setTo(new Scalar(0, 0, 0));
        qr_raw = Mat.zeros(100, 100, CvType.CV_8UC3);
        qr = Mat.zeros(100, 100, CvType.CV_8UC3);
        qr_gray = Mat.zeros(100, 100, CvType.CV_8UC1);
        qr_thres = Mat.zeros(100, 100, CvType.CV_8UC1);

        Imgproc.Canny(gray, edges, 100, 200, 3, true);

        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
    }
}
