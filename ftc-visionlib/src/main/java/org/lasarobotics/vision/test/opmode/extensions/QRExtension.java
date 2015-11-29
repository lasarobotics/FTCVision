package org.lasarobotics.vision.test.opmode.extensions;

import android.util.Log;
import android.widget.Toast;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

import org.lasarobotics.vision.test.detection.objects.Contour;
import org.lasarobotics.vision.test.detection.objects.Rectangle;
import org.lasarobotics.vision.test.image.Drawing;
import org.lasarobotics.vision.test.image.Transform;
import org.lasarobotics.vision.test.opmode.VisionOpMode;
import org.lasarobotics.vision.test.util.SoundFeedback;
import org.lasarobotics.vision.test.util.color.Color;
import org.lasarobotics.vision.test.util.color.ColorRGBA;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.lasarobotics.vision.test.detection.QRDetector;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.Arrays;

/**
 * Uses ZXing library to detect QR codes
 */
public class QRExtension implements VisionExtension {
    private static final float GRAY_CARD_MULTIPLIER = 1.72f; //Multiplies line size that extends to gray card
    private static final float GRAY_CARD_BOX_SIZE = 0.3f; //Multiplies gray box size
    private static final int GRAY_CARD_SAMPLE_INTERVAL = 5; //Number of pixels for which one is sampled
    private static final double GRAY_CARD_LEVEL = 119.0; //18% gray
    private QRDetector qrd;
    private Result lastResult;
    private double redMul; //for color correction
    private double greenMul;
    private double blueMul;

    private boolean isEnabled = true;
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    public boolean isEnabled() {
        return isEnabled;
    }

    private boolean stopOnFTCQRCode = true;
    public void setStopOnFTCQRCode(boolean stopOnFTCQRCode) {
        this.stopOnFTCQRCode = stopOnFTCQRCode;
    }
    public boolean doesStopOnFTCQRCode() {
        return stopOnFTCQRCode;
    }

    private boolean playSoundOnFound = true;
    public void setPlaySoundOnFound(boolean playSoundOnFound) {
        this.playSoundOnFound = playSoundOnFound;
    }
    public boolean doesPlaySoundOnFound() {
        return playSoundOnFound;
    }

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

    public FTCQRCodeInfo getCodeInfo() {
        if(text == null) {
            return new FTCQRCodeInfo(false, -1, -1);
        } else {
            return QRExtension.parseFTCQRCode(text);
        }
    }

    //Sample valid FTCQRCode: !9874_0
    //! is just a character to make sure we don't pick up stray qr codes
    //9874 is team number
    //_ is team number and value separator
    //0 is value
    public static FTCQRCodeInfo parseFTCQRCode(String qrCode) {
        if(qrCode.charAt(0) != '!') {
            return new FTCQRCodeInfo(false, -1, -1);
        }
        int team = 0;
        int i;
        for(i = 1; i < qrCode.length(); i++) {
            char c = qrCode.charAt(i);
            if(c >= '0' && c <= '9') {
                //c is a digit
                int in = c - '0'; //integer representation of c
                team *= 10;
                team += in;
            } else {
                if(c == '_') {
                    //proper separator
                    break;
                } else {
                    return new FTCQRCodeInfo(false, team, -1);
                }
            }
        }
        i++;
        int val = 0;
        for(; i < qrCode.length(); i++) {
            char c = qrCode.charAt(i);
            if(c >= '0' && c <= '9') {
                //c is a digit
                int in = c - '0'; //integer representation of c
                val *= 10;
                val += in;
            } else {
                return new FTCQRCodeInfo(false, team, val);
            }
        }
        return new FTCQRCodeInfo(true, team, val);
    }

