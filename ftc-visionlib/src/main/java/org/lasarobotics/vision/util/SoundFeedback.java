package org.lasarobotics.vision.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;

/**
 * Sound feedback via phone's speakers
 */
public class SoundFeedback {
    private SoundFeedback() {
    }

    public static void defaultNotification(Context context) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void playBeep(Stream s) {
        ToneGenerator toneGen1 = new ToneGenerator(s.val, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
    }

    private static void maximizeVolume(Context context, Stream s) {
        AudioManager am =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        am.setStreamVolume(
                s.val,
                am.getStreamMaxVolume(s.val),
                0);
    }

    public static void playMaxBeepOnRing(Context c) {
        maximizeVolume(c, Stream.RING);
        playBeep(Stream.RING);
    }

    public enum Stream {
        VOICE_CALL(0),
        SYSTEM(1),
        RING(2),
        MUSIC(3),
        ALARM(4),
        NOTIFICATION(5);

        private final int val;

        Stream(int val) {
            this.val = val;
        }
    }
}
