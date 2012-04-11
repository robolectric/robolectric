package com.xtremelabs.robolectric.shadows;

import android.support.v4.content.LocalBroadcastManager;
import com.xtremelabs.robolectric.Robolectric;

/**
 * This class is not a a real shadow for now - we just use the real LocalBroadcastManager and
 * reset its state after each test run.
 */
public class ShadowLocalBroadcastManager {
    public static void reset() {
        Robolectric.Reflection.setFinalStaticField(LocalBroadcastManager.class, "mInstance", null);
    }
}
