package org.robolectric.shadows;

import android.app.KeyguardManager;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(KeyguardManager.KeyguardLock.class)
public class ShadowKeyGuardLock {
    private boolean keyguardEnabled = true;

    @Implementation
    public void disableKeyguard() {
        keyguardEnabled = false;
    }

    @Implementation
    public void reenableKeyguard() {
        keyguardEnabled = true;
    }

    public boolean isEnabled() {
        return keyguardEnabled;
    }
}
