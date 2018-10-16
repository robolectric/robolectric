// BEGIN-INTERNAL
package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.graphics.text.MeasuredText;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = MeasuredText.Builder.class, minSdk = Q)
public class ShadowMeasuredTextBuilder {

    @Implementation
    protected void ensureNativePtrNoReuse() {
        // Do nothing.
    }
}
// END-INTERNAL
