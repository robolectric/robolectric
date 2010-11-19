package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

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

    public boolean isPopulated() {
        return isPopulated;
    }

    public void setIsPopulated(boolean isPopulated) {
        this.isPopulated = isPopulated;
    }

    public boolean shouldHit() {
        return shouldHit;
    }

    public void setShouldHit(boolean shouldHit) {
        this.shouldHit = shouldHit;
    }

    public boolean lastFocusedIndexWasReset() {
        return lastFocusedIndexWasReset;
    }
}
