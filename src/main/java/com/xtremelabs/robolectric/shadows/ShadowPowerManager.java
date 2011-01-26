package com.xtremelabs.robolectric.shadows;

import android.os.PowerManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadows the {@code android.os.PowerManager} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(PowerManager.class)
public class ShadowPowerManager {
    @Implementation
    public PowerManager.WakeLock newWakeLock(int flags, String tag) {
        return Robolectric.newInstanceOf(PowerManager.WakeLock.class);
    }
}
