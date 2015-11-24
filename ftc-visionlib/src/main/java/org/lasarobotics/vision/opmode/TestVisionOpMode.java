package org.lasarobotics.vision.opmode;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.lasarobotics.vision.opmode.extensions.BeaconColorExtension;

/**
 * Created by Russell Coleman on 11/23/2015.
 */
public class TestVisionOpMode extends VisionOpMode {
    BeaconColorExtension bce = new BeaconColorExtension();
    private int beaconWaitCounter = 0;
    private int lastBeaconCenterX = 0;
    private int absoluteCenter; //Image width divided by two
    private int moveThreshold; //Allowed deviation of beacon from image center
    private static final int BEACON_WAIT_TIME = 5; //Time to wait before rotation if unable to find beacon
    private static final double ROTATE_MOTOR_POWER = 0.1; //Motor power when rotating
    private static final double DRIVE_MOTOR_POWER = 0.2; //Motor power when driving
    private static final double UNCERTAIN_MOTOR_POWER = 0; //Motor power when unsure what to do
    DcMotor leftBack, rightBack, leftFront, rightFront; //Motors

    @Override
    public void init() {
        super.init();
        bce.init(this);
        leftBack = hardwareMap.dcMotor.get("leftBack");
        rightBack = hardwareMap.dcMotor.get("rightBack");
        leftFront = hardwareMap.dcMotor.get("leftFront");
        rightFront = hardwareMap.dcMotor.get("rightFront");
    }

    @Override
    public void loop() {
        super.loop();
        bce.loop(this);
        absoluteCenter = height/2;
        moveThreshold = height/19;

        int beaconCenterX = (100 + 100)/2; //TODO replace 100s with actual values
        if(/* TODO FOUND BEACON */ true) {
            if(!withinThreshold(beaconCenterX, lastBeaconCenterX, moveThreshold)) {
                //Last known location of beacon is drastically different from current location
                if(beaconWaitCounter++ < BEACON_WAIT_TIME) {
                    unsureMotors();
                    return;
                }
            }
            shouldMove(beaconCenterX);
            beaconWaitCounter = 0;
            lastBeaconCenterX = beaconCenterX;
        } else {
            findBeacon(beaconCenterX);
        }
    }

    public void findBeacon(int beaconCenterX) {
        //Have we waited long enough?
        if(beaconWaitCounter < BEACON_WAIT_TIME) {
            beaconWaitCounter++;
            unsureMotors();
            return; //No we haven't
        }

        //Yes we have
        rotateLeft();
    }

    public void rotateLeft() {
        rightFront.setPower(ROTATE_MOTOR_POWER);
        rightBack.setPower(ROTATE_MOTOR_POWER);
    }

    public void rotateRight() {
        leftFront.setPower(ROTATE_MOTOR_POWER);
        leftBack.setPower(ROTATE_MOTOR_POWER);
    }

    public void driveForward() {
        rightFront.setPower(DRIVE_MOTOR_POWER);
        rightBack.setPower(DRIVE_MOTOR_POWER);
        leftFront.setPower(DRIVE_MOTOR_POWER);
        leftBack.setPower(DRIVE_MOTOR_POWER);
    }

    public void unsureMotors() {
        rightFront.setPower(UNCERTAIN_MOTOR_POWER);
        rightBack.setPower(UNCERTAIN_MOTOR_POWER);
        leftFront.setPower(UNCERTAIN_MOTOR_POWER);
        leftBack.setPower(UNCERTAIN_MOTOR_POWER);
    }

    public void shouldMove(int beaconCenterX) {
        if(withinThreshold(beaconCenterX, absoluteCenter, moveThreshold)) {
            driveForward();
        } else {
            if(beaconCenterX < absoluteCenter) {
                rotateLeft();
            } else {
                rotateRight();
            }
        }
    }

    public static boolean withinThreshold(int item1, int item2, int threshold) { //TODO move to helper method in some other class
        int distanceFrom = Math.abs(item1 - item2);
        return distanceFrom <= threshold;
    }

    @Override
    public void stop() {
        super.stop();
        bce.stop(this);
    }
}
