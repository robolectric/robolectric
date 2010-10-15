package com.xtremelabs.robolectric.fakes;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ItemizedOverlay.class)
public class FakeItemizedOverlay {
    public boolean populated;
    public boolean shouldHit;
    public boolean lastFocusedIndexWasReset;
    
    @Implementation
    protected final void populate() {
        populated = true;
    }

    @Implementation
    protected boolean hitTest(OverlayItem item, android.graphics.drawable.Drawable drawable, int i, int i1) {
        return shouldHit;
    }

    @Implementation
    protected void setLastFocusedIndex(int i) {
        lastFocusedIndexWasReset = (i == -1);
    }
}
