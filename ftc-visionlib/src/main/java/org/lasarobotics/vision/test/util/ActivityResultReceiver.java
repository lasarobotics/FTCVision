package org.lasarobotics.vision.test.util;

import android.content.Intent;

/**
 * Created by Russell on 11/26/2015.
 */
public interface ActivityResultReceiver {
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
