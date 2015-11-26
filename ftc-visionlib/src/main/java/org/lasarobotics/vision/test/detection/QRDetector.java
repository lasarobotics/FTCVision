package org.lasarobotics.vision.test.detection;

import android.graphics.Bitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.DataMatrixReader;
import com.google.zxing.qrcode.QRCodeReader;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Created by Russell on 11/26/2015.
 */
public class QRDetector {
    QRCodeReader qrc;

    public QRDetector() {
        qrc = new QRCodeReader();
    }

    public Result detectFromMat(Mat rgba) throws NotFoundException, ChecksumException, FormatException {
        //Convert OpenCV Mat into BinaryBitmap so that ZXing understands it
        Bitmap bMap = Bitmap.createBitmap(rgba.width(), rgba.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, bMap);
        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        //Copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(),intArray);

        //Perform actual reading of image
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        return detectFromBinaryBitmap(bitmap);
    }

    public Result detectFromBinaryBitmap(BinaryBitmap map) throws NotFoundException, ChecksumException, FormatException {
        Reader reader = new DataMatrixReader();
        return reader.decode(map);
    }

    public void reset() {
        qrc.reset();
    }
}
