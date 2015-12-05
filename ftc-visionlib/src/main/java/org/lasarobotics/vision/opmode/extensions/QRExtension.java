package org.lasarobotics.vision.opmode.extensions;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;

import org.lasarobotics.vision.detection.QRDetector;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.opencv.core.Mat;

/**
 * Uses ZXing library to detect QR codes
 */
public class QRExtension implements VisionExtension {
    private QRDetector qrd;
    private Result lastResult;

    private String reason;
    private String text;

    public boolean hasErrorReason() {
        return reason != null;
    }

    public String getErrorReason() {
        return reason != null ? reason : "";
    }

    public boolean hasText() {
        return text != null;
    }

    public String getText() {
        return text != null ? text : "";
    }

    @Override
    public void init(VisionOpMode opmode) {
        qrd = new QRDetector();
    }

    public QRDetector.Orientation getOrientation() {
        return QRDetector.getOrientation(lastResult.getResultPoints());
    }

    @Override
    public void loop(VisionOpMode opmode) {
        //do nothing
    }

    @Override
    public Mat frame(VisionOpMode opmode, Mat rgba, Mat gray) {
        try {
            lastResult = qrd.detectFromMat(rgba);
            text = lastResult.getText();
            reason = null;
            qrd.reset();
        } catch (NotFoundException ex) {
            text = null;
            reason = "QR Code not found. Extra info: " + ex.getMessage();
        } catch (ChecksumException ex) {
            text = null;
            reason = "QR Code has invalid checksum. Extra info: " + ex.getMessage();
        } catch (FormatException ex) {
            text = null;
            reason = "QR Code not properly formatted. Extra info: " + ex.getMessage();
        }
        return rgba;
    }

    @Override
    public void stop(VisionOpMode opmode) {
        //do nothing
    }
}