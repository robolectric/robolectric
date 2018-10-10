// BEGIN-INTERNAL
package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.graphics.text.MeasuredText;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(value = MeasuredText.Builder.class, minSdk = Q)
public class ShadowMeasuredTextBuilder {

    private static int nativeCounter = 1;

    @Implementation
    protected static long nInitBuilder() {
        return ++nativeCounter;
    }

    @Implementation
    protected static long nBuildMeasuredText(
        long nativeBuilderPtr,
        char[] text,
        boolean computeHyphenation,
        boolean computeLayout) {
        return ++nativeCounter;
    }

    @Resetter
    public static void reset() {
        nativeCounter = 0;
    }
}
// END-INTERNAL
