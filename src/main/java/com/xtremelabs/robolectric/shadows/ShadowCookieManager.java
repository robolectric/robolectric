package com.xtremelabs.robolectric.shadows;

import android.webkit.CookieManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadows the {@code android.telephony.TelephonyManager} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(CookieManager.class)
public class ShadowCookieManager {
    private static CookieManager sRef;

    @Implementation
    public static CookieManager getInstance() {
        if (sRef == null) {
            sRef = Robolectric.newInstanceOf(CookieManager.class);
        }
        return sRef;
    }
}
