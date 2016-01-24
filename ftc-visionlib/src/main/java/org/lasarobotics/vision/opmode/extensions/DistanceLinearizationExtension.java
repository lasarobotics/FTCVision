package org.lasarobotics.vision.opmode.extensions;

import org.lasarobotics.vision.android.Sensors;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * If car is not accelerating, apply linear fitting to the distance values to reduce noise.
 */
public class DistanceLinearizationExtension implements VisionExtension {

    private static final int VALUES_HEAP_SIZE = 5; //How many points should be stored at a time for linearization
    private static final Sensors.Axis MAIN_AXIS = Sensors.Axis.Z; //Plug in the axis that is horizontal relative to the beacon

    private List<Point> previousValues;

    private double currentTime = 0;

    public void init(VisionOpMode opmode) {
        previousValues = new ArrayList<>();
    }

    public void loop(VisionOpMode opmode) {

    }

    public Mat frame(VisionOpMode opmode, Mat rgba, Mat gray) {
        if (opmode.sensors.isAccelerating(MAIN_AXIS)) {
            currentTime = 0;
            previousValues = new ArrayList<>();
            return rgba;
        }
        currentTime += 1.0/opmode.getFPS();
        previousValues.add(new Point(currentTime, opmode.getBeaconState().getAnalysis().getDistance()));
        if(previousValues.size() > VALUES_HEAP_SIZE)
            previousValues.remove(0);
        if(previousValues.size() > 1)
            VisionOpMode.beacon.analysis = new Beacon.BeaconAnalysis(VisionOpMode.beacon.analysis,
                    linearEstimate(opmode.getBeaconState().getAnalysis().getDistance()));
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

    public void stop(VisionOpMode opmode) {

    }
}
