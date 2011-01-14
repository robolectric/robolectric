package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow for {@code ItemizedOverlay} that just keeps track of what has been called and enables the return value for
 * {@link #hitTest(com.google.android.maps.OverlayItem, android.graphics.drawable.Drawable, int, int)} to be set up by
 * tests.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(ItemizedOverlay.class)
public class ShadowItemizedOverlay {
    private boolean isPopulated;
    private boolean shouldHit;
    private boolean lastFocusedIndexWasReset;

    @Implementation
    public final void populate() {
        isPopulated = true;
    }

    @Implementation
    public boolean hitTest(OverlayItem item, android.graphics.drawable.Drawable drawable, int i, int i1) {
        return shouldHit;
    }

    @Implementation
    public void setLastFocusedIndex(int i) {
        lastFocusedIndexWasReset = (i == -1);
    }

    @Implementation
    public static Drawable boundCenterBottom(Drawable drawable) {
        return drawable;
    }

    /**
     * Non-Android accessor that indicates whether {@link #setLastFocusedIndex(int)} has been called with a value other
     * than -1.
     *
     * @return whether {@link #setLastFocusedIndex(int)} has been called with a value other
     *         than -1
     */
    public boolean lastFocusedIndexWasReset() {
        return lastFocusedIndexWasReset;
    }

    public void setIsPopulated(boolean isPopulated) {
        this.isPopulated = isPopulated;
    }

    public boolean isPopulated() {
        return isPopulated;
    }

    /**
     * Sets up the return value for
     * {@link #hitTest(com.google.android.maps.OverlayItem, android.graphics.drawable.Drawable, int, int)}
     *
     * @param shouldHit the value that
     *                  {@link #hitTest(com.google.android.maps.OverlayItem, android.graphics.drawable.Drawable, int, int)}
     *                  should return
     */
    public void setShouldHit(boolean shouldHit) {
        this.shouldHit = shouldHit;
    }
}
