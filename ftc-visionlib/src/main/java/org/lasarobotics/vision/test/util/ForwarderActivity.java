package org.lasarobotics.vision.test.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Russell on 11/26/2015.
 */
public class ForwarderActivity extends Activity {
    private boolean started = false;
    private ActivityResultReceiver arr;
    private Intent intent;
    public ForwarderActivity(ActivityResultReceiver arr, Intent intent) {
        this.arr = arr;
        this.intent = intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!started) {
            started = true;
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        arr.onActivityResult(requestCode, resultCode, data);
    }
}