package com.xtremelabs.robolectric.fakes;

import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ItemizedOverlay.class)
public class ShadowItemizedOverlay {
    public boolean populated;
    public boolean shouldHit;
    public boolean lastFocusedIndexWasReset;
    
    @Implementation
    public final void populate() {
        populated = true;
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
}
