package com.lasarobotics.tests.camera;

import android.hardware.SensorManager;

import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.lasarobotics.vision.util.Accelerometer;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for distance linearization extension in camera activity.
 */
public class DistanceLinearizationTest {

    private static final int VALUES_HEAP_SIZE = 5; //How many points should be stored at a time for linearization
    private static final int MAIN_AXIS = SensorManager.AXIS_Z - 1; //Plug in the axis that is horizontal relative
    //to the beacon

    private Accelerometer accelerometer = new Accelerometer();
    private List<Point> previousValues = new ArrayList<>();

    private double currentTime = 0;

    public void update(double fps, Beacon.BeaconAnalysis beaconState) {
        if(VALUES_HEAP_SIZE < 1)
            return;
        if(accelerometer.isAccelerating(MAIN_AXIS)) {
            currentTime = 0;
            previousValues = new ArrayList<>();
            return;
        }
        currentTime += 1.0/fps;
        previousValues.add(new Point(currentTime, beaconState.getRadius()));
        if(previousValues.size() > VALUES_HEAP_SIZE)
            previousValues.remove(0);
        if(previousValues.size() > 1)
            beaconState.setRadius(linearEstimate(beaconState.getRadius()));
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
}
