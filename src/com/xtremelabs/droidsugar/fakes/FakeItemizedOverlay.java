package com.xtremelabs.droidsugar.fakes;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ItemizedOverlay.class)
public class FakeItemizedOverlay {
    public boolean populated;
    public boolean shouldHit;
    
    protected final void populate() {
        populated = true;
    }

    protected boolean hitTest(OverlayItem item, android.graphics.drawable.Drawable drawable, int i, int i1) {
        return shouldHit;
    }
}
