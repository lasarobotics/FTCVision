package org.lasarobotics.vision.test.detection;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.integration.android.IntentIntegrator;

import org.lasarobotics.vision.test.util.ForwarderActivity;

/**
 * Created by Russell on 11/26/2015.
 */
public class QRDetector {
    Activity context;

    public QRDetector(Activity context) {
        this.context = context;
    }

    public void intiateDetection() {
        Intent i = new Intent(context, ForwarderActivity.class);
    }
}
