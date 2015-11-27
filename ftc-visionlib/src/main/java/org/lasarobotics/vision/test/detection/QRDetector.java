package org.lasarobotics.vision.test.detection;

import android.graphics.Bitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Uses Google's ZXing library to detect QR codes
 */
public class QRDetector {
    QRCodeReader qrc;

    public enum Orientation {
        UP, //Code is normal
        DOWN, //Code is upside-down
        LEFT, //Code has been rotated left
        RIGHT //Code has been rotated right
    }

    public QRDetector() {
        qrc = new QRCodeReader();
    }

    public Result detectFromMat(Mat rgba) throws NotFoundException, ChecksumException, FormatException {
        //Convert OpenCV Mat into Bitmap and detect from that
        Bitmap bMap = Bitmap.createBitmap(rgba.width(), rgba.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, bMap);
        return detectFromBitmap(bMap);
    }

    public Result detectFromBitmap(Bitmap bMap) throws FormatException, ChecksumException, NotFoundException {
        //Convert Bitmap into BinaryBitmap
        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        //Copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(),intArray);

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
}
