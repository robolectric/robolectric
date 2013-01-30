package com.xtremelabs.robolectric.shadows;

import android.app.KeyguardManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

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
