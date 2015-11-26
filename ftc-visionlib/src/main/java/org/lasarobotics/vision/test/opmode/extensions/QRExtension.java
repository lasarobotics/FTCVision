package org.lasarobotics.vision.test.opmode.extensions;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;

import org.lasarobotics.vision.test.opmode.VisionOpMode;
import org.opencv.core.Mat;

import org.lasarobotics.vision.test.detection.QRDetector;

/**
 * Uses ZXing library to detect QR codes
 */
public class QRExtension implements VisionExtension {
    private QRDetector qrd;
    private Result lastResult;
    private boolean enabled = false;
    private String qrText;

    @Override
    public void init(VisionOpMode opmode) {
        qrd = new QRDetector();
    }

    public void startLooking() {
        enabled = true;
    }

    public String getQrText() {
        return qrText;
    }

    @Override
    public void loop(VisionOpMode opmode) {
        //do nothing
    }

    @Override
    public Mat frame(VisionOpMode opmode, Mat rgba, Mat gray) {
        if(enabled) {
            try {
                lastResult = qrd.detectFromMat(rgba);
                qrText = lastResult.getText();
                enabled = false;
            } catch(NotFoundException|ChecksumException|FormatException ex) {
                //do nothing
            }
        }
        return rgba;
    }

    @Override
    public void stop(VisionOpMode opmode) {
        //do nothing
    }
}