    public static class FTCQRCodeInfo {
        private boolean isValid;
        private int team;
        private int num;
        public FTCQRCodeInfo(boolean isValid, int team, int num) {
            this.isValid = isValid;
            this.team = team;
            this.num = num;
        }
        public boolean isValid() {
            return isValid;
        }
        public int getTeam() {
            return team;
        }
        public int getNum() {
            return num;
        }
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
        if(!isEnabled) {
            if(shouldColorCorrect) {
                Core.multiply(rgba, new Scalar(redMul, greenMul, blueMul), rgba);
            }
            return rgba;
        }
        try {
            lastResult = qrd.detectFromMat(rgba);

            if(playSoundOnFound) {
                if(lastResult.getResultPoints().length >= 3) {
                    SoundFeedback.playBeep(SoundFeedback.Stream.RING);
                }
            }
            if(shouldColorCorrect) {
                ResultPoint[] rp = lastResult.getResultPoints();
                if(rp.length >= 3) {
                    float p0p1avgX = (rp[0].getX() + rp[1].getX()) / 2;
                    float p0p1avgY = (rp[0].getY() + rp[1].getY()) / 2;
                    float p1p0yDiff = (rp[1].getY() - rp[0].getY());
                    float p2p1xDiff = (rp[2].getX() - rp[1].getX());
                    float p2p1yDiff = (rp[2].getY() - rp[1].getY());
                    float p2p0yDiff = (rp[2].getY() - rp[0].getY());
                    float p2p0xDiff = (rp[2].getY() - rp[0].getY());
                    float adjMult = 1 + GRAY_CARD_MULTIPLIER;
                    float np1x = p0p1avgX + p2p1xDiff * adjMult;
                    float np1y = p0p1avgY + p2p1yDiff * adjMult;
                    float xOffset = p2p1xDiff * GRAY_CARD_MULTIPLIER;
                    float yOffset = p2p1yDiff * GRAY_CARD_MULTIPLIER;
                    Point[] box = new Point[]{
                            new Point(rp[1].getX() + xOffset, rp[1].getY() + yOffset),
                            new Point(rp[2].getX() + xOffset, rp[2].getY() + yOffset),
                            new Point(rp[0].getX() + xOffset + (rp[2].getX() - rp[1].getX()), rp[2].getY() + yOffset + (rp[0].getY() - rp[1].getY())),
                            new Point(rp[0].getX() + xOffset, rp[0].getY() + yOffset)
                    };
                    Point upperMost = new Point(Float.MAX_VALUE, Float.MAX_VALUE);
                    int upperMostIndex = -1;
                    float centerX = 0;
                    float centerY = 0;
                    for (int i = 0; i < box.length; i++) {
                        if (box[i].y < upperMost.y) {
                            upperMost = box[i];
                            upperMostIndex = i;
                        }
                        centerX += box[i].x;
                        centerY += box[i].y;
                    }
                    int oppUpperMostIndex = (upperMostIndex + 2) % box.length;
                    Point oppUpperMost = box[oppUpperMostIndex]; //Point opposite side of uppermost
                    centerX /= box.length;
                    centerY /= box.length;

                    int otherIndex = (oppUpperMostIndex + 1) % 4;
                    float line1 = (float) Math.sqrt(Math.pow(oppUpperMost.x - box[otherIndex].x, 2) + Math.pow(oppUpperMost.y - box[otherIndex].y, 2));
                    float line2 = (float) (oppUpperMost.y - box[otherIndex].y);
                    float angleOfRotation = (float) Math.acos(line2 / line1);
                    float newAngleOfRotation = angleOfRotation % (float) (Math.PI / 2);
                    if (newAngleOfRotation > Math.PI / 4) {
                        int newOtherIndex = (otherIndex + 1) % 4;
                        int newOppUpperMostIndex = (oppUpperMostIndex + 1) % 4;
                        line2 = (float) (box[newOppUpperMostIndex].y - box[newOtherIndex].y);
                    }
                    float newSquareSideLength = line2 / (float) (Math.cos(newAngleOfRotation) + Math.sin(newAngleOfRotation));
                    Point[] newBox = new Point[]{
                            new Point(centerX - newSquareSideLength / 2, centerY - newSquareSideLength / 2),
                            new Point(centerX + newSquareSideLength / 2, centerY + newSquareSideLength / 2)
                    };
                    int[] col = getAvgColor(rgba, new Rectangle(newBox));
                    //Drawing.drawText(rgba, "THIS IS THE COLOR", new Point(100, 600), 1.0f, new ColorRGBA(col[0], col[1], col[2]));
                    redMul = GRAY_CARD_LEVEL / col[0];
                    greenMul = GRAY_CARD_LEVEL / col[1];
                    blueMul = GRAY_CARD_LEVEL / col[2];
                    if (stopOnFTCQRCode) {
                        isEnabled = false;
                    }
                    if (matDebugInfo) {
                        ColorRGBA red = new ColorRGBA("#ff0000");
                        ColorRGBA blue = new ColorRGBA("#88ccff");
                        ColorRGBA green = new ColorRGBA("#66ff99");
                        ColorRGBA white = new ColorRGBA("#ffffff");
                        Drawing.drawLine(rgba, new Point(p0p1avgX, p0p1avgY), new Point(np1x, np1y), green);
                        Drawing.drawRectangle(rgba, new Point(centerX - 1, centerY - 1), new Point(centerX + 1, centerY + 1), red);
                        Drawing.drawContour(rgba, new Contour(new MatOfPoint(new Point[]{
                                box[3],
                                box[0],
                                new Point(box[3].x, box[0].y)
                        })), blue);
                        Drawing.drawText(rgba, "0", new Point(rp[0].getX(), rp[0].getY()), 1.0f, blue);
                        Drawing.drawLine(rgba, upperMost, oppUpperMost, blue);
                        for (int i = 0; i < box.length; i++) {
                            Drawing.drawText(rgba, String.valueOf(i), box[i], 1.0f, blue);
                        }
                        for (int i = 0; i < rp.length - 1; i++) {
                            ResultPoint topLeft = lastResult.getResultPoints()[i];
                            ResultPoint bottomRight = lastResult.getResultPoints()[i + 1];
                            Drawing.drawText(rgba, String.valueOf(i + 1), new Point(bottomRight.getX(), bottomRight.getY()), 1.0f, blue);
                            Drawing.drawLine(rgba, new Point(topLeft.getX(), topLeft.getY()), new Point(bottomRight.getX(), bottomRight.getY()), new ColorRGBA("#ff0000"));
                        }
                        if (rp.length >= 3) {
                            Contour c = new Contour(new MatOfPoint(box));
                            Drawing.drawLine(rgba, box[0], box[1], green);
                            Drawing.drawLine(rgba, box[1], box[2], green);
                            Drawing.drawLine(rgba, box[2], box[3], green);
                            Drawing.drawLine(rgba, box[3], box[0], green);

                            Drawing.drawRectangle(rgba, newBox[0], newBox[1], red);

                            Drawing.drawText(rgba, "line1: " + line1 + " line2: " + line2 + " line2/line1: " + (line2 / line1), new Point(0, 110), 1.0f, white);
                            Drawing.drawText(rgba, "angle: " + angleOfRotation + " deg: " + Math.toDegrees(angleOfRotation) + " newangle: " + newAngleOfRotation + " deg: " + Math.toDegrees(newAngleOfRotation), new Point(0, 140), 1.0f, white);
                            Drawing.drawText(rgba, "new len: " + newSquareSideLength, new Point(0, 170), 1.0f, white);
                            Drawing.drawText(rgba, "new rect: " + Arrays.toString(newBox), new Point(0, 200), 1.0f, white);
                            Drawing.drawText(rgba, "oppuppermostindex: " + oppUpperMostIndex + " otherindex: " + otherIndex, new Point(0, 230), 1.0f, white);
                        }
                    }
                }
            } else if(matDebugInfo) {
                ResultPoint[] rp = lastResult.getResultPoints();
                if(rp.length >= 3) {
                    Drawing.drawText(rgba, "0", new Point(rp[0].getX(), rp[0].getY()), 1.0f, new ColorRGBA("#88ccff"));
                    for (int i = 0; i < rp.length - 1; i++) {
                        ResultPoint topLeft = lastResult.getResultPoints()[i];
                        ResultPoint bottomRight = lastResult.getResultPoints()[i + 1];
                        Drawing.drawText(rgba, String.valueOf(i + 1), new Point(bottomRight.getX(), bottomRight.getY()), 1.0f, new ColorRGBA("#88ccff"));
                        Drawing.drawLine(rgba, new Point(topLeft.getX(), topLeft.getY()), new Point(bottomRight.getX(), bottomRight.getY()), new ColorRGBA("#ff0000"));
                    }
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

    //Returns {r,g,b}
    public int[] getAvgColor(Mat m, Rectangle box) {
        if(m.type() != CvType.CV_8UC4) {
            throw new RuntimeException("Unable to find average color within mat: Unknown mat type.");
        }
        double red = 0;
        double green = 0;
        double blue = 0;
        int count = 0;
        for(int x = (int)box.left(); x < box.right(); x += GRAY_CARD_SAMPLE_INTERVAL) {
            for(int y = (int)box.top(); y < box.bottom(); y += GRAY_CARD_SAMPLE_INTERVAL) {
                double[] pt = m.get(y, x);
                red += pt[0];
                green += pt[1];
                blue += pt[2];
                count++;
            }
        }
        if(count == 0) {
            double[] rgba = m.get((int)box.top(), (int)box.left());
            return new int[] {(int)rgba[0], (int)rgba[1], (int)rgba[2]};
        }
        return new int[] {(int)(red / count), (int)(green / count), (int)(blue / count)};
    }

    @Override
    public void stop(VisionOpMode opmode) {
        //do nothing
    }
}