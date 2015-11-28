package org.lasarobotics.vision.test.opmode.extensions;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

import org.lasarobotics.vision.test.image.Drawing;
import org.lasarobotics.vision.test.opmode.VisionOpMode;
import org.lasarobotics.vision.test.util.color.ColorRGBA;
import org.opencv.core.Mat;

import org.lasarobotics.vision.test.detection.QRDetector;
import org.opencv.core.Point;

/**
 * Uses ZXing library to detect QR codes
 */
public class QRExtension implements VisionExtension {
    private QRDetector qrd;
    private Result lastResult;

    private boolean shouldColorCorrect = false;
    public void setShouldColorCorrect(boolean shouldColorCorrect) {
        this.shouldColorCorrect = shouldColorCorrect;
    }
    public boolean doesColorCorrect() {
        return shouldColorCorrect;
    }

    private boolean matDebugInfo = false;
    public boolean hasDebugInfo(boolean matDebugInfo) {
        return matDebugInfo;
    }
    public void setDebugInfo(boolean matDebugInfo) {
        this.matDebugInfo = matDebugInfo;
    }

    private boolean hasInit = false;
    public boolean hasInit() {
        return hasInit;
    }

    private String reason;
    public boolean hasErrorReason() {
        return reason != null;
    }
    public String getErrorReason() {
        return reason != null ? reason : "";
    }

    private String text;
    public boolean hasText() {
        return text != null;
    }
    public String getText() {
        return text != null ? text : "";
    }

    @Override
    public void init(VisionOpMode opmode) {
        qrd = new QRDetector();
        hasInit = true;
    }

    public QRDetector.Orientation getOrientation() {
        return lastResult == null ? QRDetector.Orientation.UNKNOWN : QRDetector.getOrientation(lastResult.getResultPoints());
    }

    @Override
    public void loop(VisionOpMode opmode) {
        //do nothing
    }

    @Override
    public Mat frame(VisionOpMode opmode, Mat rgba, Mat gray) {
        try {
            lastResult = qrd.detectFromMat(rgba);
            if(matDebugInfo) {
                ResultPoint[] rp = lastResult.getResultPoints();
                for(int i = 0; i < rp.length - 1; i++) {
                    ResultPoint topLeft = lastResult.getResultPoints()[i];
                    ResultPoint bottomRight = lastResult.getResultPoints()[i+1];
                    Drawing.drawLine(rgba, new Point(topLeft.getX(), topLeft.getY()), new Point(bottomRight.getX(), bottomRight.getY()), new ColorRGBA("#ff0000"));
                }
            }
            text = lastResult.getText();
            reason = null;
            qrd.reset();
        } catch(NotFoundException ex) {
            text = null;
            reason = "QR Code not found. Extra info: " + ex.getMessage();
        } catch(ChecksumException ex) {
            text = null;
            reason = "QR Code has invalid checksum. Extra info: " + ex.getMessage();
        } catch(FormatException ex) {
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