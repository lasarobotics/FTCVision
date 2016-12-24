/*
 * Copyright (c) 2016 Arthur Pachachura, LASA Robotics, and contributors
 * MIT licensed
 *
 * Some code from FIRST library, Copyright (C) Qualcomm
 *
 * Thank you to Russell Coleman (LASA).
 */
package org.lasarobotics.vision.opmode;

import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;

import org.firstinspires.ftc.robotcore.internal.TelemetryInternal;
import org.lasarobotics.vision.util.color.Color;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Linear version of the Vision OpMode
 * This includes code from the FIRST library (C) Qualcomm as of 1/23/2016
 */
public abstract class LinearVisionOpMode extends VisionOpMode {

    //------------------------------------------------------------------------------------------------
    // State
    //------------------------------------------------------------------------------------------------

    private LinearOpModeHelper helper          = null;
    private ExecutorService executorService = null;
    private volatile boolean   isStarted       = false;
    private volatile boolean   stopRequested   = false;

    //------------------------------------------------------------------------------------------------
    // Construction
    //------------------------------------------------------------------------------------------------

    public LinearVisionOpMode() {
    }

    private final ElapsedTime timer = new ElapsedTime();
    private Mat rgba;
    private Mat gray;
    private boolean hasNewFrame = false;

    @Override
    public final Mat frame(Mat rgba, Mat gray) {
        if (!isStarted) return rgba;
        this.rgba = super.frame(rgba, gray);
        Imgproc.cvtColor(rgba, this.gray, Imgproc.COLOR_RGBA2GRAY);
        hasNewFrame = true;
        return rgba;
    }

    public final Mat getFrameRgba() {
        return rgba;
    }

    public final Mat getFrameGray() {
        return gray;
    }

    public boolean hasNewFrame() {
        return hasNewFrame;
    }

    public void discardFrame() {
        hasNewFrame = false;
    }

    /**
     * Override this method and place your code here.
     * <p>
     * Please do not swallow the InterruptedException, as it is used in cases
     * where the op mode needs to be terminated early.
     * @throws InterruptedException
     */
    abstract public void runOpMode() throws InterruptedException;

    public final void waitForVisionStart() throws InterruptedException {

        while (!this.isInitialized()) {
            synchronized (this) {
                this.wait();
            }
        }

    }

    /**
     * Pause the Linear Op Mode until start has been pressed
     * @throws InterruptedException
     */
    public synchronized void waitForStart() throws InterruptedException {
    /*
     * If an InterruptedException is thrown we won't handle it, instead
     * we will pass it up to the calling method to handle.
     *
     * In the case of the linear op mode; this will likely cause the
     * thread to terminate.
     */

        while (!isStarted) {
            synchronized (this) {
                this.wait();
            }
        }
    }

    /**
     * Wait for one full cycle of the hardware
     * <p>
     * Each cycle of the hardware your commands are sent out to the hardware; and
     * the latest data is read back in.
     * <p>
     * This method has a strong guarantee to wait for <strong>at least</strong> one
     * full hardware hardware cycle.
     * @throws InterruptedException
     *
     * @deprecated The need for user code to synchronize with the loop() thread has been
     *             obviated by improvements in the modern motor and servo controller implementations.
     *             Remaining uses of this API are likely unncessarily wasting cycles. If a simple non-zero
     *             delay is required, the {@link Thread#sleep(long) sleep()} method is a better choice.
     *             If one simply wants to allow other threads to run, {@link #idle()} is a good choice.
     *
     * @see Thread#sleep(long)
     * @see #idle()
     * @see #waitForNextHardwareCycle()
     */
    @Deprecated
    public void waitOneFullHardwareCycle() throws InterruptedException {
        // wait for current partial cycle to finish
        waitForNextHardwareCycle();

        // wait for the next hardware cycle to start
        Thread.sleep(1);

        // now wait one full cycle
        waitForNextHardwareCycle();
    }

    /**
     * Wait for the start of the next hardware cycle
     * <p>
     * Each cycle of the hardware your commands are sent out to the hardware; and
     * the latest data is read back in.
     * <p>
     * This method will wait for the current hardware cycle to finish, which is
     * also the start of the next hardware cycle.
     * @throws InterruptedException
     *
     * @deprecated The need for user code to synchronize with the loop() thread has been
     *             obviated by improvements in the modern motor and servo controller implementations.
     *             Remaining uses of this API are likely unncessarily wasting cycles. If a simple non-zero
     *             delay is required, the {@link Thread#sleep(long) sleep()} method is a better choice.
     *             If one simply wants to allow other threads to run, {@link #idle()} is a good choice.
     *
     * @see Thread#sleep(long)
     * @see #idle()
     * @see #waitOneFullHardwareCycle()
     */
    @Deprecated
    public void waitForNextHardwareCycle() throws InterruptedException {
    /*
     * If an InterruptedException is thrown we won't handle it, instead
     * we will pass it up to the calling method to handle.
     *
     * In the case of the linear op mode; this will likely cause the
     * thread to terminate.
     */
        synchronized (this) {
            this.wait();
        }
    }

    /**
     * Puts the current thread to sleep for a bit as it has nothing better to do. This allows other
     * threads in the system to run.
     *
     * One should use this method when you have nothing better to do in your code, usually
     * at the very end of your while(opModeIsActive()) loop in TeleOp. Calling idle()
     * is entirely optional: it just helps make the system a little more responsive and a
     * little more efficient.
     *
     * {@link #idle()} is conceptually related to waitOneFullHardwareCycle(), but makes no
     * guarantees as to completing any particular number of hardware cycles, if any.
     *
     * @throws InterruptedException thrown if the thread is interrupted
     * @see #waitOneFullHardwareCycle()
     */
    public final void idle() throws InterruptedException {
        // Abort the OpMode if we've been asked to stop
        if (this.isStopRequested())
            throw new InterruptedException();

        // Otherwise, yield back our thread scheduling quantum and give other threads at
        // our priority level a chance to run
        Thread.yield();
    }

