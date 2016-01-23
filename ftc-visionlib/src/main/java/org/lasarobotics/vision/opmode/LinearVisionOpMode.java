package org.lasarobotics.vision.opmode;

import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

/**
 * Linear version of the Vision OpMode
 * This includes code from the FIRST library (C) Qualcomm as of 1/23/2016
 */
public abstract class LinearVisionOpMode extends VisionOpMode {
    private Threader threader = null;
    private Thread thread = null;
    private ElapsedTime timer = new ElapsedTime();
    private volatile boolean opModeStarted = false;

    public LinearVisionOpMode() {

    }

    public abstract void runOpMode() throws InterruptedException;

    public final void waitForVisionStart() throws InterruptedException {
        while (!this.isInitialized()) {
            synchronized (this) {
                this.wait();
            }
        }
    }

    public synchronized void waitForStart() throws InterruptedException {
        while (!this.opModeStarted) {
            synchronized (this) {
                this.wait();
            }
        }
    }

    public void waitOneFullHardwareCycle() throws InterruptedException {
        this.waitForNextHardwareCycle();
        Thread.sleep(1L);
        this.waitForNextHardwareCycle();
    }

    public void waitForNextHardwareCycle() throws InterruptedException {
        synchronized (this) {
            this.wait();
        }
    }

    public void sleep(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }

    public boolean opModeIsActive() {
        return this.opModeStarted;
    }

    @Override
    public final void init() {
        super.init();
        this.threader = new Threader(this);
        this.thread = new Thread(this.threader, "Linear OpMode Helper");
        this.thread.start();
    }

    @Override
    public final void init_loop() {
        this.notifyOrThrowError();
    }

    @Override
    public final void start() {
        this.opModeStarted = true;
        synchronized (this) {
            this.notifyAll();
        }
    }

    @Override
    public final void loop() {
        super.loop();
        this.notifyOrThrowError();
    }

    @Override
    public final void stop() {
        super.stop();
        this.opModeStarted = false;
        if (!this.threader.isReady()) {
            this.thread.interrupt();
        }

        this.timer.reset();

        while (!this.threader.isReady() && this.timer.time() < 0.5D) {
            Thread.yield();
        }

        if (!this.threader.isReady()) {
            RobotLog.e("*****************************************************************");
            RobotLog.e("User Linear Op Mode took too long to exit; emergency killing app.");
            RobotLog.e("Possible infinite loop in user code?");
            RobotLog.e("*****************************************************************");
            System.exit(-1);
        }
    }

    private void notifyOrThrowError() {
        if (this.threader.hasError()) {
            throw this.threader.getLastError();
        } else {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    private static class Threader implements Runnable {
        private final LinearVisionOpMode opModeReference;
        private RuntimeException lastError = null;
        private boolean ready = false;

        public Threader(LinearVisionOpMode var1) {
            this.opModeReference = var1;
        }

        public void run() {
            this.lastError = null;
            this.ready = false;

            try {
                this.opModeReference.runOpMode();
            } catch (InterruptedException var6) {
                RobotLog.d("LinearOpMode received an Interrupted Exception; shutting down this linear op mode");
            } catch (RuntimeException var7) {
                this.lastError = var7;
            } finally {
                this.ready = true;
            }

        }

        public boolean hasError() {
            return this.lastError != null;
        }

        public RuntimeException getLastError() {
            return this.lastError;
        }

        public boolean isReady() {
            return this.ready;
        }
    }
}