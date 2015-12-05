package org.lasarobotics.vision.detection;

import android.graphics.Bitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Uses Google's ZXing library to detect QR codes
 */
public class QRDetector {
    private QRCodeReader qrc;

    public QRDetector() {
        qrc = new QRCodeReader();
    }

    /* Test data:
     * Up:    (77.5, 98.0)  (77.5, 33.5)  (143.5, 34.5)
     * Down:  (143.0, 32.0) (144.0, 94.0) (82.5, 94.5)
     * Left:  (142.5, 99.0) (75.5, 98.5)  (76.5, 31.5)
     * Right: (85.0, 32.0)  (148.0, 33.0) (146.5, 95.0)
     */
    public static Orientation getOrientation(ResultPoint[] points) {
        if (points.length < 3) {
            throw new RuntimeException("Wrong number of points: " + points.length + ", expected at least 3.");
        }

        //Determine if first two X or second two X are closest
        float xDiffOneTwo = Math.abs(points[0].getX() - points[1].getX());
        float xDiffTwoThree = Math.abs(points[1].getX() - points[2].getX());
        if (xDiffOneTwo < xDiffTwoThree) {
            //Code is orientated up or down
            if (points[0].getY() > points[1].getY()) {
                return Orientation.UP;
            } else {
                return Orientation.DOWN;
            }
        } else {
            //Code is orientated left or right
            if (points[1].getY() > points[2].getY()) {
                return Orientation.LEFT;
            } else {
                return Orientation.RIGHT;
            }
        }
    }

    public static Orientation getOrientationFromResult(Result r) {
        return getOrientation(r.getResultPoints());
    }

    public Result detectFromMat(Mat rgba) throws NotFoundException, ChecksumException, FormatException {
        //Convert OpenCV Mat into Bitmap and detect from that
        Bitmap bMap = Bitmap.createBitmap(rgba.width(), rgba.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, bMap);
        return detectFromBitmap(bMap);
    }

    public Result detectFromBitmap(Bitmap bMap) throws FormatException, ChecksumException, NotFoundException {
        //Convert Bitmap into BinaryBitmap
        int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
        //Copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);

        //Send BinaryBitmap to other method
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        return detectFromBinaryBitmap(bitmap);
    }

    public Result detectFromBinaryBitmap(BinaryBitmap map) throws NotFoundException, ChecksumException, FormatException {
        //Read QR data from BinaryBitmap
        return qrc.decode(map);
    }

    public void reset() {
        qrc.reset();
    }

    public enum Orientation {
        UP("Up"), //Code is normal
        DOWN("Down"), //Code is upside-down
        LEFT("Left"), //Code has been rotated left
        RIGHT("Right"); //Code has been rotated right

        private String s;

        Orientation(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }
    }
}