    /**
     * Sleep for the given amount of milliseconds. This is simple shorthand for the operating-system-
     * provided {@link Thread#sleep(long) sleep()} method.
     *
     * @param milliseconds amount of time to sleep, in milliseconds
     * @throws InterruptedException
     * @see Thread#sleep(long)
     */
    public final void sleep(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }

    /**
     * Answer as to whether this opMode is active and the robot should continue onwards. If the
     * opMode is not active, the OpMode should terminate at its earliest convenience.
     *
     * @return whether the OpMode is currently active. If this returns false, you should
     *         break out of the loop in your {@link #runOpMode()} method and return to its caller.
     * @see #runOpMode()
     * @see #isStarted()
     * @see #isStopRequested()
     */
    public final boolean opModeIsActive() {
        return !this.isStopRequested() && this.isStarted();
    }

    /**
     * Has the opMode been started?
     *
     * @return whether this opMode has been started or not
     * @see #opModeIsActive()
     * @see #isStopRequested()
     */
    public final boolean isStarted() {
        return this.isStarted;
    }

    /**
     * Has the the stopping of the opMode been requested?
     *
     * @return whether stopping opMode has been requested or not
     * @see #opModeIsActive()
     * @see #isStarted()
     */
    public final boolean isStopRequested() {
        return this.stopRequested || Thread.currentThread().isInterrupted();
    }

    /**
     * From the non-linear OpMode; do not override
     */
    @Override
    public final void init() {
        super.init();
        hasNewFrame = false;
        this.rgba = Color.createMatRGBA(width, height);
        this.gray = Color.createMatGRAY(width, height);

        this.executorService = ThreadPool.newSingleThreadExecutor();
        this.helper          = new LinearOpModeHelper();
        this.isStarted       = false;
        this.stopRequested   = false;

        this.executorService.execute(helper);
    }

    /**
     * From the non-linear OpMode; do not override
     */
    @Override
    final public void init_loop() {
        handleLoop();
    }

    /**
     * From the non-linear OpMode; do not override
     */
    @Override
    final public void start() {
        stopRequested = false;
        isStarted = true;
        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
     * From the non-linear OpMode; do not override
     */
    @Override
    final public void loop() {
        handleLoop();
    }


    /**
     * From the non-linear OpMode; do not override
     */
    @Override
    final public void stop() {
        super.stop();
        this.rgba.release();
        this.gray.release();

        // make isStopRequested() return true (and opModeIsActive() return false)
        stopRequested = true;

        if (executorService != null) {  // paranoia

            // interrupt the linear opMode and shutdown it's service thread
            executorService.shutdownNow();

            /** Wait, forever, for the OpMode to stop. If this takes too long, then
             * {@link OpModeManagerImpl#callActiveOpModeStop()} will catch that and take action */
            try {
                String serviceName = "user linear op mode";
                ThreadPool.awaitTermination(executorService, 100, TimeUnit.DAYS, serviceName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected void handleLoop() {
        // if there is a runtime exception in user code; throw it so the normal error
        // reporting process can handle it
        if (helper.hasRuntimeException()) {
            throw helper.getRuntimeException();
        }

        synchronized (this) {
            this.notifyAll();
        }
    }

    protected class LinearOpModeHelper implements Runnable {

        protected RuntimeException exception  = null;
        protected boolean          isShutdown = false;

        public LinearOpModeHelper() {
        }

        @Override
        public void run() {
            ThreadPool.logThreadLifeCycle("LinearOpMode main", new Runnable() { @Override public void run() {
                exception = null;
                isShutdown = false;

                try {
                    LinearVisionOpMode.this.runOpMode();
                    requestOpModeStop();
                } catch (InterruptedException ie) {
                    // InterruptedException, shutting down the op mode
                    RobotLog.d("LinearOpMode received an InterruptedException; shutting down this linear op mode");
                } catch (CancellationException ie) {
                    // In our system, CancellationExceptions are thrown when data was trying to be acquired, but
                    // an interrupt occurred, and you're in the unfortunate situation that the data acquisition API
                    // involved doesn't allow InterruptedExceptions to be thrown. You can't return (what data would
                    // you return?), and so you have to throw a RuntimeException. CancellationException seems the
                    // best choice.
                    RobotLog.d("LinearOpMode received a CancellationException; shutting down this linear op mode");
                } catch (RuntimeException e) {
                    exception = e;
                } finally {
                    isShutdown = true;
                }
            }});
        }
        public boolean hasRuntimeException() {
            return (exception != null);
        }

        public RuntimeException getRuntimeException() {
            return exception;
        }

        public boolean isShutdown() {
            return isShutdown;
        }
    }

    //----------------------------------------------------------------------------------------------
    // Telemetry management
    //----------------------------------------------------------------------------------------------

    @Override protected void postInitLoop() {
        // Do NOT call super, as that updates telemetry unilaterally
        if (telemetry instanceof TelemetryInternal) {
            ((TelemetryInternal)telemetry).tryUpdateIfDirty();
        }
    }

    @Override protected void postLoop() {
        // Do NOT call super, as that updates telemetry unilaterally
        if (telemetry instanceof TelemetryInternal) {
            ((TelemetryInternal)telemetry).tryUpdateIfDirty();
        }
    }
}
