package org.lasarobotics.vision.opmode.extensions;

import android.hardware.SensorManager;

import org.lasarobotics.vision.opmode.VisionOpMode;
import org.lasarobotics.vision.util.Accelerometer;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * If car is not accelerating, apply linear fitting to the distance values to reduce noise.
 */
public class DistanceLinearization implements VisionExtension {

    private static final int VALUES_HEAP_SIZE = 5; //How many points should be stored at a time for linearization
    private static final int MAIN_AXIS = SensorManager.AXIS_Z - 1; //Plug in the axis that is horizontal relative
    //to the beacon

    private Accelerometer accelerometer;
    private List<Point> previousValues;

    private double currentTime = 0;

    public void init(VisionOpMode opmode) {
        accelerometer = new Accelerometer();
        previousValues = new ArrayList<>();
    }

    public void loop(VisionOpMode visionOpMode) {

    }

    public Mat frame(VisionOpMode opmode, Mat rgba, Mat gray) {
        if(VALUES_HEAP_SIZE < 1)
            return rgba;
        if(accelerometer.isAccelerating(MAIN_AXIS)) {
            currentTime = 0;
            previousValues = new ArrayList<>();
            return rgba;
        }
        currentTime += 1.0/opmode.getFPS();
        previousValues.add(new Point(currentTime, opmode.getBeaconState().getAnalysis().getRadius()));
        if(previousValues.size() > VALUES_HEAP_SIZE)
            previousValues.remove(0);
        if(previousValues.size() > 1)
            opmode.getBeaconState().getAnalysis().setRadius(linearEstimate(opmode.getBeaconState().getAnalysis().getRadius()));
        return rgba;
    }

    private double linearEstimate(double radius) {
        double sumX = 0;
        double sumY = 0;
        double sumX2 = 0;
        double sumXY = 0;
        for(Point p : previousValues) {
            sumX += p.x;
            sumY += p.y;
            sumX2 += p.x*p.x;
            sumXY += p.x*p.y;
        }
        double xMean = sumX/previousValues.size();
        double yMean = sumY/previousValues.size();
        double slope;
        if(sumX2 - sumX * xMean != 0)
            slope = (sumXY - sumX * yMean) / (sumX2 - sumX * xMean);
        else
            return radius;
        double yInt = yMean - slope * xMean;
        return slope * currentTime + yInt;
    }

    public void stop(VisionOpMode opMode) {
        accelerometer = null;
    }
}
